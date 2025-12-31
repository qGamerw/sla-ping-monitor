package com.acme.slamonitor.business.scheduler

import com.acme.slamonitor.business.scheduler.dto.EndpointResult
import com.acme.slamonitor.business.scheduler.dto.EndpointView

interface EndpointProcessor {
    suspend fun check(endpoint: EndpointView): EndpointResult
}
