package com.acme.slamonitor.configuration

import com.acme.slamonitor.scope.AppScope
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DispatchersConfig {

    @Bean(name = [ROOT_THREAD_EXECUTOR_BEAN_NAME], destroyMethod = "shutdown")
    fun schedulerRootExecutor(): ExecutorService = Executors.newVirtualThreadPerTaskExecutor()

    @Bean(name = [DB_THREAD_EXECUTOR_BEAN_NAME], destroyMethod = "shutdown")
    fun schedulerDbExecutor(): ExecutorService = Executors.newVirtualThreadPerTaskExecutor()

    @Bean(name = [REFRESH_LOOP_THREAD_EXECUTOR_BEAN_NAME], destroyMethod = "shutdown")
    fun schedulerRefreshLoopExecutor(): ExecutorService = Executors.newVirtualThreadPerTaskExecutor()

    @Bean(name = [FEED_LOOP_EXECUTOR_BEAN_NAME], destroyMethod = "shutdown")
    fun schedulerFeedLoopExecutor(): ExecutorService = Executors.newVirtualThreadPerTaskExecutor()

    @Bean(name = [FLUSHER_LOOP_THREAD_EXECUTOR_BEAN_NAME], destroyMethod = "shutdown")
    fun schedulerFlusherLoopExecutor(): ExecutorService = Executors.newVirtualThreadPerTaskExecutor()

    @Bean(name = [WORKER_THREAD_EXECUTOR_BEAN_NAME], destroyMethod = "shutdown")
    fun schedulerWorkerExecutor(): ExecutorService = Executors.newVirtualThreadPerTaskExecutor()

    @Bean(name = [HTTP_THREAD_EXECUTOR_BEAN_NAME], destroyMethod = "shutdown")
    fun schedulerHttpExecutor(): ExecutorService = Executors.newVirtualThreadPerTaskExecutor()

    @Bean(name = [ROOT_THREAD_DISPATCHER_BEAN_NAME], destroyMethod = "close")
    fun schedulerRootDispatcher(
        @Qualifier(ROOT_THREAD_EXECUTOR_BEAN_NAME)
        executorService: ExecutorService
    ): ExecutorCoroutineDispatcher = executorService.asCoroutineDispatcher()

    @Bean(name = [DB_THREAD_DISPATCHER_BEAN_NAME], destroyMethod = "close")
    fun schedulerDbDispatcher(
        @Qualifier(DB_THREAD_EXECUTOR_BEAN_NAME)
        executorService: ExecutorService
    ): ExecutorCoroutineDispatcher = executorService.asCoroutineDispatcher()

    @Bean(name = [REFRESH_LOOP_THREAD_DISPATCHER_BEAN_NAME], destroyMethod = "close")
    fun schedulerRefreshLoopDispatcher(
        @Qualifier(REFRESH_LOOP_THREAD_EXECUTOR_BEAN_NAME)
        executorService: ExecutorService
    ): ExecutorCoroutineDispatcher = executorService.asCoroutineDispatcher()

    @Bean(name = [FEED_LOOP_THREAD_DISPATCHER_BEAN_NAME], destroyMethod = "close")
    fun schedulerFeedLoopDispatcher(
        @Qualifier(FEED_LOOP_EXECUTOR_BEAN_NAME)
        executorService: ExecutorService
    ): ExecutorCoroutineDispatcher = executorService.asCoroutineDispatcher()

    @Bean(name = [FLUSHER_LOOP_THREAD_DISPATCHER_BEAN_NAME], destroyMethod = "close")
    fun schedulerFlusherLoopDispatcher(
        @Qualifier(FLUSHER_LOOP_THREAD_EXECUTOR_BEAN_NAME)
        executorService: ExecutorService
    ): ExecutorCoroutineDispatcher = executorService.asCoroutineDispatcher()

    @Bean(name = [WORKER_THREAD_DISPATCHER_BEAN_NAME], destroyMethod = "close")
    fun schedulerWorkerDispatcher(
        @Qualifier(WORKER_THREAD_EXECUTOR_BEAN_NAME)
        executorService: ExecutorService
    ): ExecutorCoroutineDispatcher = executorService.asCoroutineDispatcher()

    @Bean(name = [HTTP_THREAD_DISPATCHER_BEAN_NAME], destroyMethod = "close")
    fun schedulerHttpDispatcher(
        @Qualifier(HTTP_THREAD_EXECUTOR_BEAN_NAME)
        executorService: ExecutorService
    ): ExecutorCoroutineDispatcher = executorService.asCoroutineDispatcher()

    @Bean(destroyMethod = "close")
    fun appScope(): AppScope = AppScope()
}

private const val ROOT_THREAD_EXECUTOR_BEAN_NAME = "rootThreadExecutor"
private const val DB_THREAD_EXECUTOR_BEAN_NAME = "dbThreadExecutor"
private const val REFRESH_LOOP_THREAD_EXECUTOR_BEAN_NAME = "refreshLoopThreadExecutor"
private const val FEED_LOOP_EXECUTOR_BEAN_NAME = "feedLoopThreadExecutor"
private const val FLUSHER_LOOP_THREAD_EXECUTOR_BEAN_NAME = "flusherLoopThreadExecutor"
private const val WORKER_THREAD_EXECUTOR_BEAN_NAME = "workerThreadExecutor"
private const val HTTP_THREAD_EXECUTOR_BEAN_NAME = "httpThreadExecutor"

const val ROOT_THREAD_DISPATCHER_BEAN_NAME = "rootThreadDispatcher"
const val DB_THREAD_DISPATCHER_BEAN_NAME = "dbThreadDispatcher"
const val REFRESH_LOOP_THREAD_DISPATCHER_BEAN_NAME = "refreshLoopThreadDispatcher"
const val FEED_LOOP_THREAD_DISPATCHER_BEAN_NAME = "feedLoopThreadDispatcher"
const val FLUSHER_LOOP_THREAD_DISPATCHER_BEAN_NAME = "flusherLoopThreadDispatcher"
const val WORKER_THREAD_DISPATCHER_BEAN_NAME = "workerThreadDispatcher"
const val HTTP_THREAD_DISPATCHER_BEAN_NAME = "httpThreadDispatcher"
