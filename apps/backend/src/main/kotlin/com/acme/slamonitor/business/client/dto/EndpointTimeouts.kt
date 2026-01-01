package com.acme.slamonitor.business.client.dto

data class EndpointTimeouts(
    val connectMs: Long? = null,
    val requestMs: Long? = null,
    val socketMs: Long? = null
)
