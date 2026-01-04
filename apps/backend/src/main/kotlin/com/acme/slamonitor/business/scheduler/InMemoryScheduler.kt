package com.acme.slamonitor.business.scheduler

import com.acme.slamonitor.business.scheduler.dto.EndpointBad
import com.acme.slamonitor.business.scheduler.dto.EndpointOk
import com.acme.slamonitor.business.scheduler.dto.EndpointRef
import com.acme.slamonitor.business.scheduler.dto.EndpointResult
import com.acme.slamonitor.business.scheduler.dto.EndpointView
import com.acme.slamonitor.business.scheduler.dto.LocalState
import com.acme.slamonitor.persistence.CheckResultRepository
import com.acme.slamonitor.persistence.EndpointRepository
import com.acme.slamonitor.persistence.domain.EndpointEntity
import com.acme.slamonitor.scope.JpaIoWorkerCoroutineDispatcher
import com.acme.slamonitor.utils.SchedulerConfig
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.sql.Types
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.PriorityQueue
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate

/**
 * Планировщик проверок в памяти с очередью, воркерами и flush в БД.
 */
open class InMemoryScheduler(
    private val endpointRepository: EndpointRepository,
    private val checkResultRepository: CheckResultRepository,
    private val jdbcTemplate: JdbcTemplate,
    private val processor: EndpointProcessor,
    private val jpaAsyncIoWorker: JpaIoWorkerCoroutineDispatcher
) {
    private val heapMutex = Mutex()

    private val nudge = Channel<Unit>(Channel.CONFLATED)
    private val outbox = Channel<EndpointResult>(capacity = CONFIG.outboxCapacity)
    private val workQueue = Channel<UUID>(capacity = CONFIG.workQueueCapacity)

    private val registry = ConcurrentHashMap<UUID, EndpointView>()
    private val readyHeap = PriorityQueue(compareBy<EndpointRef> { it.nextRunAt }.thenBy { it.id })

    /**
     * Запускает все циклы планировщика в отдельных корутинах.
     */
    suspend fun runLoops(
        refreshLoopDispatcher: CoroutineDispatcher,
        feedLoopDispatcher: CoroutineDispatcher,
        workerDispatcher: CoroutineDispatcher,
        flusherLoopDispatcher: CoroutineDispatcher,
        cleanDataLoopDispatcher: CoroutineDispatcher,
    ) = coroutineScope {
        launch(refreshLoopDispatcher) { refreshLoop() }
        launch(feedLoopDispatcher) { feedLoop() }
        repeat(CONFIG.workers) { launch(workerDispatcher) { workerLoop() } }
        launch(flusherLoopDispatcher) { flusherLoop() }
        launch(cleanDataLoopDispatcher) { cleanDatabase() }
    }

    /**
     * Периодически синхронизирует локальный реестр с БД.
     */
    private suspend fun refreshLoop() {
        while (currentCoroutineContext().isActive) {
            val now = Instant.now(CLOCK)
            val seen = HashSet<UUID>(4096)
            val changedEndpoints = ArrayList<UUID>(1024)
            var page = 0

            while (true) {
                val endpointMetas = jpaAsyncIoWorker.executeWithTransactionalSupplier("Getting all endpoints") {
                    endpointRepository.findAllMeta(PageRequest.of(page, CONFIG.metaPageSize))
                }
                if (endpointMetas.isEmpty()) break

                for (meta in endpointMetas) {
                    seen += meta.getId()

                    val isChanged = registry[meta.getId()].let {
                        it == null ||
                                it.dbVersion != meta.getVersion() ||
                                it.enabled != meta.getEnabled() ||
                                it.intervalSec != meta.getIntervalSec()
                    }

                    if (isChanged) changedEndpoints += meta.getId()
                }
                page++
            }

            if (changedEndpoints.isNotEmpty()) {
                val entities =
                    jpaAsyncIoWorker.executeWithTransactionalSupplier("Getting endpoint: $changedEndpoints") {
                        endpointRepository.findByIdInAndIsDeletedFalse(changedEndpoints)
                    }

                for (changeEntity in entities) {
                    if (!changeEntity.enabled) {
                        registry.remove(changeEntity.id)
                        LOG.info("$changeEntity has been removed from queue worker")
                        continue
                    }

                    registry.compute(changeEntity.id) { _, existEntity ->
                        val preservedState = existEntity?.localState
                            ?.takeIf { it == LocalState.RUNNING || it == LocalState.IN_FLIGHT }
                            ?: LocalState.READY_LOCAL

                        EndpointView(
                            changeEntity,
                            existEntity.getNextRunAt(now, changeEntity),
                            existEntity.getFailCount(changeEntity),
                            preservedState
                        )
                    }?.also { view ->
                        if (view.localState == LocalState.READY_LOCAL) {
                            heapAdd(EndpointRef(view.id, view.nextRunAt, view.dbVersion))
                        }
                    }
                }

                LOG.info("Refresh loop: Send signal to updated endpoints")
                nudge.trySend(Unit)
            }

            LOG.info("Synchronization for deleting endpoints in in-memory")
            registry.keys.removeIf { id -> !seen.contains(id) }

            LOG.info("registry size=${registry.size}, heap maybe grows (lazy), workQueue cap=${CONFIG.workQueueCapacity}")
            delay(CONFIG.refreshPeriod.toMillis())
        }
    }

    /**
     * Подает готовые эндпоинты в очередь работы.
     */
    private suspend fun feedLoop() {
        while (currentCoroutineContext().isActive) {
            val now = Instant.now(CLOCK)
            val endpointRef = heapPollIfDue(now)

            if (endpointRef == null) {
                val nextAt = heapPeekNextRunAt()
                val sleepMs = nextAt?.let { Duration.between(now, it).toMillis().coerceAtLeast(1) } ?: 60000L

                withTimeoutOrNull(sleepMs) { nudge.receive() }
                continue
            }

            val pushed = markInFlightIfActual(endpointRef, now)
            if (!pushed) continue

            val send = workQueue.trySend(endpointRef.id)
            if (!send.isSuccess) {
                registry.computeIfPresent(endpointRef.id) { _, v ->
                    if (v.localState == LocalState.IN_FLIGHT) v.copy(localState = LocalState.READY_LOCAL) else v
                }
                heapAdd(endpointRef)
                delay(10)
            }
        }
    }

    /**
     * Выполняет проверки эндпоинтов и отправляет результаты в outbox.
     */
    private suspend fun workerLoop() {
        while (currentCoroutineContext().isActive) {
            val id = workQueue.receive()
            registry[id] ?: continue

            val running = registry.computeIfPresent(id) { _, view ->
                when (view.localState) {
                    LocalState.IN_FLIGHT, LocalState.READY_LOCAL -> view.copy(localState = LocalState.RUNNING)
                    LocalState.RUNNING -> view
                }
            } ?: continue

            val now = Instant.now(CLOCK)

            try {
                val result = processor.check(running)
                outbox.send(result)

                val nextAt = now.plusSeconds(running.intervalSec.toLong())

                registry.computeIfPresent(id) { _, view ->
                    view.copy(
                        nextRunAt = nextAt,
                        failCount = 0,
                        localState = LocalState.READY_LOCAL
                    )
                }?.also { updated ->
                    heapAdd(EndpointRef(updated.id, updated.nextRunAt, updated.dbVersion))
                    nudge.trySend(Unit)
                }
            } catch (ex: Throwable) {
                val failCount = running.failCount + 1
                val backoffSec = (1L shl failCount).coerceAtMost(60L)
                val delaySec = minOf(backoffSec, running.intervalSec.toLong().coerceAtLeast(1))
                val nextAt = now.plusSeconds(delaySec)

                outbox.send(
                    EndpointBad(
                        id = running.id,
                        versionSnapshot = running.dbVersion,
                        checkedAt = now,
                        error = ex.message ?: "error"
                    )
                )

                registry.computeIfPresent(id) { _, view ->
                    view.copy(
                        nextRunAt = nextAt,
                        failCount = failCount,
                        localState = LocalState.READY_LOCAL
                    )
                }?.also { updated ->
                    heapAdd(EndpointRef(updated.id, updated.nextRunAt, updated.dbVersion))
                    nudge.trySend(Unit)
                }
            }
        }
    }

    /**
     * Собирает результаты в батчи и пишет их в БД.
     */
    private suspend fun flusherLoop() {
        while (currentCoroutineContext().isActive) {
            val firstOrNull = withTimeoutOrNull(CONFIG.flushPeriod.toMillis()) {
                outbox.receiveCatching().getOrNull()
            }

            if (firstOrNull == null) {
                if (outbox.isClosedForReceive) break
                continue
            }

            val batch = ArrayList<EndpointResult>(CONFIG.flushBatchSize)
            batch += firstOrNull

            while (batch.size < CONFIG.flushBatchSize) {
                val next = outbox.tryReceive().getOrNull() ?: break
                batch += next
            }

            flushBatch(batch)
        }
    }

    /**
     * Сохраняет батч результатов в БД.
     */
    private fun flushBatch(batch: List<EndpointResult>) {
        jdbcTemplate.batchUpdate(SQL_QUEUE, object : BatchPreparedStatementSetter {
            /**
             * Размер батча для вставки.
             */
            override fun getBatchSize(): Int = batch.size

            /**
             * Заполняет параметры SQL для элемента батча.
             */
            override fun setValues(ps: PreparedStatement, i: Int) {
                val result = batch[i]

                val finishedAt: Instant = result.checkedAt

                val latencyLong: Long = when (result) {
                    is EndpointOk -> result.latencyMs
                    is EndpointBad -> result.latencyMs ?: 0L
                }

                val latencyInt: Int = when {
                    latencyLong <= 0L -> 0
                    latencyLong >= Int.MAX_VALUE.toLong() -> Int.MAX_VALUE
                    else -> latencyLong.toInt()
                }

                val startedAt: Instant = finishedAt.minusMillis(latencyLong.coerceAtLeast(0L))

                ps.setObject(1, UUID.randomUUID())
                ps.setObject(2, result.id)
                ps.setTimestamp(3, Timestamp.from(startedAt))
                ps.setTimestamp(4, Timestamp.from(finishedAt))
                ps.setInt(5, latencyInt)

                when (result) {
                    is EndpointOk -> {
                        ps.setInt(6, result.httpStatus)
                        ps.setBoolean(7, true)
                        ps.setNull(8, Types.VARCHAR)
                        ps.setNull(9, Types.VARCHAR)
                    }

                    is EndpointBad -> {
                        if (result.httpStatus != null) ps.setInt(6, result.httpStatus) else ps.setNull(6, Types.INTEGER)
                        ps.setBoolean(7, false)

                        val errorType = if (result.httpStatus != null) "HTTP" else "EXCEPTION"
                        ps.setString(8, errorType)
                        ps.setString(9, result.error)
                    }
                }
            }
        })
    }

    /**
     * Удаление устаревших записей из бд
     */
    private suspend fun cleanDatabase() {
        while (currentCoroutineContext().isActive) {
            jpaAsyncIoWorker.executeWithTransactionalConsumer("Period clean data from database") {
                endpointRepository.deleteExpiredBatch()
                checkResultRepository.deleteExpiredBatch()
            }

            delay(CONFIG.cleanDataPeriod.toMillis())
        }
    }

    /**
     * Добавляет элемент в heap с блокировкой.
     */
    private suspend fun heapAdd(ref: EndpointRef) {
        heapMutex.withLock { readyHeap.add(ref) }
    }

    /**
     * Возвращает следующий элемент, если он уже должен выполниться.
     */
    private suspend fun heapPollIfDue(now: Instant): EndpointRef? =
        heapMutex.withLock {
            val top = readyHeap.peek() ?: return@withLock null
            if (top.nextRunAt <= now) readyHeap.poll() else null
        }

    /**
     * Возвращает ближайшее время запуска из heap.
     */
    private suspend fun heapPeekNextRunAt(): Instant? = heapMutex.withLock { readyHeap.peek()?.nextRunAt }

    /**
     * Переводит эндпоинт в состояние IN_FLIGHT, если ref актуален.
     */
    private fun markInFlightIfActual(ref: EndpointRef, now: Instant): Boolean {
        val updated = registry.computeIfPresent(ref.id) { _, view ->
            val ok =
                view.enabled &&
                        view.dbVersion == ref.versionSnapshot &&
                        view.nextRunAt <= now &&
                        view.localState == LocalState.READY_LOCAL

            if (ok) view.copy(localState = LocalState.IN_FLIGHT) else view
        } ?: return false

        return updated.localState == LocalState.IN_FLIGHT &&
                updated.dbVersion == ref.versionSnapshot &&
                updated.nextRunAt <= now
    }
}

/**
 * Возвращает актуальный счетчик ошибок для обновляемого эндпоинта.
 */
private fun EndpointView?.getFailCount(
    changeEntity: EndpointEntity
): Int = when {
    this == null -> 0
    this.dbVersion != changeEntity.version -> 0
    else -> this.failCount
}

/**
 * Вычисляет следующее время запуска с учетом версии записи.
 */
private fun EndpointView?.getNextRunAt(
    now: Instant,
    changeEntity: EndpointEntity
): Instant = when {
    this == null -> now
    this.dbVersion != changeEntity.version -> this.nextRunAt
    else -> this.nextRunAt
}

private val LOG by lazy { LoggerFactory.getLogger(InMemoryScheduler::class.java) }
private val CLOCK = Clock.systemUTC()
private val CONFIG = SchedulerConfig()

val SQL_QUEUE = """
        insert into check_results(
            id, endpoint_id, started_at, finished_at,
            latency_ms, status_code, success, error_type, error_message
        )
        values (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """.trimIndent()
