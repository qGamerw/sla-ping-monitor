package com.acme.slamonitor.api.dto

import java.time.Instant

data class BackendNodeResponse(
    val nodeId: String,
    val baseUrl: String,
    val startedAt: Instant,
    val lastHeartbeatAt: Instant,
    val meta: Map<String, Any>?
)
