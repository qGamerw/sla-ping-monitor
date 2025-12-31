package com.acme.slamonitor.business.scheduler.dto

import java.time.Instant
import java.util.UUID

internal data class EndpointRef(
    val id: UUID,
    val nextRunAt: Instant,
    val versionSnapshot: Long
)
