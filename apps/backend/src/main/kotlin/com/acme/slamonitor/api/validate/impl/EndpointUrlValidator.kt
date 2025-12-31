package com.acme.slamonitor.api.validate.impl

import com.acme.slamonitor.api.dto.ValidationError
import com.acme.slamonitor.api.dto.request.EndpointRequest
import com.acme.slamonitor.api.validate.Validator
import java.net.URI

class EndpointUrlValidator : Validator<EndpointRequest> {

    override fun validate(request: EndpointRequest): List<ValidationError> {
        val url = request.url.trim()
        if (url.isEmpty()) {
            return listOf(
                ValidationError("url", "URL_BLANK", "field 'url' must not be blank")
            )
        }

        val uri = try {
            URI(url)
        } catch (_: Exception) {
            return listOf(
                ValidationError("url", "URL_INVALID", "field 'url' must be a valid URI")
            )
        }

        if (!uri.isAbsolute) {
            return listOf(
                ValidationError("url", "URL_NOT_ABSOLUTE", "field 'url' must be absolute, e.g. https://example.com")
            )
        }

        val scheme = uri.scheme?.lowercase()
        val errors = mutableListOf<ValidationError>()

        if (scheme !in ALLOWED_SCHEMES) {
            errors += ValidationError(
                "url",
                "URL_SCHEME_NOT_ALLOWED",
                "field 'url' scheme must be one of: ${ALLOWED_SCHEMES.joinToString()}"
            )
        }

        if (uri.host.isNullOrBlank()) {
            errors += ValidationError(
                "url",
                "URL_HOST_MISSING",
                "field 'url' must contain a host, e.g. https://example.com"
            )
        }

        return errors
    }
}

private val ALLOWED_SCHEMES = setOf("http", "https")
