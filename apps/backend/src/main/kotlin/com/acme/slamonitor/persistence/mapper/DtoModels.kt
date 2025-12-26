package com.acme.slamonitor.persistence.mapper

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.Instant
import java.util.UUID

const val DEFAULT_TIMEOUT_MS = 3000
const val DEFAULT_INTERVAL_SEC = 60
val DEFAULT_EXPECTED_STATUS = listOf(200, 399)


data class EndpointRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    @field:Pattern(regexp = "https?://.+")
    val url: String,
    val method: String? = "GET",
    val headers: Map<String, String>? = null,
    @field:Min(100)
    @field:Max(30000)
    val timeoutMs: Int? = DEFAULT_TIMEOUT_MS,
    val expectedStatus: List<Int>? = DEFAULT_EXPECTED_STATUS,
    @field:Min(5)
    val intervalSec: Int? = DEFAULT_INTERVAL_SEC,
    val enabled: Boolean? = true,
    val tags: List<String>? = null
)

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

data class EndpointSummaryResponse(
    val id: UUID,
    val name: String,
    val url: String,
    val enabled: Boolean,
    val lastCheckAt: Instant?,
    val lastStatusCode: Int?,
    val lastSuccess: Boolean?,
    val windowStats: StatsResponse?
)

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

data class StatsResponse(
    val sampleCount: Int,
    val p50: Double?,
    val p95: Double?,
    val p99: Double?,
    val avg: Double?,
    val min: Int?,
    val max: Int?,
    val errorRate: Double?,
    val lastStatus: Int?,
    val insufficientSamples: Boolean
)

data class AlertRuleRequest(
    @field:NotNull
    val endpointId: UUID,
    @field:NotBlank
    val type: String,
    @field:Min(0)
    val threshold: Double,
    @field:Min(60)
    val windowSec: Int,
    @field:Min(0)
    val triggerForSec: Int,
    @field:Min(0)
    val cooldownSec: Int,
    @field:Min(0)
    val hysteresisRatio: Double,
    val enabled: Boolean? = true
)

data class AlertRuleResponse(
    val id: UUID,
    val endpointId: UUID,
    val type: String,
    val threshold: Double,
    val windowSec: Int,
    val triggerForSec: Int,
    val cooldownSec: Int,
    val hysteresisRatio: Double,
    val enabled: Boolean
)

data class AlertEventRequest(
    @field:NotNull
    val ruleId: UUID,
    @field:NotNull
    val endpointId: UUID,
    @field:NotBlank
    val state: String,
    @field:NotNull
    val openedAt: Instant,
    val resolvedAt: Instant? = null,
    val lastNotifiedAt: Instant? = null,
    val details: Map<String, Any>? = null
)

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

data class NodeHeartbeatRequest(
    @field:NotBlank
    val nodeId: String,
    @field:NotBlank
    val baseUrl: String,
    @field:NotNull
    val startedAt: Instant,
    val meta: Map<String, Any>? = null
)

data class BackendNodeResponse(
    val nodeId: String,
    val baseUrl: String,
    val startedAt: Instant,
    val lastHeartbeatAt: Instant,
    val meta: Map<String, Any>?
)
