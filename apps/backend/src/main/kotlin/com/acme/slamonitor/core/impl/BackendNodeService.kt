package com.acme.slamonitor.core.impl

import com.acme.slamonitor.api.dto.NodeHeartbeatRequest
import com.acme.slamonitor.persistence.BackendNodeRepository
import com.acme.slamonitor.persistence.domain.BackendNodeEntity
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class BackendNodeService(
    private val backendNodeRepository: BackendNodeRepository
) {
    /** Регистрирует или обновляет heartbeat backend-ноды. */
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

    /** Возвращает список всех backend-нод. */
    fun list(): List<BackendNodeEntity> = backendNodeRepository.findAll()
}
