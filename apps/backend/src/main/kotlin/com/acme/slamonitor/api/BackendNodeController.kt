package com.acme.slamonitor.api

import com.acme.slamonitor.api.dto.BaseResponse
import com.acme.slamonitor.business.service.BackendNodeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST-контроллер для регистрации и мониторинга backend-нод.
 */
@RestController
@RequestMapping("/api/nodes")
class BackendNodeController(
    private val backendNodeService: BackendNodeService
) {

    @GetMapping
    fun heartbeat() = BaseResponse(backendNodeService.getNode())

}
