package com.acme.slamonitor.business.service.impl

import com.acme.slamonitor.api.dto.response.EndpointSummaryResponse
import com.acme.slamonitor.api.dto.response.StatsResponse
import com.acme.slamonitor.business.service.StatsService
import com.acme.slamonitor.persistence.CheckResultRepository
import com.acme.slamonitor.persistence.EndpointRepository
import com.acme.slamonitor.persistence.mapper.CheckResultMapper.Companion.MAPPER
import java.time.Instant
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import org.springframework.stereotype.Service

@Service
class StatsServiceImpl(
    private val checkResultRepository: CheckResultRepository,
    private val endpointRepository: EndpointRepository
) : StatsService {

    override fun getStats(endpointId: UUID, windowSec: Long, minSamples: Int?): StatsResponse {
        val now = Instant.now()
        val from = now.minusSeconds(windowSec)
        val results = checkResultRepository.findByEndpointIdAndWindow(endpointId, from, now)
        return calculateStats(
            results.map { it.latencyMs },
            results.count { !it.success },
            results.lastOrNull()?.statusCode,
            minSamples!!
        )
    }

    override fun getChecks(endpointId: UUID, from: Instant, to: Instant) = MAPPER.toResponses(
        checkResultRepository.findByEndpointIdAndWindow(endpointId, from, to)
    )

    override fun getSummery(windowSec: Long): List<EndpointSummaryResponse> {
        val now = Instant.now()
        val from = now.minusSeconds(windowSec)
        val statsByEndpoint = checkResultRepository.findByWindow(from, now).groupBy { it.endpoint }

        return endpointRepository.findAll().map { endpoint ->
            val lastCheck = checkResultRepository.findTopByEndpoint_IdOrderByFinishedAtDesc(endpoint.id)
            EndpointSummaryResponse(
                id = endpoint.id,
                name = endpoint.name,
                url = endpoint.url,
                enabled = endpoint.enabled,
                lastCheckAt = lastCheck?.finishedAt,
                lastStatusCode = lastCheck?.statusCode,
                lastSuccess = lastCheck?.success,
                windowStats = calculateStats(
                    statsByEndpoint[endpoint]?.map { it.latencyMs } ?: emptyList(),
                    statsByEndpoint[endpoint]?.count { !it.success } ?: -1,
                    statsByEndpoint[endpoint]?.lastOrNull()?.statusCode ?: -1,
                    20
                )
            )
        }
    }

    private fun calculateStats(
        latencies: List<Int>,
        errorCount: Int,
        lastStatus: Int?,
        minSamples: Int
    ): StatsResponse {
        if (latencies.isEmpty()) {
            return StatsResponse(
                lastStatus = lastStatus,
                insufficientSamples = true
            )
        }

        val sorted = latencies.sorted()
        val sampleCount = sorted.size
        val avg = sorted.average()
        val minValue = sorted.first()
        val maxValue = sorted.last()
        val p50 = percentile(sorted, 0.50)
        val p95 = percentile(sorted, 0.95)
        val p99 = percentile(sorted, 0.99)
        val errorRate = errorCount.toDouble() / sampleCount.toDouble()
        val insufficientSamples = sampleCount < minSamples

        return StatsResponse(
            sampleCount, p50, p95, p99, avg, minValue, maxValue, errorRate, lastStatus, insufficientSamples
        )
    }
}

private fun percentile(sorted: List<Int>, percentile: Double): Double {
    if (sorted.isEmpty()) {
        return 0.0
    }
    val index = ceil(percentile * sorted.size).toInt() - 1
    val bounded = max(0, min(index, sorted.size - 1))
    return sorted[bounded].toDouble()
}
