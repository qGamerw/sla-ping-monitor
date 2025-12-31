package com.acme.slamonitor.exception

import com.acme.slamonitor.api.dto.ValidationError

class ValidationExecution(
    val error: List<ValidationError>
) : SlamonitorException("Request validation failed")
