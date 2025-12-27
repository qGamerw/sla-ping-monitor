package com.acme.slamonitor.api.validate

import com.acme.slamonitor.api.dto.ValidationError
import com.acme.slamonitor.exception.ValidationExecution

class ValidationPipelineImpl<T>(
    private val validators: List<Validator<T>>,
    private val failFast: Boolean = false
) : ValidationPipeline<T> {

    override fun validate(request: T) {
        val errors = mutableListOf<ValidationError>()
        validators.forEach {
            val validationErrors = it.validate(request)
            if (validationErrors.isNotEmpty()) {
                errors.addAll(validationErrors)
                if (failFast) return@forEach
            }
        }

        if (errors.isNotEmpty()) {
            throw ValidationExecution(errors)
        }
    }
}
