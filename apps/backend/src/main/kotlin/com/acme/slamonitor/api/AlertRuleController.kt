package com.acme.slamonitor.api

import com.acme.slamonitor.api.dto.AlertRuleRequest
import com.acme.slamonitor.business.service.AlertRuleService
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
import java.util.UUID

@RestController
@RequestMapping("/api/alert-rules")
class AlertRuleController(
    private val alertRuleServiceImpl: AlertRuleService
) {
    /** Создаёт правило алерта и возвращает его данные. */
    @PostMapping
    fun create(@RequestBody request: AlertRuleRequest) = BaseResponse(alertRuleServiceImpl.create(request))

    /** Возвращает список правил алертов, опционально по endpoint. */
    @GetMapping
    fun list(@RequestParam(required = false) endpointId: UUID?) = BaseResponse(alertRuleServiceImpl.list(endpointId))

    /** Обновляет правило алерта по идентификатору. */
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: AlertRuleRequest
    ) = BaseResponse(alertRuleServiceImpl.update(id, request))

    /** Удаляет правило алерта по идентификатору. */
    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID) = BaseResponse(alertRuleServiceImpl.delete(id))
}
