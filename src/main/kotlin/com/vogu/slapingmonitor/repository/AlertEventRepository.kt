package com.vogu.slapingmonitor.repository

import com.vogu.slapingmonitor.domain.AlertEventEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface AlertEventRepository : JpaRepository<AlertEventEntity, UUID> {
    /** Возвращает события алертов по состоянию. */
    fun findAllByState(state: String): List<AlertEventEntity>
}
