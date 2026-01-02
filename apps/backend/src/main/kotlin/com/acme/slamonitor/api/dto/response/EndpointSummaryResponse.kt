package com.acme.slamonitor.api.dto.response

import java.time.Instant
import java.util.UUID

/**
 * Краткая информация по эндпоинту с последней статистикой.
 */
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
