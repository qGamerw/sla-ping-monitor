package com.acme.slamonitor.api.dto.request

import com.acme.slamonitor.utils.DEFAULT_EXPECTED_STATUS
import com.acme.slamonitor.utils.DEFAULT_INTERVAL_SEC
import com.acme.slamonitor.utils.DEFAULT_TIMEOUT_MS

/**
 * Запрос на создание или обновление эндпоинта для мониторинга.
 */
data class EndpointRequest(
    val name: String,
    val url: String,
    val method: String? = "GET",
    val headers: Map<String, String>? = null,
    val timeoutMs: Int? = DEFAULT_TIMEOUT_MS,
    val expectedStatus: List<Int>? = DEFAULT_EXPECTED_STATUS,
    val intervalSec: Int? = DEFAULT_INTERVAL_SEC,
    val enabled: Boolean? = true,
    val tags: List<String>? = null
)
