package com.acme.slamonitor.api.dto.response

import java.time.Instant
import java.util.UUID

/**
 * DTO эндпоинта для ответа API.
 */
data class EndpointResponse(
    val id: UUID,
    val name: String,
    val url: String,
    val method: String,
    val headers: Map<String, String>?,
    val timeoutMs: Int,
    val expectedStatus: List<Int>,
    val intervalSec: Int,
    val enabled: Boolean,
    val tags: List<String>?,
    val nextRunAt: Instant?,
    val leaseOwner: String?,
    val leaseUntil: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
)
