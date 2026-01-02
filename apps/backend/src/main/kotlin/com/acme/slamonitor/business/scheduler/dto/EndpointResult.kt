package com.acme.slamonitor.business.scheduler.dto

import java.time.Instant
import java.util.UUID

/**
 * Маркер для результатов проверки эндпоинта.
 */
sealed interface EndpointResult {
    val id: UUID
    val versionSnapshot: Long
    val checkedAt: Instant
}
