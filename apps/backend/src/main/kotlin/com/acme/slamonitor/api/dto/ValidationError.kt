package com.acme.slamonitor.api.dto

data class ValidationError(
    val field: String?,
    val code: String,
    val message: String
)
