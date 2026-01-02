package com.acme.slamonitor.persistence

import com.acme.slamonitor.persistence.domain.BackendNodeEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Репозиторий для backend-нод.
 */
interface BackendNodeRepository : JpaRepository<BackendNodeEntity, String>
