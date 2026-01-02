package com.acme.slamonitor.post_processor

import com.acme.slamonitor.business.client.EndpointClient
import com.acme.slamonitor.business.client.impl.LoggingEndpointClient
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("EndpointClientLoggingBeanPostProcessor")
class EndpointClientLoggingBeanPostProcessorTest {

    private val processor = EndpointClientLoggingBeanPostProcessor()

    @Test
    fun shouldWrapEndpointClientWithLogging() {
        val client = mockk<EndpointClient>()

        val result = processor.postProcessAfterInitialization(client, "client")

        assertTrue(result is LoggingEndpointClient)
    }

    @Test
    fun shouldReturnSameInstanceWhenAlreadyLoggingClient() {
        val client = LoggingEndpointClient(mockk())

        val result = processor.postProcessAfterInitialization(client, "client")

        assertTrue(result === client)
    }
}
