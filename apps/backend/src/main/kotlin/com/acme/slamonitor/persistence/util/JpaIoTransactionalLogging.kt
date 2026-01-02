package com.acme.slamonitor.persistence.util

import com.acme.slamonitor.aspect.annotation.TransactionalLogging

/**
 * Обертка для выполнения JPA-операций с транзакционным логированием.
 */
open class JpaIoTransactionalLogging {

    /**
     * Выполняет блок без результата.
     */
    @TransactionalLogging
    open fun runConsumer(logMessage: String, block: () -> Unit) = block()

    /**
     * Выполняет блок с результатом.
     */
    @TransactionalLogging
    open fun <T> runSupplier(logMessage: String, block: () -> T): T = block()
}
