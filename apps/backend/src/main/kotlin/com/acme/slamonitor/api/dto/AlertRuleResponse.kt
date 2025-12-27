package com.acme.slamonitor.api.dto

import java.util.UUID

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
