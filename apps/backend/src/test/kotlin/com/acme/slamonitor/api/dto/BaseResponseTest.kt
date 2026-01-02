package com.acme.slamonitor.api.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("BaseResponse")
class BaseResponseTest {

    @Test
    fun shouldCreateSuccessResponse() {
        val response = BaseResponse.success("ok")

        assertEquals(true, response.susses)
        assertEquals("ok", response.body)
    }

    @Test
    fun shouldCreateFailureResponse() {
        val error = ApiError("boom")
        val response = BaseResponse.failure(error)

        assertEquals(false, response.susses)
        assertEquals(error, response.error)
    }
}
