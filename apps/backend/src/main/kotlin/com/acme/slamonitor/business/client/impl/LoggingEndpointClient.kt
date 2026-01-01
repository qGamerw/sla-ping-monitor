package com.acme.slamonitor.business.client.impl

import com.acme.slamonitor.business.client.EndpointClient
import com.acme.slamonitor.business.client.dto.RuntimeRequest
import io.ktor.client.statement.HttpResponse
import io.ktor.util.toMap
import java.util.UUID
import kotlin.system.measureTimeMillis
import org.slf4j.LoggerFactory

class LoggingEndpointClient(
    private val delegate: EndpointClient
) : EndpointClient {

    override suspend fun call(req: RuntimeRequest): HttpResponse {
        val rid = UUID.randomUUID().toString().substring(0, 8)

        val safeReqHeaders = req.headers.maskSensitive()
        val reqBodyPreview = req.body.preview()

        LOG.info("➡️ [$rid] ${req.method.value} ${req.url} headers=$safeReqHeaders body=$reqBodyPreview")

        lateinit var resp: HttpResponse
        val tookMs = measureTimeMillis {
            resp = try {
                delegate.call(req)
            } catch (t: Throwable) {
                LOG.error("💥 [$rid] ${req.method.value} ${req.url} failed: ${t.message}", t)
                throw t
            }
        }

        val safeRespHeaders = resp.headers.toMap().maskSensitiveList()

        LOG.info(
            "✅ [$rid] ${req.method.value} ${req.url} -> ${resp.status.value} in $tookMs ms respHeaders=$safeRespHeaders",
        )

        return resp
    }
}

private val LOG by lazy { LoggerFactory.getLogger(LoggingEndpointClient::class.java) }

private fun Map<String, String>.maskSensitive(): Map<String, String> =
    mapValues { (k, v) ->
        if (k.equals("Authorization", true) ||
            k.equals("Cookie", true) ||
            k.equals("X-Api-Key", true)
        ) "***" else v
    }

private fun Map<String, List<String>>.maskSensitiveList(): Map<String, List<String>> =
    mapValues { (k, v) ->
        if (k.equals("Set-Cookie", true) ||
            k.equals("Authorization", true) ||
            k.equals("Cookie", true)
        ) listOf("***") else v
    }

private fun Any?.preview(max: Int = 512): String = when (this) {
    null -> "null"
    is String -> if (length <= max) this else take(max) + "…(${length} chars)"
    is ByteArray -> "byte[${size}]"
    else -> this.toString().let { if (it.length <= max) it else it.take(max) + "…(${it.length} chars)" }
}
