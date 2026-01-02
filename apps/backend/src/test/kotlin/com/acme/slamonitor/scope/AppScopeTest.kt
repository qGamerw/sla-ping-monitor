package com.acme.slamonitor.scope

import kotlinx.coroutines.Job
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("AppScope")
class AppScopeTest {

    @Test
    fun shouldCancelJobOnClose() {
        val scope = AppScope()

        scope.close()

        val job = scope.coroutineContext[Job]
        assertTrue(job?.isCancelled == true)
    }
}
