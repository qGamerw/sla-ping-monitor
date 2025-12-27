package com.acme.slamonitor.api.dto

import java.time.Instant

data class NodeHeartbeatRequest(
    val nodeId: String,
    val baseUrl: String,
    val startedAt: Instant,
    val meta: Map<String, Any>? = null
)
