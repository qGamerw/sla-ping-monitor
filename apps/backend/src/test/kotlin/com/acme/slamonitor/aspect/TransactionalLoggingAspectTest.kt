package com.acme.slamonitor.aspect

import com.acme.slamonitor.aspect.annotation.TransactionalLogging
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("TransactionalLoggingAspect")
class TransactionalLoggingAspectTest {

    private val aspect = TransactionalLoggingAspect()

    @Test
    fun shouldProceedWhenAnnotationDisabled() {
        val pjp = mockk<ProceedingJoinPoint>()
        val annotation = TransactionalLogging(enabled = false)
        every { pjp.args } returns arrayOf("log")
        every { pjp.proceed() } returns "ok"

        val result = aspect.around(pjp, annotation)

        assertEquals("ok", result)
        verify(exactly = 1) { pjp.proceed() }
    }
}
