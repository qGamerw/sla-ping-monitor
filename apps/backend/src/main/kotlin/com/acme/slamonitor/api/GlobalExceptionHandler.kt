package com.acme.slamonitor.api

import com.acme.slamonitor.api.dto.ApiError
import com.acme.slamonitor.api.dto.BaseResponse
import com.acme.slamonitor.exception.ValidationExecution
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handleAny(ex: Exception, request: HttpServletRequest): ResponseEntity<BaseResponse<Nothing>> {
        LOG.error("Unhandled exception: ${request.method} ${request.requestURI}")

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BaseResponse.failure(ApiError(ex.message)))
    }

    @ExceptionHandler(ValidationExecution::class)
    fun handleValidation(ex: ValidationExecution, request: HttpServletRequest): ResponseEntity<BaseResponse<Nothing>> {
        LOG.error("Unhandled exception: ${request.method} ${request.requestURI}")

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.failure(ApiError(ex.error)))
    }
}

private val LOG by lazy { LoggerFactory.getLogger(GlobalExceptionHandler::class.java) }
