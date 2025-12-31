package com.acme.slamonitor.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

// todo Переделать на kotlin BEAN DSL
@SpringBootApplication(scanBasePackages = ["com.acme.slamonitor"])
@EnableJpaRepositories(basePackages = ["com.acme.slamonitor.persistence"])
@EntityScan(basePackages = ["com.acme.slamonitor.persistence"])
class SlaPingMonitorApplication

/** Запускает Spring Boot приложение. */
fun main(args: Array<String>) {
    runApplication<SlaPingMonitorApplication>(*args)
}
