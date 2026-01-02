package com.acme.slamonitor.exception

/**
 * Базовое исключение домена мониторинга.
 */
open class SlamonitorException(
    message: String
) : RuntimeException(message)
