package com.acme.slamonitor.persistence.mapper

import com.acme.slamonitor.api.dto.response.FolderResponse
import com.acme.slamonitor.persistence.domain.FolderEntity
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

/**
 * Маппер сущности папок в DTO.
 */
@Mapper
interface FolderMapper {

    /**
     * Конвертирует сущность в DTO.
     */
    fun toResponse(source: FolderEntity): FolderResponse

    /**
     * Конвертирует список сущностей в DTO.
     */
    fun toResponses(source: List<FolderEntity>): List<FolderResponse>

    /**
     * Экземпляр маппера MapStruct.
     */
    companion object {
        val MAPPER: FolderMapper = Mappers.getMapper(FolderMapper::class.java)
    }
}
