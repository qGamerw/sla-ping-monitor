package com.acme.slamonitor.business.scheduler.dto

import java.time.Instant
import java.util.UUID

/**
 * Результат успешной проверки.
 */
data class EndpointOk(
    override val id: UUID,
    override val versionSnapshot: Long,
    override val checkedAt: Instant,
    val httpStatus: Int,
    val latencyMs: Long
) : EndpointResult
