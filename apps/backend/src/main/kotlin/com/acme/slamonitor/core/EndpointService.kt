package com.acme.slamonitor.core

import com.acme.slamonitor.api.dto.EndpointRequest
import com.acme.slamonitor.api.dto.EndpointResponse
import com.acme.slamonitor.api.dto.Message
import java.util.UUID

interface EndpointService {

    fun create(request: EndpointRequest): Message

    fun update(id: UUID, request: EndpointRequest): Message

    fun deleteEndpoint(id: UUID): Message

    fun getEndpoint(id: UUID): EndpointResponse

    fun getEndpoints(): List<EndpointResponse>

}
