package com.acme.slamonitor.business.service

import com.acme.slamonitor.api.dto.Message
import com.acme.slamonitor.api.dto.request.EndpointRequest
import com.acme.slamonitor.api.dto.response.EndpointResponse
import java.util.UUID

/**
 * Сервис для CRUD операций над эндпоинтами.
 */
interface EndpointService {

    /**
     * Создает эндпоинт.
     */
    fun create(request: EndpointRequest): Message

    /**
     * Обновляет эндпоинт.
     */
    fun update(id: UUID, request: EndpointRequest): Message

    /**
     * Удаляет эндпоинт.
     */
    fun deleteEndpoint(id: UUID): Message

    /**
     * Возвращает эндпоинт по идентификатору.
     */
    fun getEndpoint(id: UUID): EndpointResponse

    /**
     * Возвращает список эндпоинтов.
     */
    fun getEndpoints(): List<EndpointResponse>

}
