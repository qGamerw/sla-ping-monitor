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

/**
 * Реализация сервиса статистики на основе репозиториев.
 */
open class StatsServiceImpl(
    private val checkResultRepository: CheckResultRepository,
    private val endpointRepository: EndpointRepository
) : StatsService {

    /**
     * Возвращает статистику по эндпоинту за окно.
     */
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

    /**
     * Возвращает проверки эндпоинта за период.
     */
    override fun getChecks(endpointId: UUID, from: Instant, to: Instant) = MAPPER.toResponses(
        checkResultRepository.findByEndpointIdAndWindow(endpointId, from, to)
    )

    /**
     * Возвращает краткое резюме по всем эндпоинтам.
     */
    override fun getSummery(windowSec: Long): List<EndpointSummaryResponse> {
        val now = Instant.now()
        val from = now.minusSeconds(windowSec)
        val statsByEndpoint = checkResultRepository.findByWindow(from, now).groupBy { it.endpoint.id }

        return endpointRepository.findAllByIsDeletedFalse().map { endpoint ->
            val lastCheck = checkResultRepository.findTopByEndpointIdOrderByFinishedAtDesc(endpoint.id)
            EndpointSummaryResponse(
                id = endpoint.id,
                name = endpoint.name,
                url = endpoint.url,
                enabled = endpoint.enabled,
                lastCheckAt = lastCheck?.finishedAt,
                lastStatusCode = lastCheck?.statusCode,
                lastSuccess = lastCheck?.success,
                windowStats = calculateStats(
                    statsByEndpoint[endpoint.id]?.map { it.latencyMs } ?: emptyList(),
                    statsByEndpoint[endpoint.id]?.count { !it.success } ?: -1,
                    statsByEndpoint[endpoint.id]?.lastOrNull()?.statusCode ?: -1,
                    20
                )
            )
        }
    }

    /**
     * Собирает агрегированную статистику по списку латентностей.
     */
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

/**
 * Возвращает значение заданного перцентиля для отсортированного списка.
 */
private fun percentile(sorted: List<Int>, percentile: Double): Double {
    if (sorted.isEmpty()) {
        return 0.0
    }
    val index = ceil(percentile * sorted.size).toInt() - 1
    val bounded = max(0, min(index, sorted.size - 1))
    return sorted[bounded].toDouble()
}
