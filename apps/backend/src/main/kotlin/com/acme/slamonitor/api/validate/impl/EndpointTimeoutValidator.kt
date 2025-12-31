package com.acme.slamonitor.api.validate.impl

import com.acme.slamonitor.api.dto.ValidationError
import com.acme.slamonitor.api.dto.request.EndpointRequest
import com.acme.slamonitor.api.validate.Validator

class EndpointTimeoutValidator : Validator<EndpointRequest> {

    override fun validate(request: EndpointRequest): List<ValidationError> {
        val timeout = request.timeoutMs ?: return emptyList()
        if (timeout !in MIN_TIMEOUT_MS..MAX_TIMEOUT_MS) {
            return listOf(
                ValidationError(
                    field = "timeoutMs",
                    code = "TIMEOUT_OUT_OF_RANGE",
                    message = "field 'timeoutMs' must be between $MIN_TIMEOUT_MS and $MAX_TIMEOUT_MS (ms)"
                )
            )
        }
        return emptyList()
    }
}

private const val MIN_TIMEOUT_MS = 5
private const val MAX_TIMEOUT_MS = 60_000
