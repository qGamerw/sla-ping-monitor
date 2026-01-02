package com.acme.slamonitor.post_processor

import com.acme.slamonitor.business.client.EndpointClient
import com.acme.slamonitor.business.client.impl.LoggingEndpointClient
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

@Order(Ordered.LOWEST_PRECEDENCE)
open class EndpointClientLoggingBeanPostProcessor : BeanPostProcessor {

    @Throws(BeansException::class)
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean is EndpointClient && bean !is LoggingEndpointClient) {
            return LoggingEndpointClient(bean)
        }
        return bean
    }
}