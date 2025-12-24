package com.vogu.slapingmonitor.api

import com.vogu.slapingmonitor.service.AlertEventService
import jakarta.validation.Valid
import java.util.UUID
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/alert-events")
class AlertEventController(
    private val alertEventService: AlertEventService
) {
    /** Создаёт событие алерта и возвращает его данные. */
    @PostMapping
    fun create(@Valid @RequestBody request: AlertEventRequest): AlertEventResponse =
        alertEventService.create(request).toResponse()

    /** Обновляет событие алерта по идентификатору. */
    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: AlertEventRequest): AlertEventResponse =
        alertEventService.update(id, request).toResponse()

    /** Возвращает список событий алертов, опционально по состоянию. */
    @GetMapping
    fun list(@RequestParam(required = false) state: String?): List<AlertEventResponse> =
        alertEventService.list(state).map { it.toResponse() }
}
