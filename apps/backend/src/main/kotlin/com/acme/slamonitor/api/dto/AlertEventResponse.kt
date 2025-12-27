package com.acme.slamonitor.api.dto

import java.time.Instant
import java.util.UUID

data class AlertEventResponse(
    val id: UUID,
    val ruleId: UUID,
    val endpointId: UUID,
    val state: String,
    val openedAt: Instant,
    val resolvedAt: Instant?,
    val lastNotifiedAt: Instant?,
    val details: Map<String, Any>?
)
