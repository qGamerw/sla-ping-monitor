package com.acme.slamonitor.business.scheduler.dto

import com.acme.slamonitor.persistence.domain.EndpointEntity
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
) {
    constructor(entity: EndpointEntity, nextRunAt: Instant, failCount: Int, preservedState: LocalState) : this(
        id = entity.id,
        dbVersion = entity.version,
        name = entity.name,
        url = entity.url,
        method = entity.method,
        headers = entity.headers,
        timeoutMs = entity.timeoutMs,
        expectedStatus = entity.expectedStatus,
        intervalSec = entity.intervalSec,
        enabled = entity.enabled,

        nextRunAt = nextRunAt,
        failCount = failCount,
        localState = preservedState
    )
}
