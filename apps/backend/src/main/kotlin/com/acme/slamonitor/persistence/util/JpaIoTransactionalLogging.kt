package com.acme.slamonitor.persistence.util

import com.acme.slamonitor.aspect.annotation.TransactionalLogging
import org.springframework.stereotype.Service

@Service
class JpaIoTransactionalLogging {

    @TransactionalLogging
    fun runConsumer(logMessage: String, block: () -> Unit) = block()

    @TransactionalLogging
    fun <T> runSupplier(logMessage: String, block: () -> T): T = block()
}