package com.acme.slamonitor.business.client

import com.acme.slamonitor.business.dto.client.RuntimeRequest
import io.ktor.client.statement.HttpResponse

interface EndpointClient {
    suspend fun call(req: RuntimeRequest): HttpResponse
}
