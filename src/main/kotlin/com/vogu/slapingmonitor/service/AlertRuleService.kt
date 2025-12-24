package com.vogu.slapingmonitor.service

import com.vogu.slapingmonitor.api.AlertRuleRequest
import com.vogu.slapingmonitor.domain.AlertRuleEntity
import com.vogu.slapingmonitor.repository.AlertRuleRepository
import java.util.UUID
import org.springframework.stereotype.Service

@Service
class AlertRuleService(
    private val alertRuleRepository: AlertRuleRepository,
    private val endpointService: EndpointService
) {
    /** Создаёт правило алерта для endpoint. */
    fun create(request: AlertRuleRequest): AlertRuleEntity {
        val endpoint = endpointService.get(request.endpointId)
        val entity = AlertRuleEntity(
            endpoint = endpoint,
            type = request.type,
            threshold = request.threshold,
            windowSec = request.windowSec,
            triggerForSec = request.triggerForSec,
            cooldownSec = request.cooldownSec,
            hysteresisRatio = request.hysteresisRatio,
            enabled = request.enabled ?: true
        )
        return alertRuleRepository.save(entity)
    }

    /** Обновляет параметры правила алерта. */
    fun update(id: UUID, request: AlertRuleRequest): AlertRuleEntity {
        val entity = alertRuleRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Alert rule $id not found") }
        entity.type = request.type
        entity.threshold = request.threshold
        entity.windowSec = request.windowSec
        entity.triggerForSec = request.triggerForSec
        entity.cooldownSec = request.cooldownSec
        entity.hysteresisRatio = request.hysteresisRatio
        entity.enabled = request.enabled ?: entity.enabled
        return alertRuleRepository.save(entity)
    }

    /** Возвращает список правил, опционально по endpoint. */
    fun list(endpointId: UUID?): List<AlertRuleEntity> =
        if (endpointId == null) alertRuleRepository.findAll() else alertRuleRepository.findAllByEndpointId(endpointId)

    /** Возвращает правило алерта по идентификатору. */
    fun get(id: UUID): AlertRuleEntity = alertRuleRepository.findById(id)
        .orElseThrow { IllegalArgumentException("Alert rule $id not found") }

    /** Удаляет правило алерта по идентификатору. */
    fun delete(id: UUID) {
        alertRuleRepository.deleteById(id)
    }
}
