package com.acme.slamonitor.persistence.mapper

import com.acme.slamonitor.api.dto.response.BackendNodeResponse
import com.acme.slamonitor.persistence.domain.BackendNodeEntity
import org.mapstruct.Mapper

@Mapper
interface BackendNodeMapper {

    fun toResponse(source: BackendNodeEntity): BackendNodeResponse
}
