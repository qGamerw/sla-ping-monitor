package com.acme.slamonitor.configuration

import com.acme.slamonitor.scope.AppScope
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DispatchersConfig {

    @Bean(destroyMethod = "close")
    fun virtualThreadExecutor(): ExecutorService =
        Executors.newVirtualThreadPerTaskExecutor()

    @Bean(name = [VIRTUAL_THREAD_DISPATCHER_BEAN_NAME], destroyMethod = "close")
    fun virtualThreadDispatcher(virtualThreadExecutor: ExecutorService): ExecutorCoroutineDispatcher =
        virtualThreadExecutor.asCoroutineDispatcher()

    @Bean(destroyMethod = "shutdown")
    fun schedulerDbExecutor(): ExecutorService = Executors.newVirtualThreadPerTaskExecutor()

    @Bean(name = [DB_VIRTUAL_THREAD_DISPATCHER_BEAN_NAME], destroyMethod = "close")
    fun schedulerDbDispatcher(virtualThreadExecutor: ExecutorService): ExecutorCoroutineDispatcher =
        virtualThreadExecutor.asCoroutineDispatcher()

    @Bean(destroyMethod = "close")
    fun appScope(): AppScope = AppScope()
}

const val VIRTUAL_THREAD_DISPATCHER_BEAN_NAME = "virtualThreadDispatcher"
const val DB_VIRTUAL_THREAD_DISPATCHER_BEAN_NAME = "dbVirtualThreadDispatcher"
