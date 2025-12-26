package com.acme.slamonitor.persistence

import com.acme.slamonitor.persistence.domain.AlertRuleEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface AlertRuleRepository : JpaRepository<AlertRuleEntity, UUID> {
    /** Возвращает правила алертов для endpoint. */
    fun findAllByEndpointId(endpointId: UUID): List<AlertRuleEntity>
}
