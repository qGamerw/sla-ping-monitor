package com.acme.slamonitor.aspect

import com.acme.slamonitor.aspect.annotation.TransactionalLogging
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
        val logMessage = pjp.args.firstOrNull { it is String } as? String ?: ""
        LOG.info("Transactional: $logMessage is starting call")

        if (!ann.enabled) return pjp.proceed()

        val startNs = System.nanoTime()
        try {
            return pjp.proceed().also {
                val elapsedMs = (System.nanoTime() - startNs) / 1_000_000
                LOG.info("Transactional: $logMessage is completed in $elapsedMs ms")
            }
        } catch (ex: Throwable) {
            throw ex.also {
                val elapsed = (System.nanoTime() - startNs) / 1_000_000
                LOG.error("Transactional: $logMessage is failed: ${ex.message} ($elapsed ms)", ex)
            }
        }
    }
}

private val LOG by lazy { LoggerFactory.getLogger(TransactionalLoggingAspect::class.java) }
