package com.acme.slamonitor.persistence.mapper

import com.acme.slamonitor.api.dto.response.BackendNodeResponse
import com.acme.slamonitor.persistence.domain.BackendNodeEntity
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper
interface BackendNodeMapper {

    fun toResponse(source: BackendNodeEntity): BackendNodeResponse

    companion object {
        val MAPPER: BackendNodeMapper = Mappers.getMapper(BackendNodeMapper::class.java)
    }
}
