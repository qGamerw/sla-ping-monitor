package com.acme.slamonitor.business.service

import com.acme.slamonitor.api.dto.response.CheckResultResponse
import com.acme.slamonitor.api.dto.response.EndpointSummaryResponse
import com.acme.slamonitor.api.dto.response.StatsResponse
import java.time.Instant
import java.util.UUID

/**
 * Сервис для чтения статистики по эндпоинтам.
 */
interface StatsService {

    /**
     * Возвращает агрегированную статистику по эндпоинту.
     */
    fun getStats(endpointId: UUID, windowSec: Long, minSamples: Int? = 20): StatsResponse

    /**
     * Возвращает список проверок эндпоинта за период.
     */
    fun getChecks(endpointId: UUID, from: Instant, to: Instant): List<CheckResultResponse>

    /**
     * Возвращает краткое резюме по всем эндпоинтам.
     */
    fun getSummery(windowSec: Long): List<EndpointSummaryResponse>
}
