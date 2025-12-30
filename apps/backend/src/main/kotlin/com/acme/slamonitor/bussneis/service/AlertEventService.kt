package com.acme.slamonitor.bussneis.service

import com.acme.slamonitor.api.dto.AlertEventRequest
import com.acme.slamonitor.api.dto.AlertEventResponse
import java.util.UUID

interface AlertEventService {

    fun create(request: AlertEventRequest): AlertEventResponse

    fun update(id: UUID, request: AlertEventRequest): AlertEventResponse

    fun list(state: String?): List<AlertEventResponse>
}
