package com.acme.slamonitor.configuration

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KtorClientConfig {

    @Bean(destroyMethod = "close")
    fun ktorHttpClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }

        install(HttpTimeout)
        install(Logging) { level = LogLevel.NONE }

        engine {
            // базовые настройки движка (общие для всех запросов)
            maxConnectionsCount = 1_000
            endpoint {
                maxConnectionsPerRoute = 100
                pipelineMaxSize = 20
                keepAliveTime = 5_000
                connectTimeout = 5_000
                connectAttempts = 2
            }

            requestTimeout = 0
        }
    }
}
