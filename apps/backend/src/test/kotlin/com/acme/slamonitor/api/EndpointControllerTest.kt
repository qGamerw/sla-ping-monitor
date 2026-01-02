package com.acme.slamonitor.api

import com.acme.slamonitor.api.dto.Message
import com.acme.slamonitor.api.dto.request.EndpointRequest
import com.acme.slamonitor.api.validate.ValidationPipeline
import com.acme.slamonitor.business.service.EndpointService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.UUID

@DisplayName("EndpointController")
class EndpointControllerTest {

    private val endpointService = mockk<EndpointService>()
    private val validator = mockk<ValidationPipeline<EndpointRequest>>()
    private val controller = EndpointController(endpointService, validator)

    @Test
    fun shouldCreateEndpointWhenValid() {
        val request = EndpointRequest(name = "name", url = "https://example.com")
        every { validator.validate(request) } returns Unit
        every { endpointService.create(request) } returns Message("ok")

        val response = controller.createEndpoint(request)

        assertEquals(true, response.susses)
        assertEquals("ok", response.body?.message)
        verify(exactly = 1) { validator.validate(request) }
        verify(exactly = 1) { endpointService.create(request) }
    }

    @Test
    fun shouldUpdateEndpointWhenValid() {
        val id = UUID.randomUUID()
        val request = EndpointRequest(name = "name", url = "https://example.com")
        every { validator.validate(request) } returns Unit
        every { endpointService.update(id, request) } returns Message("updated")

        val response = controller.updateEndpoint(id, request)

        assertEquals("updated", response.body?.message)
        verify(exactly = 1) { endpointService.update(id, request) }
    }
}
