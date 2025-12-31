package com.acme.slamonitor.api

import com.acme.slamonitor.api.dto.EndpointRequest
import com.acme.slamonitor.api.dto.Message
import com.acme.slamonitor.api.validate.ValidationPipeline
import com.acme.slamonitor.business.service.EndpointService
import com.acme.slamonitor.business.service.impl.StatsService
import com.acme.slamonitor.persistence.CheckResultRepository
import com.acme.slamonitor.utils.BaseResponse
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
    private val checkResultRepository: CheckResultRepository,
    private val validator: ValidationPipeline<EndpointRequest>
) {
    /** Создаёт endpoint и возвращает сохранённые данные. */
    @PostMapping
    fun create(@RequestBody request: EndpointRequest): BaseResponse<Message> {
        validator.validate(request)
        return BaseResponse(endpointService.create(request))
    }

    /** Обновляет endpoint по идентификатору. */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: EndpointRequest
    ): BaseResponse<Message> {
        validator.validate(request)
        return BaseResponse(endpointService.update(id, request))
    }

    /** Удаляет endpoint по идентификатору. */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID) = BaseResponse(endpointService.deleteEndpoint(id))

    /** Возвращает endpoint по идентификатору. */
    @GetMapping("/{id}")
    fun getEndpoint(@PathVariable id: UUID) = BaseResponse(endpointService.getEndpoint(id))

    /** Возвращает список всех endpoints. */
    @GetMapping
    fun getEndpoints() = BaseResponse(endpointService.getEndpoints())

    /** Возвращает статистику по окну времени для endpoint. */
    @GetMapping("/{id}/stats")
    fun stats(
        @PathVariable id: UUID,
        @RequestParam windowSec: Long
    ) = BaseResponse(statsService.getStats(id, windowSec))

    /** Возвращает результаты проверок endpoint за интервал. */
    @GetMapping("/{id}/checks")
    fun checks(
        @PathVariable id: UUID,
        @RequestParam from: Instant,
        @RequestParam to: Instant
    ) = BaseResponse(checkResultRepository.findByEndpointIdAndWindow(id, from, to))

    /** Возвращает сводку по endpoints с расчётом статистики за окно. */
    @GetMapping("/summary")
    fun summary(@RequestParam windowSec: Long) = BaseResponse("TODO")
}
