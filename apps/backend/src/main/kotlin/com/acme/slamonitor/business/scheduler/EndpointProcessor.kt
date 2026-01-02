package com.acme.slamonitor.business.scheduler

import com.acme.slamonitor.business.scheduler.dto.EndpointResult
import com.acme.slamonitor.business.scheduler.dto.EndpointView

/**
 * Обрабатывает проверку одного эндпоинта.
 */
interface EndpointProcessor {
    /**
     * Выполняет проверку и возвращает результат.
     */
    suspend fun check(endpoint: EndpointView): EndpointResult
}
