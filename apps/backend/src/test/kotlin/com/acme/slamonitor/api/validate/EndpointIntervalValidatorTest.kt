package com.acme.slamonitor.api.validate

import com.acme.slamonitor.api.dto.request.EndpointRequest
import com.acme.slamonitor.api.validate.impl.EndpointIntervalValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("EndpointIntervalValidator")
class EndpointIntervalValidatorTest {

    private val validator = EndpointIntervalValidator()

    @Test
    fun shouldReturnErrorWhenIntervalTooSmall() {
        val request = EndpointRequest(name = "test", url = "https://example.com", intervalSec = 1)

        val errors = validator.validate(request)

        assertEquals(1, errors.size)
        assertEquals("INTERVAL_TOO_SMALL", errors.first().code)
    }

    @Test
    fun shouldReturnEmptyWhenIntervalIsNull() {
        val request = EndpointRequest(name = "test", url = "https://example.com", intervalSec = null)

        val errors = validator.validate(request)

        assertTrue(errors.isEmpty())
    }
}
