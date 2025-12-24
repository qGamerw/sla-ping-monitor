package com.vogu.slapingmonitor

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SlaPingMonitorApplication

/** Запускает Spring Boot приложение. */
fun main(args: Array<String>) {
    runApplication<SlaPingMonitorApplication>(*args)
}
