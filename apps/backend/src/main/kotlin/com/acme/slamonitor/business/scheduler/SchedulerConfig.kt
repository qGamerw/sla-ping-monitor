package com.acme.slamonitor.business.scheduler

import java.time.Duration

// todo Перенести в application
data class SchedulerConfig(
    val workQueueCapacity: Int = 512,
    val outboxCapacity: Int = 1024,
    val workers: Int = 8,

    val refreshPeriod: Duration = Duration.ofSeconds(60),
    val flushPeriod: Duration = Duration.ofMillis(250),
    val flushBatchSize: Int = 200,

    val prefetchHorizon: Duration = Duration.ofMinutes(2),
    val metaPageSize: Int = 1000
)
