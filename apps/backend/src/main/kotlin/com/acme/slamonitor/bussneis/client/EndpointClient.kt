package com.acme.slamonitor.bussneis.client

import com.acme.slamonitor.bussneis.dto.client.RuntimeRequest
import io.ktor.client.statement.HttpResponse

interface EndpointClient {
    suspend fun call(req: RuntimeRequest): HttpResponse
}
