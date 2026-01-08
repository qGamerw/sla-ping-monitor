package com.acme.slamonitor.utils

/**
 * Таймаут проверки по умолчанию (мс).
 */
const val DEFAULT_TIMEOUT_MS = 3000

/**
 * Интервал проверки по умолчанию (сек).
 */
const val DEFAULT_INTERVAL_SEC = 60

/**
 * Ожидаемые коды ответа по умолчанию.
 */
val DEFAULT_EXPECTED_STATUS = listOf(200, 399)
