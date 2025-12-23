package com.vogu.slapingmonitor.service

import com.vogu.slapingmonitor.api.NodeHeartbeatRequest
import com.vogu.slapingmonitor.domain.BackendNodeEntity
import com.vogu.slapingmonitor.repository.BackendNodeRepository
import java.time.Instant
import org.springframework.stereotype.Service

@Service
class BackendNodeService(
    private val backendNodeRepository: BackendNodeRepository
) {
    fun heartbeat(request: NodeHeartbeatRequest): BackendNodeEntity {
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

    fun list(): List<BackendNodeEntity> = backendNodeRepository.findAll()
}
