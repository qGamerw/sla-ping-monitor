package com.acme.slamonitor.configuration

import com.acme.slamonitor.api.dto.request.EndpointRequest
import com.acme.slamonitor.api.validate.BaseValidationPipeline
import com.acme.slamonitor.api.validate.ValidationPipeline
import com.acme.slamonitor.api.validate.impl.EndpointIntervalValidator
import com.acme.slamonitor.api.validate.impl.EndpointTimeoutValidator
import com.acme.slamonitor.api.validate.impl.EndpointUrlValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ValidationRequestConfig {

    @Bean
    fun endpointValidator(): ValidationPipeline<EndpointRequest> = BaseValidationPipeline(
        listOf(
            EndpointUrlValidator(),
            EndpointIntervalValidator(),
            EndpointTimeoutValidator()
        ),
        false
    )
}
