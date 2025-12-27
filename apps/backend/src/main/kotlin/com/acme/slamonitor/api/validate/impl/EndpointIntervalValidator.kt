package com.acme.slamonitor.api.validate.impl

import com.acme.slamonitor.api.dto.EndpointRequest
import com.acme.slamonitor.api.dto.ValidationError
import com.acme.slamonitor.api.validate.Validator

class EndpointIntervalValidator : Validator<EndpointRequest> {

    override fun validate(request: EndpointRequest): List<ValidationError> {
        val interval = request.intervalSec ?: return emptyList()
        if (interval < MIN_INTERVAL_SEC) {
            return listOf(
                ValidationError(
                    field = "intervalSec",
                    code = "INTERVAL_TOO_SMALL",
                    message = "field 'intervalSec' must be >= $MIN_INTERVAL_SEC"
                )
            )
        }
        return emptyList()
    }
}

private const val MIN_INTERVAL_SEC = 5
