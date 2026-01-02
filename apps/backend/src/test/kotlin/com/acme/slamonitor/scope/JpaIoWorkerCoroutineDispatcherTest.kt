package com.acme.slamonitor.scope

import com.acme.slamonitor.persistence.util.JpaIoTransactionalLogging
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("JpaIoWorkerCoroutineDispatcher")
class JpaIoWorkerCoroutineDispatcherTest {

    @Test
    fun shouldExecuteConsumerWithinTransactionalLogging() = runTest {
        val logging = mockk<JpaIoTransactionalLogging>()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val appScope = AppScope()
        val worker = JpaIoWorkerCoroutineDispatcher(appScope, dispatcher, logging)
        var executed = false

        every { logging.runConsumer(any(), any()) } answers {
            secondArg<() -> Unit>().invoke()
        }

        worker.executeWithTransactionalConsumer("log") { executed = true }

        assertEquals(true, executed)
        verify(exactly = 1) { logging.runConsumer("log", any()) }
        appScope.close()
    }

    @Test
    fun shouldExecuteSupplierWithinTransactionalLogging() = runTest {
        val logging = mockk<JpaIoTransactionalLogging>()
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val appScope = AppScope()
        val worker = JpaIoWorkerCoroutineDispatcher(appScope, dispatcher, logging)

        every { logging.runSupplier(any(), any<() -> String>()) } answers {
            secondArg<() -> String>().invoke()
        }

        val result = worker.executeWithTransactionalSupplier("log") { "ok" }

        assertEquals("ok", result)
        verify(exactly = 1) { logging.runSupplier("log", any()) }
        appScope.close()
    }
}
