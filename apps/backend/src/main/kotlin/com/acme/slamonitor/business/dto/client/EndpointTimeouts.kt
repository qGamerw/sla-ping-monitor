package com.acme.slamonitor.business.dto.client

data class EndpointTimeouts(
    val connectMs: Long? = null,
    val requestMs: Long? = null,
    val socketMs: Long? = null
)
