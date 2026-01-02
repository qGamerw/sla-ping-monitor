package com.acme.slamonitor.business.service

import com.acme.slamonitor.api.dto.response.CheckResultResponse
import com.acme.slamonitor.api.dto.response.EndpointSummaryResponse
import com.acme.slamonitor.api.dto.response.StatsResponse
import java.time.Instant
import java.util.UUID

interface StatsService {

    fun getStats(endpointId: UUID, windowSec: Long, minSamples: Int? = 20): StatsResponse

    fun getChecks(endpointId: UUID, from: Instant, to: Instant): List<CheckResultResponse>

    fun getSummery(windowSec: Long): List<EndpointSummaryResponse>
}