package com.acme.slamonitor.business.client.dto

/**
 * Таймауты HTTP-клиента для проверки эндпоинта.
 */
data class EndpointTimeouts(
    val connectMs: Long? = null,
    val requestMs: Long? = null,
    val socketMs: Long? = null
)
