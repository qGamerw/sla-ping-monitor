package com.acme.slamonitor.business.service.impl

import com.acme.slamonitor.api.dto.Message
import com.acme.slamonitor.api.dto.request.EndpointRequest
import com.acme.slamonitor.api.dto.response.EndpointResponse
import com.acme.slamonitor.business.service.EndpointService
import com.acme.slamonitor.exception.EndpointException
import com.acme.slamonitor.persistence.EndpointRepository
import com.acme.slamonitor.persistence.domain.EndpointEntity
import com.acme.slamonitor.persistence.mapper.EndpointMapper.Companion.MAPPER
import com.acme.slamonitor.scope.JpaIoWorkerCoroutineDispatcher
import com.acme.slamonitor.utils.DEFAULT_EXPECTED_STATUS
import com.acme.slamonitor.utils.DEFAULT_INTERVAL_SEC
import com.acme.slamonitor.utils.DEFAULT_TIMEOUT_MS
import java.util.UUID
import kotlinx.coroutines.runBlocking

/**
 * Реализация сервиса эндпоинтов с асинхронной JPA-очередью.
 */
open class EndpointServiceImpl(
    private val endpointRepository: EndpointRepository,
    private val jpaAsyncIoWorker: JpaIoWorkerCoroutineDispatcher
) : EndpointService {

    /**
     * Создает эндпоинт и ставит сохранение в очередь.
     */
    override fun create(request: EndpointRequest): Message {
        val id = UUID.randomUUID()
        val entity = EndpointEntity(
            id = id,
            name = request.name,
            url = request.url,
            method = request.method ?: "GET",
            headers = request.headers,
            timeoutMs = request.timeoutMs ?: DEFAULT_TIMEOUT_MS,
            expectedStatus = request.expectedStatus ?: DEFAULT_EXPECTED_STATUS,
            intervalSec = request.intervalSec ?: DEFAULT_INTERVAL_SEC,
            enabled = request.enabled ?: true,
            tags = request.tags,
        )
        jpaAsyncIoWorker.executeWithTransactionalConsumer("Create endpoint $id") {
            endpointRepository.save(entity)
        }

        return Message("Endpoint has been added to the creation queue")
    }

    /**
     * Обновляет эндпоинт и ставит сохранение в очередь.
     */
    override fun update(id: UUID, request: EndpointRequest): Message {
        val entity = runBlocking {
            jpaAsyncIoWorker.executeWithTransactionalSupplier("Get endpoint $id") {
                endpointRepository.findById(id)
            }
        }.orElseThrow {
            EndpointException("No endpoint found with id $id")
        }

        entity.name = request.name
        entity.url = request.url
        entity.method = request.method ?: entity.method
        entity.headers = request.headers
        entity.timeoutMs = request.timeoutMs ?: entity.timeoutMs
        entity.expectedStatus = request.expectedStatus ?: entity.expectedStatus
        entity.intervalSec = request.intervalSec ?: entity.intervalSec
        entity.enabled = request.enabled ?: entity.enabled

        jpaAsyncIoWorker.executeWithTransactionalConsumer("Update endpoint $id") {
            endpointRepository.save(entity)
        }

        return Message("Endpoint with id $id has been added to the update queue")
    }

    /**
     * Ставит удаление эндпоинта в очередь.
     */
    override fun deleteEndpoint(id: UUID): Message {
        jpaAsyncIoWorker.executeWithTransactionalConsumer("Delete endpoint: $id") {
            endpointRepository.deleteById(id)
        }

        return Message("Deletion process is queued for id: $id")
    }

    /**
     * Возвращает эндпоинт по идентификатору.
     */
    override fun getEndpoint(id: UUID): EndpointResponse {
        val entity = runBlocking {
            jpaAsyncIoWorker.executeWithTransactionalSupplier("Get endpoint: $id") {
                endpointRepository.findById(id)
            }
        }.orElseThrow {
            EndpointException("No endpoint found with id $id")
        }
        return MAPPER.toResponse(entity)
    }

    /**
     * Возвращает список всех эндпоинтов.
     */
    override fun getEndpoints(): List<EndpointResponse> {
        val entities = runBlocking {
            jpaAsyncIoWorker.executeWithTransactionalSupplier("Get endpoints") {
                endpointRepository.findAll()
            }
        }
        return MAPPER.toResponses(entities)
    }
}
