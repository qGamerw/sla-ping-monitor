package com.acme.slamonitor.api.validate

/**
 * Контракт для запуска набора валидаторов.
 */
interface ValidationPipeline<T> {

    /**
     * Выполняет валидацию запроса и выбрасывает исключение при ошибках.
     */
    fun validate(request: T)
}
