package com.acme.slamonitor.business.url

import java.net.URI

/**
 * Подменяет localhost/127.0.0.1 на указанный хост для доступа к сервисам вне кластера.
 */
class LocalhostUrlRewriter(
    private val replacementHost: String?
) {
    fun rewrite(url: String): String {
        val host = replacementHost?.trim().orEmpty()
        if (host.isBlank()) {
            return url
        }

        val uri = runCatching { URI(url) }.getOrNull() ?: return url
        val currentHost = uri.host ?: return url

        if (!currentHost.equals("localhost", ignoreCase = true) && currentHost != "127.0.0.1") {
            return url
        }

        return URI(
            uri.scheme,
            uri.userInfo,
            host,
            uri.port,
            uri.path,
            uri.query,
            uri.fragment
        ).toString()
    }
}
