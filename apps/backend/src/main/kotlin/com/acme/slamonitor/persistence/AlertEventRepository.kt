package com.acme.slamonitor.persistence

import com.acme.slamonitor.persistence.domain.AlertEventEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface AlertEventRepository : JpaRepository<AlertEventEntity, UUID> {
    /** Возвращает события алертов по состоянию. */
    fun findAllByState(state: String): List<AlertEventEntity>
}
