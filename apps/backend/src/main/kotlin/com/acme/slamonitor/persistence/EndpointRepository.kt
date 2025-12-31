package com.acme.slamonitor.persistence

import com.acme.slamonitor.business.scheduler.dto.EndpointMeta
import com.acme.slamonitor.persistence.domain.EndpointEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

import java.util.UUID

interface EndpointRepository : JpaRepository<EndpointEntity, UUID> {
    fun findAllByEnabledTrue(): List<EndpointEntity>

    @Query("select e.id from EndpointEntity e where e.enabled = true")
    fun findAllEnabledIds(): List<UUID>

    @Query("""
        select e.id as id,
               e.version as version,
               e.enabled as enabled,
               e.intervalSec as intervalSec,
               e.updatedAt as updatedAt
        from EndpointEntity e
        order by e.id
    """)
    fun findAllMeta(pageable: Pageable): List<EndpointMeta>
}
