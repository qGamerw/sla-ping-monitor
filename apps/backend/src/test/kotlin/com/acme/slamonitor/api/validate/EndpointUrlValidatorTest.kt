package com.acme.slamonitor.api.validate

import com.acme.slamonitor.api.dto.request.EndpointRequest
import com.acme.slamonitor.api.validate.impl.EndpointUrlValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("EndpointUrlValidator")
class EndpointUrlValidatorTest {

    private val validator = EndpointUrlValidator()

    @Test
    fun shouldReturnErrorWhenUrlBlank() {
        val request = EndpointRequest(name = "test", url = " ")

        val errors = validator.validate(request)

        assertEquals(1, errors.size)
        assertEquals("URL_BLANK", errors.first().code)
    }

    @Test
    fun shouldReturnErrorWhenUrlInvalid() {
        val request = EndpointRequest(name = "test", url = "ht@tp")

        val errors = validator.validate(request)

        assertEquals(1, errors.size)
        assertEquals("URL_INVALID", errors.first().code)
    }

    @Test
    fun shouldReturnErrorWhenUrlNotAbsolute() {
        val request = EndpointRequest(name = "test", url = "/path")

        val errors = validator.validate(request)

        assertEquals(1, errors.size)
        assertEquals("URL_NOT_ABSOLUTE", errors.first().code)
    }

    @Test
    fun shouldReturnErrorsWhenSchemeNotAllowed() {
        val request = EndpointRequest(name = "test", url = "ftp://example.com")

        val errors = validator.validate(request)

        assertTrue(errors.any { it.code == "URL_SCHEME_NOT_ALLOWED" })
    }

    @Test
    fun shouldReturnErrorsWhenHostMissing() {
        val request = EndpointRequest(name = "test", url = "https://")

        val errors = validator.validate(request)

        assertTrue(errors.any { it.code == "URL_HOST_MISSING" })
    }

    @Test
    fun shouldReturnEmptyWhenUrlValid() {
        val request = EndpointRequest(name = "test", url = "https://example.com")

        val errors = validator.validate(request)

        assertTrue(errors.isEmpty())
    }
}
