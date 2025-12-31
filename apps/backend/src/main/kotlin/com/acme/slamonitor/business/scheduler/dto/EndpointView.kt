package com.acme.slamonitor.business.scheduler.dto

import java.time.Instant
import java.util.UUID

data class EndpointView(
    val id: UUID,
    val dbVersion: Long,

    val name: String,
    val url: String,
    val method: String,
    val headers: Map<String, String>?,
    val timeoutMs: Int,
    val expectedStatus: List<Int>,
    val intervalSec: Int,
    val enabled: Boolean,

    val nextRunAt: Instant,
    val failCount: Int,
    val localState: LocalState
)
