package com.acme.slamonitor.business.scheduler

import com.acme.slamonitor.configuration.FEED_LOOP_THREAD_DISPATCHER_BEAN_NAME
import com.acme.slamonitor.configuration.FLUSHER_LOOP_THREAD_DISPATCHER_BEAN_NAME
import com.acme.slamonitor.configuration.REFRESH_LOOP_THREAD_DISPATCHER_BEAN_NAME
import com.acme.slamonitor.configuration.ROOT_THREAD_DISPATCHER_BEAN_NAME
import com.acme.slamonitor.configuration.WORKER_THREAD_DISPATCHER_BEAN_NAME
import com.acme.slamonitor.scope.AppScope
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.SmartLifecycle
import org.springframework.stereotype.Component

@Component
class SchedulerLifecycle(
    private val scheduler: InMemoryScheduler,
    @param:Qualifier(ROOT_THREAD_DISPATCHER_BEAN_NAME)
    private val rootDispatcher: CoroutineDispatcher,
    @param:Qualifier(REFRESH_LOOP_THREAD_DISPATCHER_BEAN_NAME)
    private val refreshLoopDispatcher: CoroutineDispatcher,
    @param:Qualifier(FEED_LOOP_THREAD_DISPATCHER_BEAN_NAME)
    private val reedLoopDispatcher: CoroutineDispatcher,
    @param:Qualifier(WORKER_THREAD_DISPATCHER_BEAN_NAME)
    private val workerDispatcher: CoroutineDispatcher,
    @param:Qualifier(FLUSHER_LOOP_THREAD_DISPATCHER_BEAN_NAME)
    private val flusherLoopDispatcher: CoroutineDispatcher,
    private val appScope: AppScope
) : SmartLifecycle {

    private val running = AtomicBoolean(false)
    private var job: Job? = null

    override fun start() {
        if (!running.compareAndSet(false, true)) return

        job = appScope.launch(rootDispatcher) {
            scheduler.runLoops(
                refreshLoopDispatcher,
                reedLoopDispatcher,
                workerDispatcher,
                flusherLoopDispatcher
            )
        }
    }

    override fun stop() {
        job?.cancel()
        running.set(false)
    }

    override fun stop(callback: Runnable) {
        stop()
        callback.run()
    }

    override fun isRunning(): Boolean = running.get()

    override fun getPhase(): Int = 0
}
