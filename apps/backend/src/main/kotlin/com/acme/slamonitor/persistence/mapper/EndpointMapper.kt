package com.acme.slamonitor.persistence.mapper

import com.acme.slamonitor.api.dto.EndpointResponse
import com.acme.slamonitor.persistence.domain.EndpointEntity
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper
interface EndpointMapper {

    fun toResponse(source: EndpointEntity): EndpointResponse

    fun toResponses(source: List<EndpointEntity>): List<EndpointResponse>

    companion object {
        val MAPPER: EndpointMapper = Mappers.getMapper(EndpointMapper::class.java)
    }
}
