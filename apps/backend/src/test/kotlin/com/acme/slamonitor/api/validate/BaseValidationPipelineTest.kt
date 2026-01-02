package com.acme.slamonitor.api.validate

import com.acme.slamonitor.api.dto.ValidationError
import com.acme.slamonitor.exception.ValidationExecution
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("BaseValidationPipeline")
class BaseValidationPipelineTest {

    @Test
    fun shouldCollectAllErrorsWhenFailFastIsFalse() {
        val validatorA = object : Validator<String> {
            override fun validate(request: String): List<ValidationError> =
                listOf(ValidationError("a", "A", "error a"))
        }
        val validatorB = object : Validator<String> {
            override fun validate(request: String): List<ValidationError> =
                listOf(ValidationError("b", "B", "error b"))
        }
        val pipeline = BaseValidationPipeline(listOf(validatorA, validatorB), failFast = false)

        val exception = assertThrows(ValidationExecution::class.java) {
            pipeline.validate("request")
        }

        assertEquals(2, exception.error.size)
    }

    @Test
    fun shouldStopOnFirstErrorWhenFailFastIsTrue() {
        val validatorA = object : Validator<String> {
            override fun validate(request: String): List<ValidationError> =
                listOf(ValidationError("a", "A", "error a"))
        }
        val validatorB = object : Validator<String> {
            override fun validate(request: String): List<ValidationError> =
                listOf(ValidationError("b", "B", "error b"))
        }
        val pipeline = BaseValidationPipeline(listOf(validatorA, validatorB), failFast = true)

        val exception = assertThrows(ValidationExecution::class.java) {
            pipeline.validate("request")
        }

        assertEquals(1, exception.error.size)
    }
}
