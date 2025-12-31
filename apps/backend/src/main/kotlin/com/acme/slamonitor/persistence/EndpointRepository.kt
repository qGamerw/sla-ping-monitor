package com.acme.slamonitor.persistence

import com.acme.slamonitor.persistence.domain.EndpointEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface EndpointRepository : JpaRepository<EndpointEntity, UUID> {
    fun findAllByEnabledTrue(): List<EndpointEntity>

    @Query("select e.id from EndpointEntity e where e.enabled = true")
    fun findAllEnabledIds(): List<UUID>
}
