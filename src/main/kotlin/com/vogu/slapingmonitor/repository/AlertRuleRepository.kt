package com.vogu.slapingmonitor.repository

import com.vogu.slapingmonitor.domain.AlertRuleEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface AlertRuleRepository : JpaRepository<AlertRuleEntity, UUID> {
    fun findAllByEndpointId(endpointId: UUID): List<AlertRuleEntity>
}
