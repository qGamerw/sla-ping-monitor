package com.acme.slamonitor.persistence.util

import com.acme.slamonitor.aspect.annotation.TransactionalLogging

open class JpaIoTransactionalLogging {

    @TransactionalLogging
    open fun runConsumer(logMessage: String, block: () -> Unit) = block()

    @TransactionalLogging
    open fun <T> runSupplier(logMessage: String, block: () -> T): T = block()
}