package com.acme.slamonitor.business.service.impl

import com.acme.slamonitor.api.dto.request.NodeHeartbeatRequest
import com.acme.slamonitor.persistence.BackendNodeRepository
import com.acme.slamonitor.persistence.domain.BackendNodeEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.Optional

@DisplayName("BackendNodeServiceImpl")
class BackendNodeServiceImplTest {

    private val backendNodeRepository = mockk<BackendNodeRepository>()
    private val service = BackendNodeServiceImpl(backendNodeRepository)

    @Test
    fun shouldCreateNewNodeWhenNotExists() {
        val request = NodeHeartbeatRequest(
            nodeId = "node-1",
            baseUrl = "http://localhost:8080",
            startedAt = Instant.parse("2024-01-01T00:00:00Z"),
            meta = mapOf("env" to "test")
        )
        val savedSlot = mutableListOf<BackendNodeEntity>()

        every { backendNodeRepository.findById("node-1") } returns Optional.empty()
        every { backendNodeRepository.save(capture(savedSlot)) } answers { savedSlot.first() }

        val result = service.heartbeat(request)

        assertEquals("node-1", result.nodeId)
        assertEquals("http://localhost:8080", result.baseUrl)
        assertEquals(request.startedAt, result.startedAt)
        assertEquals(request.meta, result.meta)
        verify(exactly = 1) { backendNodeRepository.save(any()) }
    }

    @Test
    fun shouldUpdateExistingNodeWhenExists() {
        val existing = BackendNodeEntity(
            nodeId = "node-1",
            baseUrl = "http://old",
            startedAt = Instant.parse("2024-01-01T00:00:00Z"),
            lastHeartbeatAt = Instant.parse("2024-01-02T00:00:00Z"),
            meta = mapOf("env" to "old")
        )
        val request = NodeHeartbeatRequest(
            nodeId = "node-1",
            baseUrl = "http://new",
            startedAt = Instant.parse("2024-01-01T00:00:00Z"),
            meta = mapOf("env" to "new")
        )

        every { backendNodeRepository.findById("node-1") } returns Optional.of(existing)
        every { backendNodeRepository.save(existing) } returns existing

        val result = service.heartbeat(request)

        assertEquals("http://new", result.baseUrl)
        assertEquals(mapOf("env" to "new"), result.meta)
        verify(exactly = 1) { backendNodeRepository.save(existing) }
    }

    @Test
    fun shouldReturnNodesWhenRequested() {
        val nodes = listOf(
            BackendNodeEntity(
                nodeId = "node-1",
                baseUrl = "http://one",
                startedAt = Instant.parse("2024-01-01T00:00:00Z"),
                lastHeartbeatAt = Instant.parse("2024-01-02T00:00:00Z"),
                meta = null
            )
        )

        every { backendNodeRepository.findAll() } returns nodes

        val result = service.getNodes()

        assertEquals(nodes, result)
    }
}
