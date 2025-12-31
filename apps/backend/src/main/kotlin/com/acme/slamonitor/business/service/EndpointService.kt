package com.acme.slamonitor.business.service

import com.acme.slamonitor.api.dto.Message
import com.acme.slamonitor.api.dto.request.EndpointRequest
import com.acme.slamonitor.api.dto.response.EndpointResponse
import java.util.UUID

interface EndpointService {

    fun create(request: EndpointRequest): Message

    fun update(id: UUID, request: EndpointRequest): Message

    fun deleteEndpoint(id: UUID): Message

    fun getEndpoint(id: UUID): EndpointResponse

    fun getEndpoints(): List<EndpointResponse>

}
