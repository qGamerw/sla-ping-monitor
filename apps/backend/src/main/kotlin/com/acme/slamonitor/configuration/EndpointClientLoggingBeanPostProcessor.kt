package com.acme.slamonitor.configuration

import com.acme.slamonitor.bussneis.client.EndpointClient
import com.acme.slamonitor.bussneis.client.impl.LoggingEndpointClient
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
class EndpointClientLoggingBeanPostProcessor : BeanPostProcessor {

    @Throws(BeansException::class)
    override fun postProcessAfterInitialization(bean: Any, beanName: String): Any {
        if (bean is EndpointClient && bean !is LoggingEndpointClient) {
            return LoggingEndpointClient(bean, beanName)
        }
        return bean
    }
}
