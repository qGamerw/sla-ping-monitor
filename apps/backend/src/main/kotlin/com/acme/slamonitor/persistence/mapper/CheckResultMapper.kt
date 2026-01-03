package com.acme.slamonitor.persistence.mapper

import com.acme.slamonitor.api.dto.response.CheckResultResponse
import com.acme.slamonitor.persistence.domain.CheckResultEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.factory.Mappers

/**
 * Маппер результата проверки в DTO.
 */
@Mapper
interface CheckResultMapper {

    /**
     * Конвертирует сущность в DTO.
     */
    @Mapping(source = "endpoint.id", target = "endpointId")
    fun toResponse(source: CheckResultEntity): CheckResultResponse

    /**
     * Конвертирует список сущностей в DTO.
     */
    fun toResponses(source: List<CheckResultEntity>): List<CheckResultResponse>

    /**
     * Экземпляр маппера MapStruct.
     */
    companion object {
        val MAPPER: CheckResultMapper = Mappers.getMapper(CheckResultMapper::class.java)
    }
}
