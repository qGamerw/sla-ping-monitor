package com.acme.slamonitor.bussneis.dto.client

data class EndpointTimeouts(
    val connectMs: Long? = null,
    val requestMs: Long? = null,
    val socketMs: Long? = null
)
