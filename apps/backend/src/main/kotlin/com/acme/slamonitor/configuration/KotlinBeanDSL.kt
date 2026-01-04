package com.acme.slamonitor.configuration

import com.acme.slamonitor.api.BackendNodeController
import com.acme.slamonitor.api.EndpointController
import com.acme.slamonitor.api.FolderController
import com.acme.slamonitor.api.StatsController
import com.acme.slamonitor.api.dto.request.EndpointRequest
import com.acme.slamonitor.api.filter.HttpLoggingFilter
import com.acme.slamonitor.api.handler.GlobalExceptionHandler
import com.acme.slamonitor.api.validate.BaseValidationPipeline
import com.acme.slamonitor.api.validate.ValidationPipeline
import com.acme.slamonitor.api.validate.impl.EndpointIntervalValidator
import com.acme.slamonitor.api.validate.impl.EndpointTimeoutValidator
import com.acme.slamonitor.api.validate.impl.EndpointUrlValidator
import com.acme.slamonitor.aspect.TransactionalLoggingAspect
import com.acme.slamonitor.business.client.EndpointClient
import com.acme.slamonitor.business.client.impl.KtorEndpointClient
import com.acme.slamonitor.business.scheduler.EndpointProcessor
import com.acme.slamonitor.business.scheduler.InMemoryScheduler
import com.acme.slamonitor.business.scheduler.SchedulerSmartLifecycle
import com.acme.slamonitor.business.scheduler.impl.EndpointProcessorImpl
import com.acme.slamonitor.business.service.BackendNodeService
import com.acme.slamonitor.business.service.EndpointService
import com.acme.slamonitor.business.service.FolderService
import com.acme.slamonitor.business.service.StatsService
import com.acme.slamonitor.business.service.impl.BackendNodeServiceImpl
import com.acme.slamonitor.business.service.impl.EndpointServiceImpl
import com.acme.slamonitor.business.service.impl.FolderServiceImpl
import com.acme.slamonitor.business.service.impl.StatsServiceImpl
import com.acme.slamonitor.persistence.BackendNodeRepository
import com.acme.slamonitor.persistence.CheckResultRepository
import com.acme.slamonitor.persistence.EndpointRepository
import com.acme.slamonitor.persistence.FolderRepository
import com.acme.slamonitor.persistence.util.JpaIoTransactionalLogging
import com.acme.slamonitor.post_processor.EndpointClientLoggingBeanPostProcessor
import com.acme.slamonitor.scope.AppScope
import com.acme.slamonitor.scope.JpaIoWorkerCoroutineDispatcher
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.cio.endpoint
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.serialization.json.Json
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.SmartLifecycle
import org.springframework.context.support.beans
import org.springframework.core.Ordered
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

val controllerBeans = beans {
    bean(::GlobalExceptionHandler)
    bean(::EndpointController)
    bean(::StatsController)
    bean(::BackendNodeController)
    bean(::FolderController)

    bean<FilterRegistrationBean<HttpLoggingFilter>> {
        FilterRegistrationBean(HttpLoggingFilter()).apply {
            order = Ordered.HIGHEST_PRECEDENCE + 10
            addUrlPatterns("/*")
        }
    }

    bean<ValidationPipeline<EndpointRequest>> {
        BaseValidationPipeline(
            validators = listOf(
                EndpointUrlValidator(),
                EndpointIntervalValidator(),
                EndpointTimeoutValidator()
            ),
            failFast = false
        )
    }
}

val corsMvcBeans = beans {
    bean<WebMvcConfigurer> {
        object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
            }
        }
    }
}

val ktorBeans = beans {
    bean<HttpClient>(destroyMethodName = "close") {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }

            install(HttpTimeout)
            install(Logging) { level = LogLevel.NONE }

            engine {
                maxConnectionsCount = 1_000
                endpoint {
                    maxConnectionsPerRoute = 100
                    pipelineMaxSize = 20
                    keepAliveTime = 5_000
                    connectTimeout = 5_000
                    connectAttempts = 2
                }
                requestTimeout = 0
            }
        }
    }
}

val beanPostProcessor = beans {
    bean(::EndpointClientLoggingBeanPostProcessor)
}

val serviceBeans = beans {
    bean<BackendNodeService> {
        BackendNodeServiceImpl(
            backendNodeRepository = ref<BackendNodeRepository>()
        )
    }

    bean<EndpointService> {
        EndpointServiceImpl(
            endpointRepository = ref<EndpointRepository>(),
            jpaAsyncIoWorker = ref<JpaIoWorkerCoroutineDispatcher>()
        )
    }

    bean<StatsService> {
        StatsServiceImpl(
            checkResultRepository = ref<CheckResultRepository>(),
            endpointRepository = ref<EndpointRepository>()
        )
    }

    bean<FolderService> {
        FolderServiceImpl(
            folderRepository = ref<FolderRepository>(),
            jpaAsyncIoWorker = ref<JpaIoWorkerCoroutineDispatcher>()
        )
    }

    bean<SmartLifecycle> {
        SchedulerSmartLifecycle(
            scheduler = ref(),
            rootDispatcher = ref<CoroutineDispatcher>(ROOT_THREAD_DISPATCHER_BEAN_NAME),
            refreshLoopDispatcher = ref<CoroutineDispatcher>(REFRESH_LOOP_THREAD_DISPATCHER_BEAN_NAME),
            feedLoopDispatcher = ref<CoroutineDispatcher>(FEED_LOOP_THREAD_DISPATCHER_BEAN_NAME),
            workerDispatcher = ref<CoroutineDispatcher>(WORKER_THREAD_DISPATCHER_BEAN_NAME),
            flusherLoopDispatcher = ref<CoroutineDispatcher>(FLUSHER_LOOP_THREAD_DISPATCHER_BEAN_NAME),
            cleanDataLoopDispatcher = ref<CoroutineDispatcher>(CLEAN_DATA_LOOP_THREAD_DISPATCHER_BEAN_NAME),
            appScope = ref()
        )
    }

    bean<InMemoryScheduler> {
        InMemoryScheduler(
            endpointRepository = ref<EndpointRepository>(),
            checkResultRepository = ref<CheckResultRepository>(),
            jdbcTemplate = ref<JdbcTemplate>(),
            processor = ref<EndpointProcessor>(),
            jpaAsyncIoWorker = ref<JpaIoWorkerCoroutineDispatcher>()
        )
    }

    bean<EndpointClient> {
        KtorEndpointClient(
            http = ref(),
            httpDispatcher = ref<CoroutineDispatcher>(HTTP_THREAD_DISPATCHER_BEAN_NAME)
        )
    }

    bean<EndpointProcessor> {
        EndpointProcessorImpl(
            client = ref<EndpointClient>()
        )
    }

    bean<JpaIoTransactionalLogging> {
        JpaIoTransactionalLogging()
    }

    bean<JpaIoWorkerCoroutineDispatcher> {
        JpaIoWorkerCoroutineDispatcher(
            appScope = ref(),
            dispatcher = ref<CoroutineDispatcher>(DB_THREAD_DISPATCHER_BEAN_NAME),
            jpaWorkerLogging = ref()
        )
    }
}

val aspectBean = beans {
    bean(::TransactionalLoggingAspect)
}

val dispatchersBeans = beans {
    bean<ExecutorService>(
        name = ROOT_THREAD_EXECUTOR_BEAN_NAME,
        destroyMethodName = "shutdown"
    ) { virtualPerTaskExecutor("sched-root-") }

    bean<ExecutorService>(
        name = DB_THREAD_EXECUTOR_BEAN_NAME,
        destroyMethodName = "shutdown"
    ) { virtualPerTaskExecutor("sched-db-") }

    bean<ExecutorService>(
        name = REFRESH_LOOP_THREAD_EXECUTOR_BEAN_NAME,
        destroyMethodName = "shutdown"
    ) { virtualPerTaskExecutor("sched-refresh-") }

    bean<ExecutorService>(
        name = FEED_LOOP_EXECUTOR_BEAN_NAME,
        destroyMethodName = "shutdown"
    ) { virtualPerTaskExecutor("sched-feed-") }

    bean<ExecutorService>(
        name = FLUSHER_LOOP_THREAD_EXECUTOR_BEAN_NAME,
        destroyMethodName = "shutdown"
    ) { virtualPerTaskExecutor("sched-flusher-") }

    bean<ExecutorService>(
        name = CLEAN_DATA_LOOP_THREAD_EXECUTOR_BEAN_NAME,
        destroyMethodName = "shutdown"
    ) { virtualPerTaskExecutor("sched-clean-data-") }

    bean<ExecutorService>(
        name = WORKER_THREAD_EXECUTOR_BEAN_NAME,
        destroyMethodName = "shutdown"
    ) { virtualPerTaskExecutor("sched-worker-") }

    bean<ExecutorService>(
        name = HTTP_THREAD_EXECUTOR_BEAN_NAME,
        destroyMethodName = "shutdown"
    ) { virtualPerTaskExecutor("sched-http-") }

    bean<ExecutorCoroutineDispatcher>(
        name = ROOT_THREAD_DISPATCHER_BEAN_NAME,
        destroyMethodName = "close"
    ) { ref<ExecutorService>(ROOT_THREAD_EXECUTOR_BEAN_NAME).asCoroutineDispatcher() }

    bean<ExecutorCoroutineDispatcher>(
        name = DB_THREAD_DISPATCHER_BEAN_NAME,
        destroyMethodName = "close"
    ) { ref<ExecutorService>(DB_THREAD_EXECUTOR_BEAN_NAME).asCoroutineDispatcher() }

    bean<ExecutorCoroutineDispatcher>(
        name = REFRESH_LOOP_THREAD_DISPATCHER_BEAN_NAME,
        destroyMethodName = "close"
    ) { ref<ExecutorService>(REFRESH_LOOP_THREAD_EXECUTOR_BEAN_NAME).asCoroutineDispatcher() }

    bean<ExecutorCoroutineDispatcher>(
        name = FEED_LOOP_THREAD_DISPATCHER_BEAN_NAME,
        destroyMethodName = "close"
    ) { ref<ExecutorService>(FEED_LOOP_EXECUTOR_BEAN_NAME).asCoroutineDispatcher() }

    bean<ExecutorCoroutineDispatcher>(
        name = FLUSHER_LOOP_THREAD_DISPATCHER_BEAN_NAME,
        destroyMethodName = "close"
    ) { ref<ExecutorService>(FLUSHER_LOOP_THREAD_EXECUTOR_BEAN_NAME).asCoroutineDispatcher() }

    bean<ExecutorCoroutineDispatcher>(
        name = CLEAN_DATA_LOOP_THREAD_DISPATCHER_BEAN_NAME,
        destroyMethodName = "close"
    ) { ref<ExecutorService>(CLEAN_DATA_LOOP_THREAD_EXECUTOR_BEAN_NAME).asCoroutineDispatcher() }

    bean<ExecutorCoroutineDispatcher>(
        name = WORKER_THREAD_DISPATCHER_BEAN_NAME,
        destroyMethodName = "close"
    ) { ref<ExecutorService>(WORKER_THREAD_EXECUTOR_BEAN_NAME).asCoroutineDispatcher() }

    bean<ExecutorCoroutineDispatcher>(
        name = HTTP_THREAD_DISPATCHER_BEAN_NAME,
        destroyMethodName = "close"
    ) { ref<ExecutorService>(HTTP_THREAD_EXECUTOR_BEAN_NAME).asCoroutineDispatcher() }

    bean<AppScope>(destroyMethodName = "close") { AppScope() }
}

/**
 * Создает виртуальный executor с указанным префиксом потоков.
 */
private fun virtualPerTaskExecutor(prefix: String): ExecutorService {
    val factory: ThreadFactory = Thread.ofVirtual()
        .name(prefix, 1)
        .factory()

    return Executors.newThreadPerTaskExecutor(factory)
}

private const val ROOT_THREAD_EXECUTOR_BEAN_NAME = "rootThreadExecutor"
private const val DB_THREAD_EXECUTOR_BEAN_NAME = "dbThreadExecutor"
private const val REFRESH_LOOP_THREAD_EXECUTOR_BEAN_NAME = "refreshLoopThreadExecutor"
private const val FEED_LOOP_EXECUTOR_BEAN_NAME = "feedLoopThreadExecutor"
private const val FLUSHER_LOOP_THREAD_EXECUTOR_BEAN_NAME = "flusherLoopThreadExecutor"
private const val CLEAN_DATA_LOOP_THREAD_EXECUTOR_BEAN_NAME = "cleanDataLoopThreadExecutor"
private const val WORKER_THREAD_EXECUTOR_BEAN_NAME = "workerThreadExecutor"
private const val HTTP_THREAD_EXECUTOR_BEAN_NAME = "httpThreadExecutor"

const val ROOT_THREAD_DISPATCHER_BEAN_NAME = "rootThreadDispatcher"
const val DB_THREAD_DISPATCHER_BEAN_NAME = "dbThreadDispatcher"
const val REFRESH_LOOP_THREAD_DISPATCHER_BEAN_NAME = "refreshLoopThreadDispatcher"
const val FEED_LOOP_THREAD_DISPATCHER_BEAN_NAME = "feedLoopThreadDispatcher"
const val FLUSHER_LOOP_THREAD_DISPATCHER_BEAN_NAME = "flusherLoopThreadDispatcher"
const val CLEAN_DATA_LOOP_THREAD_DISPATCHER_BEAN_NAME = "cleanDataLoopThreadDispatcher"
const val WORKER_THREAD_DISPATCHER_BEAN_NAME = "workerThreadDispatcher"
const val HTTP_THREAD_DISPATCHER_BEAN_NAME = "httpThreadDispatcher"
