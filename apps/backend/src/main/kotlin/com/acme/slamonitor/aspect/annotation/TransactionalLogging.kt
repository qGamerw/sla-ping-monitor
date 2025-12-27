package com.acme.slamonitor.aspect.annotation

import org.springframework.transaction.annotation.Transactional

@Transactional
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TransactionalLogging(
    val enabled: Boolean = true
)
