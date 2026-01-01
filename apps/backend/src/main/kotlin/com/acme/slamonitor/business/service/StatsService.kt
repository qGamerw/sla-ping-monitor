package com.acme.slamonitor.business.service

import com.acme.slamonitor.api.dto.response.StatsResponse
import java.util.UUID

interface StatsService {

    fun getStats(endpointId: UUID, windowSec: Long, minSamples: Int? = 20): StatsResponse
    fun calculateStats(latencies: List<Int>, errorCount: Int, lastStatus: Int?, minSamples: Int): StatsResponse
}