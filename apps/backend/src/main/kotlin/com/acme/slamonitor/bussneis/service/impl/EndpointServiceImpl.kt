package com.acme.slamonitor.bussneis.service.impl

import com.acme.slamonitor.api.dto.EndpointRequest
import com.acme.slamonitor.api.dto.EndpointResponse
import com.acme.slamonitor.api.dto.Message
import com.acme.slamonitor.bussneis.service.EndpointService
import com.acme.slamonitor.exception.EndpointException
import com.acme.slamonitor.persistence.EndpointRepository
import com.acme.slamonitor.persistence.domain.EndpointEntity
import com.acme.slamonitor.persistence.mapper.EndpointMapper.Companion.MAPPER
import com.acme.slamonitor.scope.JpaIoWorkerCoroutineDispatcher
import com.acme.slamonitor.utils.DEFAULT_EXPECTED_STATUS
import com.acme.slamonitor.utils.DEFAULT_INTERVAL_SEC
import com.acme.slamonitor.utils.DEFAULT_TIMEOUT_MS
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class EndpointServiceImpl(
    private val endpointRepository: EndpointRepository,
    private val jpaAsyncIoWorker: JpaIoWorkerCoroutineDispatcher
) : EndpointService {

    /** Создаёт endpoint и подготавливает поля планировщика. */
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
        )
        jpaAsyncIoWorker.executeWithTransactionalConsumer("Create endpoint $id") {
            endpointRepository.save(entity)
        }

        return Message("Endpoint has been added to the creation queue")
    }

    /** Обновляет конфигурацию endpoint. */
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

    /** Удаляет endpoint по идентификатору. */
    override fun deleteEndpoint(id: UUID): Message {
        jpaAsyncIoWorker.executeWithTransactionalConsumer("Delete endpoint: $id") {
            endpointRepository.deleteById(id)
        }

        return Message("Deletion process is queued for id: $id")
    }

    /** Возвращает endpoint по идентификатору. */
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

    /** Возвращает список всех endpoints. */
    override fun getEndpoints(): List<EndpointResponse> {
        val entities = runBlocking {
            jpaAsyncIoWorker.executeWithTransactionalSupplier("Get endpoints") {
                endpointRepository.findAll()
            }
        }
        return MAPPER.toResponses(entities)
    }
}
