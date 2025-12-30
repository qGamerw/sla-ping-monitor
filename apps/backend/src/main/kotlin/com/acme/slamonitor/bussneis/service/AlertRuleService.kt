package com.acme.slamonitor.bussneis.service

import com.acme.slamonitor.api.dto.AlertRuleRequest
import com.acme.slamonitor.api.dto.AlertRuleResponse
import java.util.UUID

interface AlertRuleService {

    fun create(request: AlertRuleRequest): AlertRuleResponse

    fun update(id: UUID, request: AlertRuleRequest): AlertRuleResponse

    fun list(endpointId: UUID?): List<AlertRuleResponse>

    fun get(id: UUID): AlertRuleResponse

    fun delete(id: UUID)

}
