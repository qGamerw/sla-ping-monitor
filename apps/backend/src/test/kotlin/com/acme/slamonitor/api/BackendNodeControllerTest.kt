package com.acme.slamonitor.api

import com.acme.slamonitor.api.dto.request.NodeHeartbeatRequest
import com.acme.slamonitor.business.service.BackendNodeService
import com.acme.slamonitor.persistence.domain.BackendNodeEntity
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.Instant

@DisplayName("BackendNodeController")
class BackendNodeControllerTest {

    private val backendNodeService = mockk<BackendNodeService>()
    private val controller = BackendNodeController(backendNodeService)

    @Test
    fun shouldHandleHeartbeat() {
        val request = NodeHeartbeatRequest(
            nodeId = "node-1",
            baseUrl = "http://localhost",
            startedAt = Instant.parse("2024-01-01T00:00:00Z")
        )
        val entity = BackendNodeEntity(
            nodeId = "node-1",
            baseUrl = "http://localhost",
            startedAt = request.startedAt,
            lastHeartbeatAt = Instant.parse("2024-01-01T00:01:00Z"),
            meta = null
        )
        every { backendNodeService.heartbeat(request) } returns entity

        val response = controller.heartbeat(request)

        assertEquals(entity, response.body)
        verify(exactly = 1) { backendNodeService.heartbeat(request) }
    }

    @Test
    fun shouldReturnNodes() {
        every { backendNodeService.getNodes() } returns emptyList()

        val response = controller.getNodes()

        assertEquals(0, response.body?.size)
    }
}
