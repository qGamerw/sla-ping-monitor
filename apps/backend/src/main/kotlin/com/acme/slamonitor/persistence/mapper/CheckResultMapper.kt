package com.acme.slamonitor.persistence.mapper

import com.acme.slamonitor.api.dto.response.CheckResultResponse
import com.acme.slamonitor.persistence.domain.CheckResultEntity
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper
interface CheckResultMapper {

    fun toResponse(source: CheckResultEntity): CheckResultResponse

    companion object {
        val MAPPER: CheckResultMapper = Mappers.getMapper(CheckResultMapper::class.java)
    }
}
