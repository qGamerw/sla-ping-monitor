package com.acme.slamonitor.api.validate

import com.acme.slamonitor.api.dto.request.EndpointRequest
import com.acme.slamonitor.api.validate.impl.EndpointTimeoutValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("EndpointTimeoutValidator")
class EndpointTimeoutValidatorTest {

    private val validator = EndpointTimeoutValidator()

    @Test
    fun shouldReturnErrorWhenTimeoutOutOfRange() {
        val request = EndpointRequest(name = "test", url = "https://example.com", timeoutMs = 1)

        val errors = validator.validate(request)

        assertEquals(1, errors.size)
        assertEquals("TIMEOUT_OUT_OF_RANGE", errors.first().code)
    }

    @Test
    fun shouldReturnEmptyWhenTimeoutIsNull() {
        val request = EndpointRequest(name = "test", url = "https://example.com", timeoutMs = null)

        val errors = validator.validate(request)

        assertTrue(errors.isEmpty())
    }
}
