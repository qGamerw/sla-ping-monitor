package com.acme.slamonitor.api

import com.acme.slamonitor.api.dto.BaseResponse
import com.acme.slamonitor.api.dto.Message
import com.acme.slamonitor.api.dto.request.EndpointRequest
import com.acme.slamonitor.api.validate.ValidationPipeline
import com.acme.slamonitor.business.service.EndpointService
import java.util.UUID
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST-контроллер для управления эндпоинтами мониторинга.
 */
@RestController
@RequestMapping("/api/endpoints")
class EndpointController(
    private val endpointService: EndpointService,
    private val validator: ValidationPipeline<EndpointRequest>
) {

    /**
     * Создает новый эндпоинт.
     */
    @PostMapping
    fun createEndpoint(@RequestBody request: EndpointRequest): BaseResponse<Message> {
        validator.validate(request)
        return BaseResponse(endpointService.create(request))
    }

    /**
     * Обновляет существующий эндпоинт.
     */
    @PutMapping("/{id}")
    fun updateEndpoint(
        @PathVariable id: UUID,
        @RequestBody request: EndpointRequest
    ): BaseResponse<Message> {
        validator.validate(request)
        return BaseResponse(endpointService.update(id, request))
    }

    /**
     * Удаляет эндпоинт по идентификатору.
     */
    @DeleteMapping("/{id}")
    fun deleteEndpoint(@PathVariable id: UUID) = BaseResponse(endpointService.deleteEndpoint(id))

    /**
     * Возвращает эндпоинт по идентификатору.
     */
    @GetMapping("/{id}")
    fun getEndpoint(@PathVariable id: UUID) = BaseResponse(endpointService.getEndpoint(id))

    /**
     * Возвращает список эндпоинтов.
     */
    @GetMapping
    fun getEndpoints() = BaseResponse(endpointService.getEndpoints())

}
