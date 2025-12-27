package com.acme.slamonitor.persistence.mapper

import com.acme.slamonitor.api.dto.AlertEventResponse
import com.acme.slamonitor.persistence.domain.AlertEventEntity
import org.mapstruct.factory.Mappers

interface AlertEventMapper {

    fun toResponse(source: AlertEventEntity): AlertEventResponse

    fun toResponse(source: List<AlertEventEntity>): List<AlertEventResponse>

    companion object {
        val MAPPER = Mappers.getMapper(AlertEventMapper::class.java)
    }
}


