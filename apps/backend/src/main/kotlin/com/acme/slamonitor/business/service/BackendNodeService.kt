package com.acme.slamonitor.business.service

import com.acme.slamonitor.api.dto.request.NodeHeartbeatRequest
import com.acme.slamonitor.persistence.domain.BackendNodeEntity

interface BackendNodeService {

    fun heartbeat(request: NodeHeartbeatRequest): BackendNodeEntity
    fun getNodes(): List<BackendNodeEntity>
}