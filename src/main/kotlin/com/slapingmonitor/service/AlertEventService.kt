package com.slapingmonitor.service

import com.slapingmonitor.repository.AlertEventRepository
import com.slapingmonitor.repository.domain.AlertEventEntity
import com.slapingmonitor.repository.mapper.AlertEventRequest
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class AlertEventService(
    private val alertEventRepository: AlertEventRepository,
    private val alertRuleService: AlertRuleService,
    private val endpointService: EndpointService
) {
    /** Создаёт событие алерта и сохраняет его. */
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

    /** Обновляет параметры события алерта. */
    fun update(
        id: UUID,
        request: AlertEventRequest
    ): AlertEventEntity {
        val existing = alertEventRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Alert event $id not found") }
        existing.state = request.state
        existing.openedAt = request.openedAt
        existing.resolvedAt = request.resolvedAt
        existing.lastNotifiedAt = request.lastNotifiedAt
        existing.details = request.details
        return alertEventRepository.save(existing)
    }

    /** Возвращает список событий алертов, опционально по состоянию. */
    fun list(state: String?): List<AlertEventEntity> =
        if (state == null) alertEventRepository.findAll() else alertEventRepository.findAllByState(state)
}
