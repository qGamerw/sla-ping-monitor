package com.acme.slamonitor.api.dto

import java.time.Instant
import java.util.UUID

data class EndpointSummaryResponse(
    val id: UUID,
    val name: String,
    val url: String,
    val enabled: Boolean,
    val lastCheckAt: Instant?,
    val lastStatusCode: Int?,
    val lastSuccess: Boolean?,
    val windowStats: StatsResponse?
)
