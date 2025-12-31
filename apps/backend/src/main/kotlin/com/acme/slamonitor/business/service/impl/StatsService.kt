package com.acme.slamonitor.business.service.impl

import com.acme.slamonitor.api.dto.StatsResponse
import com.acme.slamonitor.persistence.CheckResultRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

@Service
class StatsService(
    private val checkResultRepository: CheckResultRepository
) {
    /** Возвращает статистику проверок endpoint за окно времени. */
    fun getStats(endpointId: UUID, windowSec: Long, minSamples: Int = 20): StatsResponse {
        val now = Instant.now()
        val from = now.minusSeconds(windowSec)
        val results = checkResultRepository.findByEndpointIdAndWindow(endpointId, from, now)
        return calculateStats(
            results.map { it.latencyMs },
            results.count { !it.success },
            results.lastOrNull()?.statusCode,
            minSamples
        )
    }

    /** Считает статистику по списку задержек и числу ошибок. */
    fun calculateStats(
        latencies: List<Int>,
        errorCount: Int,
        lastStatus: Int?,
        minSamples: Int
    ): StatsResponse {
        if (latencies.isEmpty()) {
            return StatsResponse(
                sampleCount = 0,
                p50 = null,
                p95 = null,
                p99 = null,
                avg = null,
                min = null,
                max = null,
                errorRate = null,
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
            sampleCount = sampleCount,
            p50 = p50,
            p95 = p95,
            p99 = p99,
            avg = avg,
            min = minValue,
            max = maxValue,
            errorRate = errorRate,
            lastStatus = lastStatus,
            insufficientSamples = insufficientSamples
        )
    }

    /** Вычисляет percentile из отсортированного списка. */
    private fun percentile(sorted: List<Int>, percentile: Double): Double {
        if (sorted.isEmpty()) {
            return 0.0
        }
        val index = ceil(percentile * sorted.size).toInt() - 1
        val bounded = max(0, min(index, sorted.size - 1))
        return sorted[bounded].toDouble()
    }
}
