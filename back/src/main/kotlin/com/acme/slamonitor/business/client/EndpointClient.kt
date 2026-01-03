package com.acme.slamonitor.business.client

import com.acme.slamonitor.business.client.dto.RuntimeRequest
import io.ktor.client.statement.HttpResponse

/**
 * HTTP-клиент для выполнения проверок эндпоинтов.
 */
interface EndpointClient {
    /**
     * Выполняет запрос и возвращает сырой ответ клиента.
     */
    suspend fun call(req: RuntimeRequest): HttpResponse
}
