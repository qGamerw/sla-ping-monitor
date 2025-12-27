package com.acme.slamonitor.api.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.Charset
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
class RestBodyLoggingFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val req = ContentCachingRequestWrapper(request)
        val res = ContentCachingResponseWrapper(response)

        val requestId = request.getHeader("X-Request-Id")?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()
        MDC.put("requestId", requestId)

        val startNs = System.nanoTime()
        val method = request.method
        val uri = request.requestURI + (request.queryString?.let { "?$it" } ?: "")

        try {
            filterChain.doFilter(req, res)
        } catch (ex: Exception) {
            log.error("\nHTTP {} {} failed requestId={}", method, uri, requestId, ex)
            throw ex
        } finally {
            val tookMs = (System.nanoTime() - startNs) / 1_000_000
            val status = res.status

            val reqBody = extractRequestBody(req)
            val resBody = extractResponseBody(res)

            val headers = extractSafeHeaders(request)

            when {
                status >= 500 -> log.error(
                    "HTTP {} {} -> {} ({} ms) \nrequestId={} \nheaders={} \nreqBody={} \nresBody={}",
                    method, uri, status, tookMs, requestId, headers, reqBody, resBody
                )

                status >= 400 -> log.warn(
                    "HTTP {} {} -> {} ({} ms) \nrequestId={} \nheaders={} \nreqBody={} \nresBody={}",
                    method, uri, status, tookMs, requestId, headers, reqBody, resBody
                )

                else -> log.info(
                    "HTTP {} {} -> {} ({} ms) \nrequestId={} \nheaders={} \nreqBody={} \nresBody={}",
                    method, uri, status, tookMs, requestId, headers, reqBody, resBody
                )
            }

            res.copyBodyToResponse()

            MDC.remove("requestId")
        }
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        if (path.startsWith("/actuator") || path == "/favicon.ico") return true

        val ct = request.contentType?.lowercase().orEmpty()
        if (ct.startsWith("multipart/")) return true
        if (ct.startsWith("application/octet-stream")) return true

        return false
    }

    private fun extractRequestBody(req: ContentCachingRequestWrapper): String {
        val bytes = req.contentAsByteArray
        if (bytes.isEmpty()) return "-"
        if (!isLoggableContentType(req.contentType)) return "<non-text-content>"
        return bytesToSafeString(bytes, req.characterEncoding)
    }

    private fun extractResponseBody(res: ContentCachingResponseWrapper): String {
        val bytes = res.contentAsByteArray
        if (bytes.isEmpty()) return "-"
        if (!isLoggableContentType(res.contentType)) return "<non-text-content>"
        return bytesToSafeString(bytes, res.characterEncoding)
    }

    private fun bytesToSafeString(bytes: ByteArray, encoding: String?): String {
        val charset = runCatching { Charset.forName(encoding ?: "UTF-8") }.getOrDefault(Charsets.UTF_8)

        val limited = if (bytes.size > MAX_BODY_BYTES) bytes.copyOf(MAX_BODY_BYTES) else bytes
        val suffix = if (bytes.size > MAX_BODY_BYTES) "...<truncated ${bytes.size - MAX_BODY_BYTES} bytes>" else ""

        val text = runCatching { String(limited, charset) }.getOrElse { "<decode-error>" }

        val oneLine = text.replace("\r", "\\r").replace("\n", "\\n")

        return oneLine + suffix
    }

    private fun isLoggableContentType(contentType: String?): Boolean {
        val ct = contentType?.lowercase().orEmpty()
        return ct.contains("application/json") ||
                ct.contains("application/xml") ||
                ct.contains("text/") ||
                ct.contains("application/x-www-form-urlencoded")
    }

    private fun extractSafeHeaders(req: HttpServletRequest): Map<String, String> {
        val names = req.headerNames.toList()
        val result = linkedMapOf<String, String>()

        for (name in names) {
            val lower = name.lowercase()
            if (lower in SENSITIVE_HEADERS) {
                result[name] = "***"
            } else {
                // ограничим длину заголовка, чтобы не было мусора
                val v = req.getHeader(name) ?: ""
                result[name] = if (v.length > 256) v.take(256) + "...<truncated>" else v
            }
        }
        return result
    }
}

private fun <T> java.util.Enumeration<T>.toList(): List<T> =
    java.util.Collections.list(this)


private val log = LoggerFactory.getLogger(RestBodyLoggingFilter::class.java)

private const val MAX_BODY_BYTES = 8 * 1024

private val SENSITIVE_HEADERS = setOf(
    "authorization",
    "cookie",
    "set-cookie",
    "x-api-key"
)
