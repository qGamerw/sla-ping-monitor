package com.acme.slamonitor.api.dto

import java.time.Instant
import java.util.UUID

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
