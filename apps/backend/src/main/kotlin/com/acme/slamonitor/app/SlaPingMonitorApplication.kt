package com.acme.slamonitor.app

import com.acme.slamonitor.configuration.aspectBean
import com.acme.slamonitor.configuration.beanPostProcessor
import com.acme.slamonitor.configuration.controllerBeans
import com.acme.slamonitor.configuration.dispatchersBeans
import com.acme.slamonitor.configuration.ktorBeans
import com.acme.slamonitor.configuration.serviceBeans
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Точка конфигурации Spring Boot приложения.
 */
@EnableJpaRepositories(basePackages = ["com.acme.slamonitor.persistence"])
@EntityScan(basePackages = ["com.acme.slamonitor.persistence"])
@SpringBootApplication
class SlaPingMonitorApplication

/**
 * Точка входа приложения.
 */
fun main(args: Array<String>) {
    runApplication<SlaPingMonitorApplication>(*args) {
        addInitializers(
            ktorBeans,
            controllerBeans,
            dispatchersBeans,
            serviceBeans,
            beanPostProcessor,
            aspectBean
        )
    }
}
