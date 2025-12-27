package com.acme.slamonitor.aspect

import com.acme.slamonitor.aspect.annotation.TransactionalLogging
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import kotlin.system.measureTimeMillis

@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
class TransactionalLoggingAspect {

    @Around("@annotation(ann)")
    fun around(pjp: ProceedingJoinPoint, ann: TransactionalLogging): Any? {
        if (!ann.enabled) return pjp.proceed()

        val logMessage = pjp.args.firstOrNull { it is String } as? String ?: ""

        LOG.info("$logMessage is starting call")

        var result: Any? = null
        try {
            measureTimeMillis {
                result = pjp.proceed()
            }.also {
                LOG.info("$logMessage is completed in $it ms")
            }
        } catch (ex: Throwable) {
            val elapsed = measureTimeMillis { }
            LOG.error("$logMessage is failed: ${ex.message} ($elapsed ms)", ex)
            throw ex
        }

        return result
    }
}

private val LOG by lazy { LoggerFactory.getLogger("com.acme.slamonitor.TransactionalLoggingAspect") }
