package com.acme.slamonitor.api.dto

/**
 * Унифицированный ответ API с телом или ошибкой.
 */
class BaseResponse<Rq>(
    val susses: Boolean,
    val body: Rq? = null,
    val error: ApiError? = null
) {
    /**
     * Упрощенный конструктор успешного ответа.
     */
    constructor(body: Rq) : this(true, body, null)

    /**
     * Фабрики для успешных и ошибочных ответов.
     */
    companion object {
        /**
         * Создает успешный ответ с телом.
         */
        fun <Rq> success(body: Rq): BaseResponse<Rq> = BaseResponse(true, body, null)

        /**
         * Создает ответ с ошибкой.
         */
        fun failure(error: ApiError): BaseResponse<Nothing> = BaseResponse(false, null, error)
    }
}
