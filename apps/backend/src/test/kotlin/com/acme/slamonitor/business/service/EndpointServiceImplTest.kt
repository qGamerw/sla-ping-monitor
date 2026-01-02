package com.acme.slamonitor.business.service

import com.acme.slamonitor.api.dto.request.EndpointRequest
import com.acme.slamonitor.business.service.impl.EndpointServiceImpl
import com.acme.slamonitor.exception.EndpointException
import com.acme.slamonitor.persistence.EndpointRepository
import com.acme.slamonitor.persistence.domain.EndpointEntity
import com.acme.slamonitor.scope.JpaIoWorkerCoroutineDispatcher
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.coEvery
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.Optional
import java.util.UUID

@DisplayName("EndpointServiceImpl")
class EndpointServiceImplTest {

    private val endpointRepository = mockk<EndpointRepository>()
    private val jpaAsyncIoWorker = mockk<JpaIoWorkerCoroutineDispatcher>()
    private val service = EndpointServiceImpl(endpointRepository, jpaAsyncIoWorker)

    @Test
    fun shouldQueueCreateWhenRequestIsValid() {
        val request = EndpointRequest(name = "name", url = "https://example.com")
        val blockSlot = slot<() -> Unit>()

        every { jpaAsyncIoWorker.executeWithTransactionalConsumer(match { it.startsWith("Create endpoint") }, capture(blockSlot)) } answers {
            blockSlot.captured.invoke()
        }
        every { endpointRepository.save(any()) } answers { firstArg() }

        val result = service.create(request)

        assertEquals("Endpoint has been added to the creation queue", result.message)
        verify(exactly = 1) { endpointRepository.save(any()) }
        verify(exactly = 1) { jpaAsyncIoWorker.executeWithTransactionalConsumer(match { it.startsWith("Create endpoint") }, any()) }
    }

    @Test
    fun shouldUpdateEntityWhenEndpointExists() {
        val id = UUID.randomUUID()
        val entity = EndpointEntity(
            id = id,
            name = "old",
            url = "https://old.example.com",
            method = "GET",
            timeoutMs = 1000,
            expectedStatus = listOf(200),
            intervalSec = 30,
            enabled = true
        )
        val request = EndpointRequest(name = "new", url = "https://new.example.com", method = "POST", timeoutMs = 2000)
        val blockSlot = slot<() -> Unit>()

        coEvery {
            jpaAsyncIoWorker.executeWithTransactionalSupplier<Optional<EndpointEntity>>(
                match { it.startsWith("Get endpoint") },
                any()
            )
        } returns Optional.of(entity)
        every { jpaAsyncIoWorker.executeWithTransactionalConsumer(match { it.startsWith("Update endpoint") }, capture(blockSlot)) } answers {
            blockSlot.captured.invoke()
        }
        every { endpointRepository.save(entity) } returns entity

        val result = service.update(id, request)

        assertEquals("Endpoint with id $id has been added to the update queue", result.message)
        assertEquals("new", entity.name)
        assertEquals("https://new.example.com", entity.url)
        assertEquals("POST", entity.method)
        assertEquals(2000, entity.timeoutMs)
        verify(exactly = 1) { endpointRepository.save(entity) }
    }

    @Test
    fun shouldThrowWhenEndpointMissingOnUpdate() {
        val id = UUID.randomUUID()
        val request = EndpointRequest(name = "new", url = "https://new.example.com")

        coEvery {
            jpaAsyncIoWorker.executeWithTransactionalSupplier<Optional<EndpointEntity>>(
                match { it.startsWith("Get endpoint") },
                any()
            )
        } returns Optional.empty()

        val exception = assertThrows(EndpointException::class.java) {
            service.update(id, request)
        }

        assertEquals("No endpoint found with id $id", exception.message)
    }

    @Test
    fun shouldQueueDeleteWhenEndpointExists() {
        val id = UUID.randomUUID()
        val blockSlot = slot<() -> Unit>()

        every { jpaAsyncIoWorker.executeWithTransactionalConsumer(match { it.startsWith("Delete endpoint") }, capture(blockSlot)) } answers {
            blockSlot.captured.invoke()
        }
        every { endpointRepository.deleteById(id) } returns Unit

        val result = service.deleteEndpoint(id)

        assertEquals("Deletion process is queued for id: $id", result.message)
        verify(exactly = 1) { endpointRepository.deleteById(id) }
    }

    @Test
    fun shouldThrowWhenEndpointMissingOnGet() {
        val id = UUID.randomUUID()

        coEvery {
            jpaAsyncIoWorker.executeWithTransactionalSupplier<Optional<EndpointEntity>>(
                match { it.startsWith("Get endpoint") },
                any()
            )
        } returns Optional.empty()

        val exception = assertThrows(EndpointException::class.java) {
            service.getEndpoint(id)
        }

        assertEquals("No endpoint found with id $id", exception.message)
    }
}
