package com.acme.slamonitor.aspect

import com.acme.slamonitor.aspect.annotation.TransactionalLogging
import kotlin.system.measureTimeMillis
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class TransactionalLoggingAspect {

    @Around("@annotation(ann)")
    fun around(pjp: ProceedingJoinPoint, ann: TransactionalLogging): Any? {
        if (!ann.enabled) return pjp.proceed()

        val logMessage = pjp.args.firstOrNull { it is String } as? String ?: ""

        LOG.info("Transactional: $logMessage is starting call")

        try {
            var result: Any? = null
            measureTimeMillis {
                result = pjp.proceed()
            }.also {
                LOG.info("Transactional: $logMessage is completed in $it ms")
            }
            return result
        } catch (ex: Throwable) {
            val elapsed = measureTimeMillis { }
            LOG.error("Transactional: $logMessage is failed: ${ex.message} ($elapsed ms)", ex)
            throw ex
        }
    }
}

private val LOG by lazy { LoggerFactory.getLogger(TransactionalLoggingAspect::class.java) }
