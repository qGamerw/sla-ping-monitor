package com.acme.slamonitor.api.dto.request

import java.time.Instant

data class NodeHeartbeatRequest(
    val nodeId: String,
    val baseUrl: String,
    val startedAt: Instant,
    val meta: Map<String, Any>? = null
)
