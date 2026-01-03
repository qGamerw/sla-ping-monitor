package com.acme.slamonitor.persistence.mapper

import com.acme.slamonitor.api.dto.response.EndpointResponse
import com.acme.slamonitor.persistence.domain.EndpointEntity
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

/**
 * Маппер сущности эндпоинта в DTO.
 */
@Mapper
interface EndpointMapper {

    /**
     * Конвертирует сущность в DTO.
     */
    fun toResponse(source: EndpointEntity): EndpointResponse

    /**
     * Конвертирует список сущностей в DTO.
     */
    fun toResponses(source: List<EndpointEntity>): List<EndpointResponse>

    /**
     * Экземпляр маппера MapStruct.
     */
    companion object {
        val MAPPER: EndpointMapper = Mappers.getMapper(EndpointMapper::class.java)
    }
}
