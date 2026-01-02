package com.acme.slamonitor.api.dto.response

data class StatsResponse(
    val sampleCount: Int = 0,
    val p50: Double? = null,
    val p95: Double? = null,
    val p99: Double? = null,
    val avg: Double? = null,
    val min: Int? = null,
    val max: Int? = null,
    val errorRate: Double? = null,
    val lastStatus: Int? = null,
    val insufficientSamples: Boolean = false
)
