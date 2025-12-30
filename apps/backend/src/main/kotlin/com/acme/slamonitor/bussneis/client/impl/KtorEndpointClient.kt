package com.acme.slamonitor.bussneis.client.impl

import com.acme.slamonitor.bussneis.client.EndpointClient
import com.acme.slamonitor.bussneis.dto.client.RuntimeRequest
import io.ktor.client.HttpClient
import io.ktor.client.plugins.timeout
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.contentType

class KtorEndpointClient(
    private val http: HttpClient
) : EndpointClient {

    override suspend fun call(req: RuntimeRequest): HttpResponse {
        return http.request(req.url) {
            method = req.method

            headers {
                req.headers.forEach { (k, v) -> append(k, v) }
            }

            timeout {
                req.timeouts.connectMs?.let { connectTimeoutMillis = it }
                req.timeouts.requestMs?.let { requestTimeoutMillis = it }
                req.timeouts.socketMs?.let { socketTimeoutMillis = it }
            }

            if (req.body != null) {
                setBody(req.body)
                req.contentType?.let { contentType(it) }
            }
        }
    }
}
