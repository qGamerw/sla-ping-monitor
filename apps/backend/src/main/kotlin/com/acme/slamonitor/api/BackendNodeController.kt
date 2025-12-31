package com.acme.slamonitor.api

import com.acme.slamonitor.api.dto.NodeHeartbeatRequest
import com.acme.slamonitor.business.service.impl.BackendNodeService
import com.acme.slamonitor.utils.BaseResponse
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
    fun heartbeat(@RequestBody request: NodeHeartbeatRequest) = BaseResponse(backendNodeService.heartbeat(request))

    /** Возвращает список активных backend-нод. */
    @GetMapping
    fun getNodes() = BaseResponse(backendNodeService.list())

    /** Возвращает заглушку метрик для backend-ноды. */
    @GetMapping("/{nodeId}/metrics")
    fun metrics(@PathVariable nodeId: String): Map<String, Any> = mapOf(
        "nodeId" to nodeId,
        "queueSize" to 0,
        "activeChecks" to 0,
        "checksPerMin" to 0
    )
}
