package com.acme.slamonitor.api.validate

import com.acme.slamonitor.api.dto.ValidationError
import com.acme.slamonitor.exception.ValidationExecution
import org.slf4j.LoggerFactory

// todo По приколу переделать на аннотации
/**
 * Базовая реализация конвейера валидации.
 */
class BaseValidationPipeline<T>(
    private val validators: List<Validator<T>>,
    private val failFast: Boolean = false
) : ValidationPipeline<T> {

    /**
     * Выполняет валидации и выбрасывает исключение при ошибках.
     */
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
            LOG.error("Validation failed: ${errors.joinToString("\n")}")
            throw ValidationExecution(errors)
        }
    }
}

private val LOG by lazy { LoggerFactory.getLogger(BaseValidationPipeline::class.java) }
