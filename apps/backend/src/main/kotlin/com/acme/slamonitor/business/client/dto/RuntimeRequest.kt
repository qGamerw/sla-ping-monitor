package com.acme.slamonitor.business.client.dto

import io.ktor.http.ContentType
import io.ktor.http.HttpMethod

data class RuntimeRequest(
    val url: String,
    val method: HttpMethod,
    val headers: Map<String, String> = emptyMap(),
    val timeouts: EndpointTimeouts = EndpointTimeouts(),
    val body: Any? = null,
    val contentType: ContentType? = ContentType.Application.Json
)
