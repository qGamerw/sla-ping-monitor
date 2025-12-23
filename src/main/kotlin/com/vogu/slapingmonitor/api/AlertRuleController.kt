package com.vogu.slapingmonitor.api

import com.vogu.slapingmonitor.service.AlertRuleService
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
import java.util.UUID

@RestController
@RequestMapping("/api/alert-rules")
class AlertRuleController(
    private val alertRuleService: AlertRuleService
) {
    @PostMapping
    fun create(@Valid @RequestBody request: AlertRuleRequest): AlertRuleResponse =
        alertRuleService.create(request).toResponse()

    @GetMapping
    fun list(@RequestParam(required = false) endpointId: UUID?): List<AlertRuleResponse> =
        alertRuleService.list(endpointId).map { it.toResponse() }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: AlertRuleRequest): AlertRuleResponse =
        alertRuleService.update(id, request).toResponse()

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID) = alertRuleService.delete(id)
}
