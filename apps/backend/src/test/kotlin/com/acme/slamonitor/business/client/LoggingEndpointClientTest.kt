package com.acme.slamonitor.business.client

import com.acme.slamonitor.business.client.dto.RuntimeRequest
import com.acme.slamonitor.business.client.impl.LoggingEndpointClient
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("LoggingEndpointClient")
class LoggingEndpointClientTest {

    @Test
    fun shouldDelegateCallToUnderlyingClient() = runTest {
        val delegate = mockk<EndpointClient>()
        val response = mockk<HttpResponse>()
        coEvery { response.status } returns HttpStatusCode.OK
        coEvery { delegate.call(any()) } returns response
        val client = LoggingEndpointClient(delegate)
        val request = RuntimeRequest(url = "https://example.com", method = HttpMethod.Get)

        val result = client.call(request)

        assertEquals(response, result)
    }
}
