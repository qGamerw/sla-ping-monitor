package com.acme.slamonitor.configuration

import com.acme.slamonitor.api.filter.HttpLoggingFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered

@Configuration
class HttpLoggingConfig {
    @Bean
    fun httpLoggingFilterRegistration(): FilterRegistrationBean<HttpLoggingFilter> =
        FilterRegistrationBean(HttpLoggingFilter()).apply {
            order = Ordered.HIGHEST_PRECEDENCE + 10
            addUrlPatterns("/*")
        }
}