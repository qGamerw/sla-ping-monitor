package com.acme.slamonitor.business.service

import com.acme.slamonitor.api.dto.request.NodeHeartbeatRequest
import com.acme.slamonitor.persistence.domain.BackendNodeEntity

/**
 * Сервис управления backend-нодами.
 */
interface BackendNodeService {

    /**
     * Обновляет информацию о ноде по heartbeat.
     */
    fun heartbeat(request: NodeHeartbeatRequest): BackendNodeEntity

    /**
     * Возвращает список активных нод.
     */
    fun getNodes(): List<BackendNodeEntity>
}
