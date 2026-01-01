package com.acme.slamonitor.business.scheduler

import com.acme.slamonitor.business.scheduler.dto.EndpointBad
import com.acme.slamonitor.business.scheduler.dto.EndpointRef
import com.acme.slamonitor.business.scheduler.dto.EndpointResult
import com.acme.slamonitor.business.scheduler.dto.EndpointView
import com.acme.slamonitor.business.scheduler.dto.LocalState
import com.acme.slamonitor.persistence.EndpointRepository
import com.acme.slamonitor.scope.JpaIoWorkerCoroutineDispatcher
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
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service

@Service
class InMemoryScheduler(
    private val repository: EndpointRepository,
    private val jdbc: JdbcTemplate,
    private val processor: EndpointProcessor,
    private val clock: Clock = Clock.systemUTC(),
    private val config: SchedulerConfig = SchedulerConfig(),
    private val jpaAsyncIoWorker: JpaIoWorkerCoroutineDispatcher
) {

    private val registry = ConcurrentHashMap<UUID, EndpointView>()

    private val heapMutex = Mutex()
    private val readyHeap = PriorityQueue<EndpointRef>(compareBy<EndpointRef> { it.nextRunAt }.thenBy { it.id })

    private val workQueue = Channel<UUID>(capacity = config.workQueueCapacity)
    private val outbox = Channel<EndpointResult>(capacity = config.outboxCapacity)

    private val nudge = Channel<Unit>(Channel.CONFLATED)

    suspend fun runLoops(
        refreshLoopDispatcher: CoroutineDispatcher,
        feedLoopDispatcher: CoroutineDispatcher,
        workerDispatcher: CoroutineDispatcher,
        flusherLoopDispatcher: CoroutineDispatcher
    ) = coroutineScope {
        launch(refreshLoopDispatcher) { refreshLoop() }
        launch(feedLoopDispatcher) { feedLoop() }
        repeat(config.workers) { launch(workerDispatcher) { workerLoop() } }
        launch(flusherLoopDispatcher) { flusherLoop() }
    }

    /**
     * Refresh-loop:
     * 1) meta scan (id/version/enabled/interval/updatedAt)
     * 2) changedIds -> load full entities
     * 3) upsert registry + push EndpointRef в heap (nextRunAt)
     * 4) cleanup: удалили из БД -> выбросить из registry (heap очистится лениво)
     */
    private suspend fun refreshLoop() {
        while (currentCoroutineContext().isActive) {
            val now = Instant.now(clock)

            val seen = HashSet<UUID>(4096)
            val changed = ArrayList<UUID>(1024)

            var page = 0
            while (true) {
                val metas = jpaAsyncIoWorker.executeWithTransactionalSupplier("Getting all endpoints") {
                    repository.findAllMeta(PageRequest.of(page, config.metaPageSize))
                }
                if (metas.isEmpty()) break

                for (m in metas) {
                    val id = m.getId()
                    seen += id

                    val local = registry[id]
                    val isChanged =
                        local == null ||
                                local.dbVersion != m.getVersion() ||
                                local.enabled != m.getEnabled() ||
                                local.intervalSec != m.getIntervalSec()

                    if (isChanged) changed += id
                }
                page++
            }

            if (changed.isNotEmpty()) {
                for (chunk in changed.chunked(500)) {
                    val entities = jpaAsyncIoWorker.executeWithTransactionalSupplier("Getting endpoint: $chunk") {
                        repository.findAllById(chunk)
                    }

                    for (e in entities) {
                        if (!e.enabled) {
                            registry.remove(e.id)
                            continue
                        }

                        registry.compute(e.id) { _, old ->
                            val preservedState = old?.localState
                                ?.takeIf { it == LocalState.RUNNING || it == LocalState.IN_FLIGHT }
                                ?: LocalState.READY_LOCAL

                            val nextRunAt = when {
                                old == null -> now                       // новый endpoint — запускаем сразу
                                old.dbVersion != e.version -> now        // изменили конфиг — тоже запускаем сразу
                                else -> old.nextRunAt                    // без изменений — сохраняем расписание
                            }

                            val failCount = when {
                                old == null -> 0
                                old.dbVersion != e.version -> 0          // при смене конфига сбрасываем
                                else -> old.failCount
                            }

                            EndpointView(
                                id = e.id,
                                dbVersion = e.version,

                                name = e.name,
                                url = e.url,
                                method = e.method,
                                headers = e.headers,
                                timeoutMs = e.timeoutMs,
                                expectedStatus = e.expectedStatus,
                                intervalSec = e.intervalSec,
                                enabled = e.enabled,

                                nextRunAt = nextRunAt,
                                failCount = failCount,
                                localState = preservedState
                            )
                        }?.also { view ->
                            if (view.localState == LocalState.READY_LOCAL) {
                                heapAdd(EndpointRef(view.id, view.nextRunAt, view.dbVersion))
                            }
                        }
                    }
                }

                nudge.trySend(Unit)
            }

            // cleanup удалённых из БД
            registry.keys.removeIf { id -> !seen.contains(id) }

            LOG.info("registry size=${registry.size}, heap maybe grows (lazy), workQueue cap=${config.workQueueCapacity}")

            delay(config.refreshPeriod.toMillis())
        }
    }

    /**
     * Feed-loop:
     * достаёт due из heap и кладёт в workQueue.
     * lazy deletion: ref.versionSnapshot должен совпасть с registry.dbVersion.
     */
    private suspend fun feedLoop() {
        while (currentCoroutineContext().isActive) {
            val now = Instant.now(clock)

            val ref = heapPollIfDue(now)
            if (ref == null) {
                val nextAt = heapPeekNextRunAt()
                val sleepMs = nextAt
                    ?.let { Duration.between(now, it).toMillis().coerceAtLeast(1) }
                    ?: 50L

                withTimeoutOrNull(sleepMs) { nudge.receive() }
                continue
            }

            val pushed = markInFlightIfActual(ref, now)
            if (!pushed) continue

            val send = workQueue.trySend(ref.id)
            if (!send.isSuccess) {
                registry.computeIfPresent(ref.id) { _, v ->
                    if (v.localState == LocalState.IN_FLIGHT) v.copy(localState = LocalState.READY_LOCAL) else v
                }
                heapAdd(ref)
                delay(10)
            }
        }
    }

    /**
     * Worker:
     * - берёт endpoint из registry
     * - выполняет check()
     * - планирует следующий nextRunAt (успех: intervalSec; ошибка: backoff + interval cap)
     * - пишет результат в outbox
     */
    private suspend fun workerLoop() {
        while (currentCoroutineContext().isActive) {
            val id = workQueue.receive()
            registry[id] ?: continue

            val running = registry.computeIfPresent(id) { _, v ->
                when (v.localState) {
                    LocalState.IN_FLIGHT, LocalState.READY_LOCAL -> v.copy(localState = LocalState.RUNNING)
                    LocalState.RUNNING -> v
                }
            } ?: continue

            val now = Instant.now(clock)

            try {
                val result = processor.check(running)
                outbox.send(result)

                val nextAt = now.plusSeconds(running.intervalSec.toLong())

                // планируем следующий запуск, сбрасываем failCount
                registry.computeIfPresent(id) { _, v ->
                    v.copy(
                        nextRunAt = nextAt,
                        failCount = 0,
                        localState = LocalState.READY_LOCAL
                    )
                }?.also { updated ->
                    heapAdd(EndpointRef(updated.id, updated.nextRunAt, updated.dbVersion))
                    nudge.trySend(Unit)
                }
            } catch (ex: Throwable) {
                val err = ex.message ?: ex::class.simpleName ?: "error"
                val failCount = running.failCount + 1

                // backoff: 2^failCount секунд, но не больше intervalSec (чтобы не “зависать” навечно)
                val backoffSec = (1L shl failCount).coerceAtMost(60L)
                val delaySec = minOf(backoffSec, running.intervalSec.toLong().coerceAtLeast(1))
                val nextAt = now.plusSeconds(delaySec)

                outbox.send(
                    EndpointBad(
                        id = running.id,
                        versionSnapshot = running.dbVersion,
                        checkedAt = now,
                        error = err
                    )
                )

                registry.computeIfPresent(id) { _, v ->
                    v.copy(
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
     * Flusher-loop:
     * пример — батч вставка результатов в endpoint_checks.
     * Можно заменить на обновление другой таблицы/метрик/логов.
     */
    private suspend fun flusherLoop() {
        while (currentCoroutineContext().isActive) {
            val batch = ArrayList<EndpointResult>(config.flushBatchSize)

            val first = withTimeoutOrNull(config.flushPeriod.toMillis()) { outbox.receive() } ?: continue
            batch += first

            while (batch.size < config.flushBatchSize) {
                val next = outbox.tryReceive().getOrNull() ?: break
                batch += next
            }

            flushBatch(batch)
        }
    }

    private suspend fun flushBatch(batch: List<EndpointResult>) {
//        val sql = """
//        insert into endpoint_checks(endpoint_id, checked_at, ok, http_status, latency_ms, error)
//        values (?, ?, ?, ?, ?, ?)
//    """.trimIndent()
//
//        jdbc.batchUpdate(sql, object : BatchPreparedStatementSetter {
//            override fun setValues(ps: PreparedStatement, i: Int) {
//                val r = batch[i]
//
//                ps.setObject(1, r.id) // UUID ок для Postgres
//
//                // Instant -> Timestamp (важно)
//                ps.setTimestamp(2, Timestamp.from(r.checkedAt))
//
//                when (r) {
//                    is EndpointOk -> {
//                        ps.setBoolean(3, true)
//                        ps.setInt(4, r.httpStatus)
//                        ps.setLong(5, r.latencyMs)
//                        ps.setNull(6, Types.VARCHAR)
//                    }
//                    is EndpointBad -> {
//                        ps.setBoolean(3, false)
//
//                        if (r.httpStatus != null) ps.setInt(4, r.httpStatus) else ps.setNull(4, Types.INTEGER)
//                        if (r.latencyMs != null) ps.setLong(5, r.latencyMs) else ps.setNull(5, Types.BIGINT)
//
//                        ps.setString(6, r.error)
//                    }
//                    else -> error("Unknown result type: ${r::class}")
//                }
//            }
//
//            override fun getBatchSize(): Int = batch.size
//        })
    }

    // ===== Heap helpers =====

    private suspend fun heapAdd(ref: EndpointRef) {
        heapMutex.withLock { readyHeap.add(ref) }
    }

    private suspend fun heapPollIfDue(now: Instant): EndpointRef? =
        heapMutex.withLock {
            val top = readyHeap.peek() ?: return@withLock null
            if (top.nextRunAt <= now) readyHeap.poll() else null
        }

    private suspend fun heapPeekNextRunAt(): Instant? =
        heapMutex.withLock { readyHeap.peek()?.nextRunAt }

    /**
     * lazy deletion + READY_LOCAL -> IN_FLIGHT
     */
    private fun markInFlightIfActual(ref: EndpointRef, now: Instant): Boolean {
        val updated = registry.computeIfPresent(ref.id) { _, v ->
            val ok =
                v.enabled &&
                        v.dbVersion == ref.versionSnapshot &&
                        v.nextRunAt <= now &&
                        v.localState == LocalState.READY_LOCAL

            if (ok) v.copy(localState = LocalState.IN_FLIGHT) else v
        } ?: return false

        return updated.localState == LocalState.IN_FLIGHT &&
                updated.dbVersion == ref.versionSnapshot &&
                updated.nextRunAt <= now
    }
}

private val LOG by lazy { LoggerFactory.getLogger(InMemoryScheduler::class.java) }
