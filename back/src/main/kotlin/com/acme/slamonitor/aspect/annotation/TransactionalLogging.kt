package com.acme.slamonitor.aspect.annotation

import org.springframework.transaction.annotation.Transactional

/**
 * Включает логирование транзакций вокруг помеченного метода.
 */
@Transactional
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TransactionalLogging(
    val enabled: Boolean = true
)
