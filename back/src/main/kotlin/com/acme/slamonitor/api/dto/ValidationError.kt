package com.acme.slamonitor.api.dto

/**
 * Деталь ошибки валидации входных данных.
 */
data class ValidationError(
    val field: String?,
    val code: String,
    val message: String
)
