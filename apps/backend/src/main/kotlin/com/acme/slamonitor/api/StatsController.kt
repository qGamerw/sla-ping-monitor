package com.acme.slamonitor.api

import com.acme.slamonitor.api.dto.BaseResponse
import com.acme.slamonitor.business.service.StatsService
import java.time.Instant
import java.util.UUID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/endpoints")
class StatsController(
    private val statsService: StatsService
) {

    @GetMapping("/{id}/stats")
    fun stats(
        @PathVariable id: UUID,
        @RequestParam windowSec: Long
    ) = BaseResponse(statsService.getStats(id, windowSec))

    @GetMapping("/{id}/checks")
    fun checks(
        @PathVariable id: UUID,
        @RequestParam from: Instant,
        @RequestParam to: Instant
    ) = BaseResponse(statsService.getChecks(id, from, to))

    @GetMapping("/summary")
    fun summary(@RequestParam windowSec: Long) = BaseResponse(statsService.getSummery(windowSec))
}