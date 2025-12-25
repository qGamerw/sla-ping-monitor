package com.slapingmonitor.repository

import com.slapingmonitor.repository.domain.AlertRuleEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface AlertRuleRepository : JpaRepository<AlertRuleEntity, UUID> {
    /** Возвращает правила алертов для endpoint. */
    fun findAllByEndpointId(endpointId: UUID): List<AlertRuleEntity>
}
