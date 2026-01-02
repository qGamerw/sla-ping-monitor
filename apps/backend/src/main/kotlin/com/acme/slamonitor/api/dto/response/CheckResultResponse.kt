package com.acme.slamonitor.api.dto.response

import java.time.Instant
import java.util.UUID

/**
 * DTO результата проверки эндпоинта.
 */
data class CheckResultResponse(
    val id: UUID,
    val endpointId: UUID,
    val startedAt: Instant,
    val finishedAt: Instant,
    val latencyMs: Int,
    val statusCode: Int?,
    val success: Boolean,
    val errorType: String?,
    val errorMessage: String?
)
