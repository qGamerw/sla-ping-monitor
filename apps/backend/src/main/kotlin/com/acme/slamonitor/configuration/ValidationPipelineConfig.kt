package com.acme.slamonitor.configuration

import com.acme.slamonitor.api.dto.EndpointRequest
import com.acme.slamonitor.api.validate.ValidationPipeline
import com.acme.slamonitor.api.validate.ValidationPipelineImpl
import com.acme.slamonitor.api.validate.impl.EndpointIntervalValidator
import com.acme.slamonitor.api.validate.impl.EndpointTimeoutValidator
import com.acme.slamonitor.api.validate.impl.EndpointUrlValidator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ValidationPipelineConfig {

    @Bean
    fun endpointValidator(): ValidationPipeline<EndpointRequest> = ValidationPipelineImpl(
        listOf(
            EndpointUrlValidator(),
            EndpointIntervalValidator(),
            EndpointTimeoutValidator()
        ),
        false
    )
}
