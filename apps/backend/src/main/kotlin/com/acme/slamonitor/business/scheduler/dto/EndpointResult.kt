package com.acme.slamonitor.business.scheduler.dto

import java.time.Instant
import java.util.UUID

sealed interface EndpointResult {
    val id: UUID
    val versionSnapshot: Long
    val checkedAt: Instant
}
