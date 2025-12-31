package com.acme.slamonitor.business.scheduler.impl

import com.acme.slamonitor.business.scheduler.EndpointProcessor
import com.acme.slamonitor.business.scheduler.dto.EndpointOk
import com.acme.slamonitor.business.scheduler.dto.EndpointResult
import com.acme.slamonitor.business.scheduler.dto.EndpointView
import java.time.Instant
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EndpointProcessorImpl : EndpointProcessor {
    override suspend fun check(endpoint: EndpointView): EndpointResult {
        LOG.info("processing job ${endpoint.name}")
        delay(50) // имитация I/O
        // throw RuntimeException("boom") // раскомментируй, чтобы увидеть retry/fail

        return EndpointOk(
            endpoint.id,
            endpoint.dbVersion,
            Instant.now(),
            200,
            200
        )
    }
}

private val LOG by lazy { LoggerFactory.getLogger(EndpointProcessorImpl::class.java) }
