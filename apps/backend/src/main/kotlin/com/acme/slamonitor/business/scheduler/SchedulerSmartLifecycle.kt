package com.acme.slamonitor.business.scheduler

import com.acme.slamonitor.scope.AppScope
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import org.springframework.context.SmartLifecycle

/**
 * Управляет жизненным циклом планировщика через SmartLifecycle.
 */
open class SchedulerSmartLifecycle(
    private val scheduler: InMemoryScheduler,
    private val rootDispatcher: CoroutineDispatcher,
    private val refreshLoopDispatcher: CoroutineDispatcher,
    private val feedLoopDispatcher: CoroutineDispatcher,
    private val workerDispatcher: CoroutineDispatcher,
    private val flusherLoopDispatcher: CoroutineDispatcher,
    private val cleanDataLoopDispatcher: CoroutineDispatcher,
    private val appScope: AppScope
) : SmartLifecycle {

    private val running = AtomicBoolean(false)
    private var job: Job? = null

    /**
     * Запускает фоновые циклы планировщика.
     */
    override fun start() {
        if (!running.compareAndSet(false, true)) return

        job = appScope.launch(rootDispatcher) {
            scheduler.runLoops(
                refreshLoopDispatcher,
                feedLoopDispatcher,
                workerDispatcher,
                flusherLoopDispatcher,
                cleanDataLoopDispatcher
            )
        }
    }

    /**
     * Останавливает фоновые циклы планировщика.
     */
    override fun stop() {
        job?.cancel()
        running.set(false)
    }

    /**
     * Останавливает планировщик и уведомляет callback.
     */
    override fun stop(callback: Runnable) {
        stop()
        callback.run()
    }

    /**
     * Возвращает признак работы планировщика.
     */
    override fun isRunning(): Boolean = running.get()

    /**
     * Фаза запуска компонента.
     */
    override fun getPhase(): Int = 0
}
