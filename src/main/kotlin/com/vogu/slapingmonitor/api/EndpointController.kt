package com.vogu.slapingmonitor.api

import com.vogu.slapingmonitor.repository.CheckResultRepository
import com.vogu.slapingmonitor.service.EndpointService
import com.vogu.slapingmonitor.service.StatsService
import jakarta.validation.Valid
import java.time.Instant
import java.util.UUID
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/endpoints")
class EndpointController(
    private val endpointService: EndpointService,
    private val statsService: StatsService,
    private val checkResultRepository: CheckResultRepository
) {
    /** Создаёт endpoint и возвращает сохранённые данные. */
    @PostMapping
    fun create(@Valid @RequestBody request: EndpointRequest): EndpointResponse =
        endpointService.create(request).toResponse()

    /** Возвращает список всех endpoints. */
    @GetMapping
    fun list(): List<EndpointResponse> = endpointService.list().map { it.toResponse() }

    /** Возвращает endpoint по идентификатору. */
    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID): EndpointResponse = endpointService.get(id).toResponse()

    /** Обновляет endpoint по идентификатору. */
    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: EndpointRequest): EndpointResponse =
        endpointService.update(id, request).toResponse()

    /** Удаляет endpoint по идентификатору. */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID) = endpointService.delete(id)

    /** Возвращает статистику по окну времени для endpoint. */
    @GetMapping("/{id}/stats")
    fun stats(
        @PathVariable id: UUID,
        @RequestParam windowSec: Long
    ): StatsResponse = statsService.getStats(id, windowSec)

    /** Возвращает результаты проверок endpoint за интервал. */
    @GetMapping("/{id}/checks")
    fun checks(
        @PathVariable id: UUID,
        @RequestParam from: Instant,
        @RequestParam to: Instant
    ): List<CheckResultResponse> =
        checkResultRepository.findByEndpointIdAndWindow(id, from, to).map { it.toResponse() }

    /** Возвращает сводку по endpoints с расчётом статистики за окно. */
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
