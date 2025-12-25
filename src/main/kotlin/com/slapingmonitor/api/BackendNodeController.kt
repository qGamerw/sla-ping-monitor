package com.slapingmonitor.api

import com.slapingmonitor.repository.mapper.BackendNodeResponse
import com.slapingmonitor.repository.mapper.NodeHeartbeatRequest
import com.slapingmonitor.repository.mapper.toResponse
import com.slapingmonitor.service.BackendNodeService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/nodes")
class BackendNodeController(
    private val backendNodeService: BackendNodeService
) {
    /** Принимает heartbeat от backend-ноды и обновляет её данные. */
    @PostMapping("/heartbeat")
    fun heartbeat(@Valid @RequestBody request: NodeHeartbeatRequest): BackendNodeResponse =
        backendNodeService.heartbeat(request).toResponse()

    /** Возвращает список активных backend-нод. */
    @GetMapping
    fun list(): List<BackendNodeResponse> = backendNodeService.list().map { it.toResponse() }

    /** Возвращает заглушку метрик для backend-ноды. */
    @GetMapping("/{nodeId}/metrics")
    fun metrics(@PathVariable nodeId: String): Map<String, Any> = mapOf(
        "nodeId" to nodeId,
        "queueSize" to 0,
        "activeChecks" to 0,
        "checksPerMin" to 0
    )
}
