package com.acme.slamonitor.business.service.impl

import com.acme.slamonitor.persistence.CheckResultRepository
import com.acme.slamonitor.persistence.EndpointRepository
import com.acme.slamonitor.persistence.domain.CheckResultEntity
import com.acme.slamonitor.persistence.domain.EndpointEntity
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@DisplayName("StatsServiceImpl")
class StatsServiceImplTest {

    private val checkResultRepository = mockk<CheckResultRepository>()
    private val endpointRepository = mockk<EndpointRepository>()
    private val service = StatsServiceImpl(checkResultRepository, endpointRepository)

    @Test
    fun shouldReturnStatsWhenResultsExist() {
        val endpointId = UUID.randomUUID()
        val endpoint = EndpointEntity(
            id = endpointId,
            name = "name",
            url = "https://example.com",
            method = "GET",
            timeoutMs = 1000,
            expectedStatus = listOf(200),
            intervalSec = 30,
            enabled = true
        )
        val results = listOf(
            CheckResultEntity(
                endpoint = endpoint,
                startedAt = Instant.parse("2024-01-01T00:00:00Z"),
                finishedAt = Instant.parse("2024-01-01T00:00:01Z"),
                latencyMs = 100,
                statusCode = 200,
                success = true
            ),
            CheckResultEntity(
                endpoint = endpoint,
                startedAt = Instant.parse("2024-01-01T00:01:00Z"),
                finishedAt = Instant.parse("2024-01-01T00:01:02Z"),
                latencyMs = 200,
                statusCode = 200,
                success = true
            ),
            CheckResultEntity(
                endpoint = endpoint,
                startedAt = Instant.parse("2024-01-01T00:02:00Z"),
                finishedAt = Instant.parse("2024-01-01T00:02:01Z"),
                latencyMs = 300,
                statusCode = 500,
                success = false
            )
        )

        every { checkResultRepository.findByEndpointIdAndWindow(eq(endpointId), any(), any()) } returns results

        val stats = service.getStats(endpointId, windowSec = 60, minSamples = 2)

        assertEquals(3, stats.sampleCount)
        assertEquals(100, stats.min)
        assertEquals(300, stats.max)
        assertEquals(200.0, stats.p50, 0.0001)
        assertEquals(1.0 / 3.0, stats.errorRate, 0.0001)
        assertEquals(500, stats.lastStatus)
        assertTrue(!stats.insufficientSamples)
    }

    @Test
    fun shouldReturnSummaryForEachEndpoint() {
        val endpointId = UUID.randomUUID()
        val endpoint = EndpointEntity(
            id = endpointId,
            name = "name",
            url = "https://example.com",
            method = "GET",
            timeoutMs = 1000,
            expectedStatus = listOf(200),
            intervalSec = 30,
            enabled = true
        )
        val lastCheck = CheckResultEntity(
            endpoint = endpoint,
            startedAt = Instant.parse("2024-01-01T00:00:00Z"),
            finishedAt = Instant.parse("2024-01-01T00:00:01Z"),
            latencyMs = 120,
            statusCode = 200,
            success = true
        )

        every { endpointRepository.findAll() } returns listOf(endpoint)
        every { checkResultRepository.findByWindow(any(), any()) } returns listOf(lastCheck)
        every { checkResultRepository.findTopByEndpoint_IdOrderByFinishedAtDesc(endpointId) } returns lastCheck

        val result = service.getSummery(windowSec = 60)

        assertEquals(1, result.size)
        val summary = result.first()
        assertEquals(endpointId, summary.id)
        assertEquals("name", summary.name)
        assertEquals(200, summary.lastStatusCode)
        assertEquals(true, summary.lastSuccess)
    }
}
