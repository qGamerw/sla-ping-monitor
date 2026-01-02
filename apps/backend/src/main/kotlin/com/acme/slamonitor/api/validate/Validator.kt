package com.acme.slamonitor.api.validate

import com.acme.slamonitor.api.dto.ValidationError

/**
 * Контракт для валидации запроса и возврата списка ошибок.
 */
interface Validator<T> {

    /**
     * Возвращает список ошибок валидации, либо пустой список при успехе.
     */
    fun validate(request: T): List<ValidationError>
}
