package com.acme.slamonitor.business.scheduler.dto

import java.time.Instant
import java.util.UUID

/**
 * Результат проверки с ошибкой.
 */
data class EndpointBad(
    override val id: UUID,
    override val versionSnapshot: Long,
    override val checkedAt: Instant,
    val error: String,
    val latencyMs: Long? = null,
    val httpStatus: Int? = null
) : EndpointResult
