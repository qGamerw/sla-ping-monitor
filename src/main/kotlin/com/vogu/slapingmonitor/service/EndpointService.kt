package com.vogu.slapingmonitor.service

import com.vogu.slapingmonitor.api.DEFAULT_EXPECTED_STATUS
import com.vogu.slapingmonitor.api.DEFAULT_INTERVAL_SEC
import com.vogu.slapingmonitor.api.DEFAULT_TIMEOUT_MS
import com.vogu.slapingmonitor.api.EndpointRequest
import com.vogu.slapingmonitor.domain.EndpointEntity
import com.vogu.slapingmonitor.repository.EndpointRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class EndpointService(
    private val endpointRepository: EndpointRepository
) {
    fun create(request: EndpointRequest): EndpointEntity {
        val entity = EndpointEntity(
            name = request.name,
            url = request.url,
            method = request.method ?: "GET",
            headers = request.headers,
            timeoutMs = request.timeoutMs ?: DEFAULT_TIMEOUT_MS,
            expectedStatus = request.expectedStatus ?: DEFAULT_EXPECTED_STATUS,
            intervalSec = request.intervalSec ?: DEFAULT_INTERVAL_SEC,
            enabled = request.enabled ?: true,
            tags = request.tags,
            nextRunAt = Instant.now().plusSeconds((request.intervalSec ?: DEFAULT_INTERVAL_SEC).toLong())
        )
        return endpointRepository.save(entity)
    }

    fun update(id: UUID, request: EndpointRequest): EndpointEntity {
        val entity = get(id)
        entity.name = request.name
        entity.url = request.url
        entity.method = request.method ?: entity.method
        entity.headers = request.headers
        entity.timeoutMs = request.timeoutMs ?: entity.timeoutMs
        entity.expectedStatus = request.expectedStatus ?: entity.expectedStatus
        entity.intervalSec = request.intervalSec ?: entity.intervalSec
        entity.enabled = request.enabled ?: entity.enabled
        entity.tags = request.tags
        return endpointRepository.save(entity)
    }

    fun get(id: UUID): EndpointEntity = endpointRepository.findById(id)
        .orElseThrow { IllegalArgumentException("Endpoint $id not found") }

    fun list(): List<EndpointEntity> = endpointRepository.findAll()

    fun delete(id: UUID) {
        endpointRepository.deleteById(id)
    }
}
