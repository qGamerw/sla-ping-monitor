package com.vogu.slapingmonitor.repository

import com.vogu.slapingmonitor.domain.AlertEventEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AlertEventRepository : JpaRepository<AlertEventEntity, UUID> {
    /** Возвращает события алертов по состоянию. */
    fun findAllByState(state: String): List<AlertEventEntity>
}
