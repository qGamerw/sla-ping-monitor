package com.acme.slamonitor.exception

import com.acme.slamonitor.api.dto.ValidationError

/**
 * Исключение при ошибках валидации запроса.
 */
class ValidationExecution(
    val error: List<ValidationError>
) : SlamonitorException("Request validation failed")
