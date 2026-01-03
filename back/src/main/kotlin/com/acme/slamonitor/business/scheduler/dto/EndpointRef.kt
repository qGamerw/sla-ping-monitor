package com.acme.slamonitor.business.scheduler.dto

import java.time.Instant
import java.util.UUID

/**
 * Ссылка на эндпоинт для очереди планировщика.
 */
internal data class EndpointRef(
    val id: UUID,
    val nextRunAt: Instant,
    val versionSnapshot: Long
)
