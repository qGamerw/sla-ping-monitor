package com.acme.slamonitor.business.scheduler.impl

import com.acme.slamonitor.business.client.EndpointClient
import com.acme.slamonitor.business.client.dto.EndpointTimeouts
import com.acme.slamonitor.business.client.dto.RuntimeRequest
import com.acme.slamonitor.business.scheduler.EndpointProcessor
import com.acme.slamonitor.business.scheduler.dto.EndpointBad
import com.acme.slamonitor.business.scheduler.dto.EndpointOk
import com.acme.slamonitor.business.scheduler.dto.EndpointResult
import com.acme.slamonitor.business.scheduler.dto.EndpointView
import com.acme.slamonitor.business.url.LocalhostUrlRewriter
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import java.time.Instant
import org.slf4j.LoggerFactory

/**
 * Выполняет проверку эндпоинта через HTTP-клиент.
 */
open class EndpointProcessorImpl(
    private val client: EndpointClient,
    private val localhostUrlRewriter: LocalhostUrlRewriter
) : EndpointProcessor {
    /**
     * Делает запрос и формирует результат проверки.
     */
    override suspend fun check(endpoint: EndpointView): EndpointResult {
        val startNs = System.nanoTime()

        return try {
            val resolvedUrl = localhostUrlRewriter.rewrite(endpoint.url)
            val request = RuntimeRequest(
                resolvedUrl,
                HttpMethod.parse(endpoint.method),
                endpoint.headers ?: emptyMap(),
                EndpointTimeouts(
                    endpoint.timeoutMs.toLong(),
                    endpoint.timeoutMs.toLong(),
                    endpoint.timeoutMs.toLong()
                ),
            )

            client.call(request).let {
                if (endpoint.expectedStatus.contains(it.status.value)) {
                    EndpointOk(
                        endpoint.id,
                        endpoint.dbVersion,
                        Instant.now(),
                        it.status.value,
                        (System.nanoTime() - startNs) / 1_000_000
                    )
                } else {
                    EndpointBad(
                        endpoint.id,
                        endpoint.dbVersion,
                        Instant.now(),
                        it.bodyAsText(),
                        (System.nanoTime() - startNs) / 1_000_000,
                        it.status.value
                    )
                }

            }
        } catch (e: Exception) {
            LOG.error("Error during request", e)
            EndpointBad(
                endpoint.id,
                endpoint.dbVersion,
                Instant.now(),
                e.message ?: "Unknown error",
                (System.nanoTime() - startNs) / 1_000_000,
            )
        }
    }
}

private val LOG by lazy { LoggerFactory.getLogger(EndpointProcessorImpl::class.java) }
