package com.acme.slamonitor.business.service.impl

import com.acme.slamonitor.api.dto.AlertEventRequest
import com.acme.slamonitor.api.dto.AlertEventResponse
import com.acme.slamonitor.business.service.AlertEventService
import com.acme.slamonitor.persistence.AlertEventRepository
import com.acme.slamonitor.persistence.mapper.AlertEventMapper.Companion.MAPPER
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AlertEventServiceImpl(
    private val alertEventRepository: AlertEventRepository
) : AlertEventService {


    override fun create(request: AlertEventRequest): AlertEventResponse {
//        val rule = alertRuleServiceImpl.get(request.ruleId)
//        val endpoint = endpointService.get(request.endpointId)
//        val entity = AlertEventEntity(
//            rule = MAPPER.toEntity(rule),
//            endpoint = endpoint,
//            state = request.state,
//            openedAt = request.openedAt,
//            resolvedAt = request.resolvedAt,
//            lastNotifiedAt = request.lastNotifiedAt,
//            details = request.details
//        )
//        return MAPPER.toResponse(alertEventRepository.save(entity))
        throw RuntimeException()
    }

    override fun update(
        id: UUID,
        request: AlertEventRequest
    ): AlertEventResponse {
        val existing = alertEventRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Alert event $id not found") }
        existing.state = request.state
        existing.openedAt = request.openedAt
        existing.resolvedAt = request.resolvedAt
        existing.lastNotifiedAt = request.lastNotifiedAt
        existing.details = request.details
        return MAPPER.toResponse(alertEventRepository.save(existing))
    }

    override fun list(state: String?): List<AlertEventResponse> =
        if (state == null)
            MAPPER.toResponse(alertEventRepository.findAll())
        else
            MAPPER.toResponse(alertEventRepository.findAllByState(state))
}
