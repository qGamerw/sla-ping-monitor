package com.acme.slamonitor.api

import com.acme.slamonitor.api.dto.AlertEventRequest
import com.acme.slamonitor.business.service.AlertEventService
import com.acme.slamonitor.utils.BaseResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/alert-events")
class AlertEventController(
    private val alertEventServiceImpl: AlertEventService
) {
    /** Создаёт событие алерта и возвращает его данные. */
    @PostMapping
    fun create(@RequestBody request: AlertEventRequest) = BaseResponse(alertEventServiceImpl.create(request))

    /** Обновляет событие алерта по идентификатору. */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: AlertEventRequest
    ) = BaseResponse(alertEventServiceImpl.update(id, request))

    /** Возвращает список событий алертов, опционально по состоянию. */
    @GetMapping
    fun list(@RequestParam(required = false) state: String?) = BaseResponse(alertEventServiceImpl.list(state))
}
