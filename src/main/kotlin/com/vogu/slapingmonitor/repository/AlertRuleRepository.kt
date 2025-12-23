package com.vogu.slapingmonitor.repository

import com.vogu.slapingmonitor.domain.AlertRuleEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AlertRuleRepository : JpaRepository<AlertRuleEntity, UUID> {
    fun findAllByEndpointId(endpointId: UUID): List<AlertRuleEntity>
}
