package com.acme.slamonitor.persistence.mapper

import com.acme.slamonitor.api.dto.response.CheckResultResponse
import com.acme.slamonitor.persistence.domain.CheckResultEntity
import org.mapstruct.Mapper

@Mapper
interface CheckResultMapper {

    fun toResponse(source: CheckResultEntity): CheckResultResponse
}
