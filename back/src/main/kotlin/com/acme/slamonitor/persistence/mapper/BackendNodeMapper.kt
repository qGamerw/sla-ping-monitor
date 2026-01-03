package com.acme.slamonitor.persistence.mapper

import com.acme.slamonitor.api.dto.response.BackendNodeResponse
import com.acme.slamonitor.persistence.domain.BackendNodeEntity
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

/**
 * Маппер сущности backend-ноды в DTO.
 */
@Mapper
interface BackendNodeMapper {

    /**
     * Конвертирует сущность в DTO.
     */
    fun toResponse(source: BackendNodeEntity): BackendNodeResponse

    /**
     * Экземпляр маппера MapStruct.
     */
    companion object {
        val MAPPER: BackendNodeMapper = Mappers.getMapper(BackendNodeMapper::class.java)
    }
}
