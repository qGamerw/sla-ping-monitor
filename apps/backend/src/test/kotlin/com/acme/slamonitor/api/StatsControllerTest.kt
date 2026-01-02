package com.acme.slamonitor.api

import com.acme.slamonitor.api.dto.response.StatsResponse
import com.acme.slamonitor.business.service.StatsService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

@DisplayName("StatsController")
class StatsControllerTest {

    private val statsService = mockk<StatsService>()
    private val controller = StatsController(statsService)

    @Test
    fun shouldReturnStatsResponse() {
        val endpointId = UUID.randomUUID()
        val stats = StatsResponse(sampleCount = 1)
        every { statsService.getStats(endpointId, 60) } returns stats

        val response = controller.stats(endpointId, 60)

        assertEquals(stats, response.body)
    }

    @Test
    fun shouldReturnChecksResponse() {
        val endpointId = UUID.randomUUID()
        val from = Instant.parse("2024-01-01T00:00:00Z")
        val to = Instant.parse("2024-01-01T00:10:00Z")
        every { statsService.getChecks(endpointId, from, to) } returns emptyList()

        val response = controller.checks(endpointId, from, to)

        assertEquals(0, response.body?.size)
    }

    @Test
    fun shouldReturnSummaryResponse() {
        every { statsService.getSummery(120) } returns emptyList()

        val response = controller.summary(120)

        assertEquals(0, response.body?.size)
    }
}
