package com.acme.slamonitor.business.scheduler.impl

import com.acme.slamonitor.business.client.EndpointClient
import com.acme.slamonitor.business.client.dto.RuntimeRequest
import com.acme.slamonitor.business.scheduler.dto.EndpointBad
import com.acme.slamonitor.business.scheduler.dto.EndpointOk
import com.acme.slamonitor.business.scheduler.dto.EndpointView
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.every
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@DisplayName("EndpointProcessorImpl")
class EndpointProcessorImplTest {

    private val client = mockk<EndpointClient>()
    private val processor = EndpointProcessorImpl(client)

    @Test
    fun shouldReturnOkWhenStatusExpected() = runTest {
        val endpoint = EndpointView(
            id = UUID.randomUUID(),
            dbVersion = 1,
            name = "name",
            url = "https://example.com",
            method = "GET",
            headers = null,
            timeoutMs = 1000,
            expectedStatus = listOf(200),
            intervalSec = 30,
            enabled = true,
            nextRunAt = Instant.parse("2024-01-01T00:00:00Z"),
            failCount = 0,
            localState = com.acme.slamonitor.business.scheduler.dto.LocalState.READY_LOCAL
        )
        val response = mockk<HttpResponse>()
        every { response.status } returns HttpStatusCode.OK
        coEvery { client.call(any()) } returns response

        val result = processor.check(endpoint)

        assertTrue(result is EndpointOk)
        coVerify(exactly = 1) { client.call(any<RuntimeRequest>()) }
    }

    @Test
    fun shouldReturnBadWhenStatusUnexpected() = runTest {
        val endpoint = EndpointView(
            id = UUID.randomUUID(),
            dbVersion = 1,
            name = "name",
            url = "https://example.com",
            method = "GET",
            headers = null,
            timeoutMs = 1000,
            expectedStatus = listOf(200),
            intervalSec = 30,
            enabled = true,
            nextRunAt = Instant.parse("2024-01-01T00:00:00Z"),
            failCount = 0,
            localState = com.acme.slamonitor.business.scheduler.dto.LocalState.READY_LOCAL
        )
        val response = mockk<HttpResponse>()
        every { response.status } returns HttpStatusCode.InternalServerError
        coEvery { response.bodyAsText() } returns "error"
        coEvery { client.call(any()) } returns response

        val result = processor.check(endpoint)

        assertTrue(result is EndpointBad)
    }

    @Test
    fun shouldReturnBadWhenClientThrows() = runTest {
        val endpoint = EndpointView(
            id = UUID.randomUUID(),
            dbVersion = 1,
            name = "name",
            url = "https://example.com",
            method = "GET",
            headers = null,
            timeoutMs = 1000,
            expectedStatus = listOf(200),
            intervalSec = 30,
            enabled = true,
            nextRunAt = Instant.parse("2024-01-01T00:00:00Z"),
            failCount = 0,
            localState = com.acme.slamonitor.business.scheduler.dto.LocalState.READY_LOCAL
        )
        coEvery { client.call(any()) } throws RuntimeException("fail")

        val result = processor.check(endpoint)

        assertTrue(result is EndpointBad)
    }
}
