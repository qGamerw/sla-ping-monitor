package com.acme.slamonitor.business.scheduler

import com.acme.slamonitor.business.publisher.dto.EndpointAlerted
import com.acme.slamonitor.business.scheduler.dto.Job
import com.acme.slamonitor.persistence.EndpointRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

class EndpointScheduler(
    private val repo: EndpointRepository,
    private val updates: ReceiveChannel<EndpointAlerted>, // updateEndpoints
    private val jobs: SendChannel<Job>,                // in-memory очередь задач
    private val tickMs: Long = 1_000,
    private val reconcileMs: Long = 60_000,
    private val clockMs: () -> Long = { System.currentTimeMillis() }
) {
    private enum class State { setUp, run }
    private var state: State = State.setUp

    // Источник истины в памяти (активные runtime-endpoints)
    private val activeById = HashMap<String, RuntimeEndpoint>()

    // currentEndpoints: round-robin очередь ID
    private val currentEndpoints = ArrayDeque<String>()

    // Чтобы не закидывать один и тот же id в очередь бесконечно при частых UPSERT
    private val enqueuedIds = HashSet<String>()

    fun start(scope: CoroutineScope): Job = scope.launch(Dispatchers.Default) {
        setup()

        var nextTickAt = clockMs() + tickMs
        var nextReconcileAt = clockMs() + reconcileMs

        while (isActive) {
            val now = clockMs()
            val timeoutMs = minOf(
                (nextTickAt - now).coerceAtLeast(0),
                (nextReconcileAt - now).coerceAtLeast(0)
            )

            select<Unit> {
                // События приходят быстро — применяем сразу, не ждём тика
                updates.onReceiveCatching { res ->
                    val ev = res.getOrNull() ?: return@onReceiveCatching
                    applyEvent(ev)

                    // Дренаж "пачки" событий без лишнего select
                    drainUpdates()
                }

                onTimeout(timeoutMs) {
                    val t = clockMs()

                    if (t >= nextReconcileAt) {
                        reconcile()
                        nextReconcileAt = t + reconcileMs
                    }
                    if (t >= nextTickAt) {
                        tick()
                        nextTickAt = t + tickMs
                    }
                }
            }
        }
    }

    private suspend fun setup() {
        state = State.setUp
        val all = repo.findAll()

        // всё проходит единым путём: как будто пришли UPSERT события
        val now = clockMs()
        for (ep in all) {
            applyEvent(EndpointEvent.Upsert(ep.id, ep.url, ep.intervalMs), now)
        }

        state = State.run
    }

    private suspend fun drainUpdates() {
        while (true) {
            val ev = updates.tryReceive().getOrNull() ?: break
            applyEvent(ev)
        }
    }

    private suspend fun applyEvent(ev: EndpointEvent, now: Long = clockMs()) {
        check(state == State.run || state == State.setUp) { "Invalid state: $state" }

        when (ev) {
            is EndpointEvent.Upsert -> {
                val rt = RuntimeEndpoint(
                    id = ev.id,
                    url = ev.url,
                    intervalMs = ev.intervalMs,
                    nextRunAtMs = now // можно стартовать сразу
                )
                activeById[ev.id] = rt

                // Добавляем id в round-robin очередь один раз
                if (enqueuedIds.add(ev.id)) {
                    currentEndpoints.addLast(ev.id)
                }
            }

            is EndpointEvent.Delete -> {
                activeById.remove(ev.id)
                // id может ещё лежать в currentEndpoints — это ок:
                // он "вымоется", когда попадётся и мы не вернём его обратно
                enqueuedIds.remove(ev.id)
            }
        }
    }

    private suspend fun tick() {
        // За тик берём один endpoint (или можно брать k штук)
        val id = currentEndpoints.removeFirstOrNull() ?: return
        val rt = activeById[id]

        if (rt == null) {
            // endpoint удалён/отключён -> не возвращаем обратно => вымывание
            return
        }

        val now = clockMs()
        if (now >= rt.nextRunAtMs) {
            // Положить job в in-memory очередь
            // Лучше bounded Channel, чтобы не улететь в память:
            val result = jobs.trySend(Job(rt.id, rt.url))
            if (result.isSuccess) {
                rt.nextRunAtMs = now + rt.intervalMs
            } else {
                // Очередь задач переполнена — можно:
                // 1) дропнуть, 2) залогировать, 3) откатить nextRunAtMs назад и попробовать позже
                // Здесь: просто попробуем позже (не двигаем nextRunAtMs)
            }
        }

        // Возвращаем id в конец очереди (round-robin)
        currentEndpoints.addLast(id)
        enqueuedIds.add(id) // на случай, если кто-то удалил из set при Delete и потом UPSERT вернул
    }

    private suspend fun reconcile() {
        val dbIds = repo.findAllEnabledIds()

        // Удалённые из БД
        val removed = activeById.keys - dbIds
        for (id in removed) {
            activeById.remove(id)
            enqueuedIds.remove(id)
        }

        // Пропущенные/новые (если события потерялись)
        val missed = dbIds - activeById.keys
        if (missed.isNotEmpty()) {
            // Можно подтянуть целиком (дороже) или отдельными запросами.
            // Для простоты: тянем все и апдейтим — зависит от твоего репо.
            val all = repo.findAll().associateBy { it.id }
            for (id in missed) {
                val ep = all[id] ?: continue
                applyEvent(EndpointEvent.Upsert(ep.id, ep.url, ep.intervalMs))
            }
        }
    }
}
