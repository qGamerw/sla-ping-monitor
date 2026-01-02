package com.acme.slamonitor.business.scheduler

import com.acme.slamonitor.scope.AppScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("SchedulerLifecycle")
class SchedulerLifecycleTest {

    @Test
    fun shouldStartAndStopScheduler() = runTest {
        val scheduler = mockk<InMemoryScheduler>()
        coEvery { scheduler.runLoops(any(), any(), any(), any()) } returns Unit
        val lifecycle = SchedulerLifecycle(
            scheduler = scheduler,
            rootDispatcher = Dispatchers.Unconfined,
            refreshLoopDispatcher = Dispatchers.Unconfined,
            feedLoopDispatcher = Dispatchers.Unconfined,
            workerDispatcher = Dispatchers.Unconfined,
            flusherLoopDispatcher = Dispatchers.Unconfined,
            appScope = AppScope()
        )

        lifecycle.start()

        assertTrue(lifecycle.isRunning)
        coVerify(exactly = 1) { scheduler.runLoops(any(), any(), any(), any()) }

        lifecycle.stop()

        assertTrue(!lifecycle.isRunning)
    }
}
