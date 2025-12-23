package com.vogu.slapingmonitor.api

import com.vogu.slapingmonitor.domain.AlertEventEntity
import com.vogu.slapingmonitor.domain.AlertRuleEntity
import com.vogu.slapingmonitor.domain.BackendNodeEntity
import com.vogu.slapingmonitor.domain.CheckResultEntity
import com.vogu.slapingmonitor.domain.EndpointEntity

fun EndpointEntity.toResponse(): EndpointResponse = EndpointResponse(
    id = id,
    name = name,
    url = url,
    method = method,
    headers = headers,
    timeoutMs = timeoutMs,
    expectedStatus = expectedStatus,
    intervalSec = intervalSec,
    enabled = enabled,
    tags = tags,
    nextRunAt = nextRunAt,
    leaseOwner = leaseOwner,
    leaseUntil = leaseUntil,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CheckResultEntity.toResponse(): CheckResultResponse = CheckResultResponse(
    id = id,
    endpointId = endpoint.id,
    startedAt = startedAt,
    finishedAt = finishedAt,
    latencyMs = latencyMs,
    statusCode = statusCode,
    success = success,
    errorType = errorType,
    errorMessage = errorMessage
)

fun AlertRuleEntity.toResponse(): AlertRuleResponse = AlertRuleResponse(
    id = id,
    endpointId = endpoint.id,
    type = type,
    threshold = threshold,
    windowSec = windowSec,
    triggerForSec = triggerForSec,
    cooldownSec = cooldownSec,
    hysteresisRatio = hysteresisRatio,
    enabled = enabled
)

fun AlertEventEntity.toResponse(): AlertEventResponse = AlertEventResponse(
    id = id,
    ruleId = rule.id,
    endpointId = endpoint.id,
    state = state,
    openedAt = openedAt,
    resolvedAt = resolvedAt,
    lastNotifiedAt = lastNotifiedAt,
    details = details
)

fun BackendNodeEntity.toResponse(): BackendNodeResponse = BackendNodeResponse(
    nodeId = nodeId,
    baseUrl = baseUrl,
    startedAt = startedAt,
    lastHeartbeatAt = lastHeartbeatAt,
    meta = meta
)
