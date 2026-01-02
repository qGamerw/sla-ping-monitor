package com.acme.slamonitor.business.service.impl

import com.acme.slamonitor.api.dto.request.NodeHeartbeatRequest
import com.acme.slamonitor.business.service.BackendNodeService
import com.acme.slamonitor.persistence.BackendNodeRepository
import com.acme.slamonitor.persistence.domain.BackendNodeEntity
import java.time.Instant

open class BackendNodeServiceImpl(
    private val backendNodeRepository: BackendNodeRepository
) : BackendNodeService {

    override fun heartbeat(request: NodeHeartbeatRequest): BackendNodeEntity {
        val now = Instant.now()
        val existing = backendNodeRepository.findById(request.nodeId).orElse(null)
        val entity = existing?.apply {
            baseUrl = request.baseUrl
            lastHeartbeatAt = now
            meta = request.meta
        } ?: BackendNodeEntity(
            nodeId = request.nodeId,
            baseUrl = request.baseUrl,
            startedAt = request.startedAt,
            lastHeartbeatAt = now,
            meta = request.meta
        )
        return backendNodeRepository.save(entity)
    }

    override fun getNodes(): List<BackendNodeEntity> = backendNodeRepository.findAll()
}
