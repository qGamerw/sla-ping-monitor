package com.slapingmonitor.service

import com.slapingmonitor.repository.domain.BackendNodeEntity
import com.slapingmonitor.repository.mapper.NodeHeartbeatRequest
import java.time.Instant
import org.springframework.stereotype.Service

@Service
class BackendNodeService(
    private val backendNodeRepository: com.slapingmonitor.repository.BackendNodeRepository
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
