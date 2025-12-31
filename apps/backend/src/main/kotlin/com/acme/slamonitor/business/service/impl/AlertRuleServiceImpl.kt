package com.acme.slamonitor.business.service.impl

import com.acme.slamonitor.api.dto.AlertRuleRequest
import com.acme.slamonitor.api.dto.AlertRuleResponse
import com.acme.slamonitor.business.service.AlertRuleService
import com.acme.slamonitor.persistence.AlertRuleRepository
import com.acme.slamonitor.persistence.mapper.AlertRuleMapper.Companion.MAPPER
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AlertRuleServiceImpl(
    private val alertRuleRepository: AlertRuleRepository,
    private val endpointService: EndpointServiceImpl
) : AlertRuleService {
    /** Создаёт правило алерта для endpoint. */
    override fun create(request: AlertRuleRequest): AlertRuleResponse {
//        val endpoint = endpointService.get(request.endpointId)
//        val entity = AlertRuleEntity(
//            endpoint = endpoint,
//            type = request.type,
//            threshold = request.threshold,
//            windowSec = request.windowSec,
//            triggerForSec = request.triggerForSec,
//            cooldownSec = request.cooldownSec,
//            hysteresisRatio = request.hysteresisRatio,
//            enabled = request.enabled ?: true
//        )
//        return MAPPER.toResponse(alertRuleRepository.save(entity))
        throw RuntimeException()
    }

    /** Обновляет параметры правила алерта. */
    override fun update(
        id: UUID,
        request: AlertRuleRequest
    ): AlertRuleResponse {
        val entity = alertRuleRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Alert rule $id not found") }
        entity.type = request.type
        entity.threshold = request.threshold
        entity.windowSec = request.windowSec
        entity.triggerForSec = request.triggerForSec
        entity.cooldownSec = request.cooldownSec
        entity.hysteresisRatio = request.hysteresisRatio
        entity.enabled = request.enabled ?: entity.enabled
        return MAPPER.toResponse(alertRuleRepository.save(entity))
    }

    /** Возвращает список правил, опционально по endpoint. */
    override fun list(endpointId: UUID?): List<AlertRuleResponse> =
        if (endpointId == null)
            MAPPER.toResponse(alertRuleRepository.findAll())
        else
            MAPPER.toResponse(alertRuleRepository.findAllByEndpointId(endpointId))

    /** Возвращает правило алерта по идентификатору. */
    override fun get(id: UUID): AlertRuleResponse = MAPPER.toResponse(alertRuleRepository.findById(id).get())

    /** Удаляет правило алерта по идентификатору. */
    override fun delete(id: UUID) {
        alertRuleRepository.deleteById(id)
    }
}
