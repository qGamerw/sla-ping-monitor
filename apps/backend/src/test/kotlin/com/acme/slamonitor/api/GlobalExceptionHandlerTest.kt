package com.acme.slamonitor.api

import com.acme.slamonitor.exception.ValidationExecution
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import jakarta.servlet.http.HttpServletRequest

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private val handler = GlobalExceptionHandler()

    @Test
    fun shouldReturnInternalServerErrorForAnyException() {
        val request = mockk<HttpServletRequest>()
        every { request.method } returns "GET"
        every { request.requestURI } returns "/api/test"

        val response = handler.handleAny(RuntimeException("boom"), request)

        assertEquals(500, response.statusCode.value())
        assertEquals("boom", response.body?.error?.message)
    }

    @Test
    fun shouldReturnBadRequestForValidationError() {
        val request = mockk<HttpServletRequest>()
        every { request.method } returns "POST"
        every { request.requestURI } returns "/api/test"

        val exception = ValidationExecution(emptyList())
        val response = handler.handleValidation(exception, request)

        assertEquals(400, response.statusCode.value())
        assertEquals(exception.error, response.body?.error?.message)
    }
}
