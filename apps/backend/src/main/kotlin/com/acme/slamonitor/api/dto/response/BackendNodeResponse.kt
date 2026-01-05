package com.acme.slamonitor.api.dto.response

/**
 * DTO backend-ноды для ответа API.
 */
data class BackendNodeResponse(
    val cpuUsed: String,
    val ramUsed: String,
)
