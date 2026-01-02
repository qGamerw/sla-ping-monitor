package com.acme.slamonitor.api.filter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import jakarta.servlet.FilterChain

@DisplayName("HttpLoggingFilter")
class HttpLoggingFilterTest {

    @Test
    fun shouldCopyResponseBodyAfterFiltering() {
        val request = MockHttpServletRequest("GET", "/api/test")
        val response = MockHttpServletResponse()
        val filter = HttpLoggingFilter()
        val chain = FilterChain { _, resp ->
            val http = resp as MockHttpServletResponse
            http.status = 200
            http.writer.write("ok")
        }

        filter.doFilter(request, response, chain)

        assertEquals(200, response.status)
        assertEquals("ok", response.contentAsString)
    }
}
