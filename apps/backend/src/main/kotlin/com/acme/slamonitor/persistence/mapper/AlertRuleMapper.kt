package com.acme.slamonitor.persistence.mapper

import com.acme.slamonitor.api.dto.AlertRuleResponse
import com.acme.slamonitor.persistence.domain.AlertRuleEntity
import org.mapstruct.Mapper
import org.mapstruct.factory.Mappers

@Mapper
interface AlertRuleMapper {

    fun toResponse(source: AlertRuleEntity): AlertRuleResponse
    fun toEntity(source: AlertRuleResponse): AlertRuleEntity
    fun toResponse(source: List<AlertRuleEntity>): List<AlertRuleResponse>

    companion object {
        val MAPPER = Mappers.getMapper(AlertRuleMapper::class.java)
    }
}
