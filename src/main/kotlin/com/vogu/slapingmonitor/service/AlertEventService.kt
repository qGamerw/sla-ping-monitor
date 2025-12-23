package com.vogu.slapingmonitor.service

import com.vogu.slapingmonitor.api.AlertEventRequest
import com.vogu.slapingmonitor.domain.AlertEventEntity
import com.vogu.slapingmonitor.repository.AlertEventRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AlertEventService(
    private val alertEventRepository: AlertEventRepository,
    private val alertRuleService: AlertRuleService,
    private val endpointService: EndpointService
) {
    fun create(request: AlertEventRequest): AlertEventEntity {
        val rule = alertRuleService.get(request.ruleId)
        val endpoint = endpointService.get(request.endpointId)
        val entity = AlertEventEntity(
            rule = rule,
            endpoint = endpoint,
            state = request.state,
            openedAt = request.openedAt,
            resolvedAt = request.resolvedAt,
            lastNotifiedAt = request.lastNotifiedAt,
            details = request.details
        )
        return alertEventRepository.save(entity)
    }

    fun update(id: UUID, request: AlertEventRequest): AlertEventEntity {
        val existing = alertEventRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Alert event $id not found") }
        existing.state = request.state
        existing.openedAt = request.openedAt
        existing.resolvedAt = request.resolvedAt
        existing.lastNotifiedAt = request.lastNotifiedAt
        existing.details = request.details
        return alertEventRepository.save(existing)
    }

    fun list(state: String?): List<AlertEventEntity> =
        if (state == null) alertEventRepository.findAll() else alertEventRepository.findAllByState(state)
}
