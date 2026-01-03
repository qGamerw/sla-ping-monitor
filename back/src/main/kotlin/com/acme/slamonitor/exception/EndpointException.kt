package com.acme.slamonitor.exception

/**
 * Ошибка, связанная с операциями над эндпоинтами.
 */
class EndpointException(
    message: String
) : SlamonitorException(message)
