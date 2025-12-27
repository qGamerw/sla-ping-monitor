package com.acme.slamonitor.api.dto

import java.time.Instant
import java.util.UUID

data class AlertEventRequest(
    val ruleId: UUID,
    val endpointId: UUID,
    val state: String,
    val openedAt: Instant,
    val resolvedAt: Instant? = null,
    val lastNotifiedAt: Instant? = null,
    val details: Map<String, Any>? = null
)
