package com.acme.slamonitor.api.dto.response

data class StatsResponse(
    val sampleCount: Int,
    val p50: Double?,
    val p95: Double?,
    val p99: Double?,
    val avg: Double?,
    val min: Int?,
    val max: Int?,
    val errorRate: Double?,
    val lastStatus: Int?,
    val insufficientSamples: Boolean
)
