package com.vogu.slapingmonitor.api

import com.vogu.slapingmonitor.service.EndpointService
import com.vogu.slapingmonitor.service.StatsService
import com.vogu.slapingmonitor.repository.CheckResultRepository
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/endpoints")
class EndpointController(
    private val endpointService: EndpointService,
    private val statsService: StatsService,
    private val checkResultRepository: CheckResultRepository
) {
    @PostMapping
    fun create(@Valid @RequestBody request: EndpointRequest): EndpointResponse =
        endpointService.create(request).toResponse()

    @GetMapping
    fun list(): List<EndpointResponse> = endpointService.list().map { it.toResponse() }

    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): EndpointResponse = endpointService.get(id).toResponse()

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: EndpointRequest): EndpointResponse =
        endpointService.update(id, request).toResponse()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID) = endpointService.delete(id)

    @GetMapping("/{id}/stats")
    fun stats(
        @PathVariable id: UUID,
        @RequestParam windowSec: Long
    ): StatsResponse = statsService.getStats(id, windowSec)

    @GetMapping("/{id}/checks")
    fun checks(
        @PathVariable id: UUID,
        @RequestParam from: Instant,
        @RequestParam to: Instant
    ): List<CheckResultResponse> =
        checkResultRepository.findByEndpointIdAndWindow(id, from, to).map { it.toResponse() }

    @GetMapping("/summary")
    fun summary(@RequestParam windowSec: Long): List<EndpointSummaryResponse> =
        endpointService.list().map { endpoint ->
            val lastCheck = checkResultRepository.findTop1ByEndpointIdOrderByFinishedAtDesc(endpoint.id)
            EndpointSummaryResponse(
                id = endpoint.id,
                name = endpoint.name,
                url = endpoint.url,
                enabled = endpoint.enabled,
                lastCheckAt = lastCheck?.finishedAt,
                lastStatusCode = lastCheck?.statusCode,
                lastSuccess = lastCheck?.success,
                windowStats = statsService.getStats(endpoint.id, windowSec)
            )
        }
}
