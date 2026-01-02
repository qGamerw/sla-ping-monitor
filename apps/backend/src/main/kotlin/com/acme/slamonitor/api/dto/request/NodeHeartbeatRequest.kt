package com.acme.slamonitor.api.dto.request

import java.time.Instant

/**
 * Запрос с heartbeat-данными от backend-ноды.
 */
data class NodeHeartbeatRequest(
    val nodeId: String,
    val baseUrl: String,
    val startedAt: Instant,
    val meta: Map<String, Any>? = null
)
