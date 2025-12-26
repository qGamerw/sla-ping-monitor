package com.acme.slamonitor.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.acme.slamonitor"])
class SlaPingMonitorApplication

/** Запускает Spring Boot приложение. */
fun main(args: Array<String>) {
    runApplication<SlaPingMonitorApplication>(*args)
}
