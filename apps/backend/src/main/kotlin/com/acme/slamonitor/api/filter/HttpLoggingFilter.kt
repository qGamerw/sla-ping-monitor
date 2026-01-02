package com.acme.slamonitor.api.filter

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.nio.charset.Charset
import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper

/**
 * Логирует HTTP-запросы и ответы с безопасной обработкой тела.
 */
class HttpLoggingFilter(
    private val maxPayloadChars: Int = 8_192,
    private val includeHeaders: Boolean = true
) : OncePerRequestFilter() {

    /**
     * Исключает служебные пути из логирования.
     */
    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val p = request.requestURI ?: return false
        return p.startsWith("/actuator") ||
                p.startsWith("/swagger") ||
                p.startsWith("/v3/api-docs") ||
                p.startsWith("/favicon")
    }

    /**
     * Оборачивает запрос/ответ и пишет лог после обработки.
     */
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val startedNs = System.nanoTime()

        val wrappedRequest = ContentCachingRequestWrapper(request)
        val wrappedResponse = ContentCachingResponseWrapper(response)

        try {
            filterChain.doFilter(wrappedRequest, wrappedResponse)
        } finally {
            val tookMs = (System.nanoTime() - startedNs) / 1_000_000

            val method = request.method
            val uri = buildString {
                append(request.requestURI)
                if (!request.queryString.isNullOrBlank()) append('?').append(request.queryString)
            }

            val status = wrappedResponse.status

            val reqHeaders = if (includeHeaders) extractRequestHeaders(request) else emptyMap()
            val respHeaders = if (includeHeaders) extractResponseHeaders(wrappedResponse) else emptyMap()

            val reqBody = safeBodyFrom(
                bytes = wrappedRequest.contentAsByteArray,
                encoding = request.characterEncoding,
                contentType = request.contentType
            )

            val respBody = safeBodyFrom(
                bytes = wrappedResponse.contentAsByteArray,
                encoding = response.characterEncoding,
                contentType = response.contentType
            )

            LOG.info(
                "\nHTTP $method $uri -> $status ($tookMs ms){}{}{}{}",
                if (includeHeaders) "\n reqHeaders=$reqHeaders" else "",
                if (reqBody.isNotBlank()) "\n reqBody=${truncate(reqBody)}" else "",
                if (includeHeaders) "\n respHeaders=$respHeaders" else "",
                if (respBody.isNotBlank()) "\n respBody=${truncate(respBody)}" else ""
            )

            // иначе клиенту уйдёт пустой body
            wrappedResponse.copyBodyToResponse()
        }
    }

    /**
     * Читает заголовки запроса в map.
     */
    private fun extractRequestHeaders(request: HttpServletRequest): Map<String, String> {
        val names = request.headerNames ?: return emptyMap()
        val map = LinkedHashMap<String, String>()
        while (names.hasMoreElements()) {
            val name = names.nextElement()
            map[name] = request.getHeader(name)
        }
        return map
    }

    /**
     * Читает заголовки ответа в map.
     */
    private fun extractResponseHeaders(response: HttpServletResponse): Map<String, String> =
        response.headerNames.associateWith { response.getHeader(it) }

    /**
     * Безопасно превращает тело в строку или возвращает заглушку.
     */
    private fun safeBodyFrom(bytes: ByteArray, encoding: String?, contentType: String?): String {
        if (bytes.isEmpty()) return ""

        // Чтобы не убить память/логи бинарщиной
        val ct = (contentType ?: "").lowercase()
        val isTextLike = ct.startsWith("text/") ||
                ct.contains("json") ||
                ct.contains("xml") ||
                ct.contains("form-urlencoded")

        if (!isTextLike) return "<skipped: content-type=$contentType, bytes=${bytes.size}>"

        val cs = runCatching {
            if (StringUtils.hasText(encoding)) Charset.forName(encoding) else Charsets.UTF_8
        }.getOrDefault(Charsets.UTF_8)

        return runCatching { String(bytes, cs) }
            .getOrElse { "<unreadable body: ${it::class.simpleName}>" }
    }

    /**
     * Обрезает строку до максимального размера.
     */
    private fun truncate(s: String): String =
        if (s.length <= maxPayloadChars) s else s.take(maxPayloadChars) + "…(truncated)"
}

private val LOG by lazy { LoggerFactory.getLogger(HttpLoggingFilter::class.java) }
