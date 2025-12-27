package com.acme.slamonitor.utils

class BaseResponse<Rq>(
    val susses: Boolean,
    val body: Rq? = null,
    val error: ApiError? = null
) {

    constructor(body: Rq) : this(true, body, null)

    companion object {
        fun <Rq> success(body: Rq): BaseResponse<Rq> = BaseResponse(true, body, null)
        fun failure(error: ApiError): BaseResponse<Nothing> = BaseResponse(false, null, error)
    }
}

data class ApiError(
    val message: Any?
)
