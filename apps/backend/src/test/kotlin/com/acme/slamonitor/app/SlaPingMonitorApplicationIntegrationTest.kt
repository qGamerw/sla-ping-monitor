package com.acme.slamonitor.app

import com.acme.slamonitor.api.BackendNodeController
import com.acme.slamonitor.api.EndpointController
import com.acme.slamonitor.api.StatsController
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("SlaPingMonitorApplication integration")
class SlaPingMonitorApplicationIntegrationTest(
    private val applicationContext: ApplicationContext
) {

    @Test
    fun shouldLoadApplicationContext() {
        assertTrue(applicationContext.containsBean("endpointController"))
        assertTrue(applicationContext.containsBean("statsController"))
        assertTrue(applicationContext.containsBean("backendNodeController"))
    }

    @Test
    fun shouldExposeControllerBeans() {
        assertTrue(applicationContext.getBean(EndpointController::class.java) != null)
        assertTrue(applicationContext.getBean(StatsController::class.java) != null)
        assertTrue(applicationContext.getBean(BackendNodeController::class.java) != null)
    }
}
