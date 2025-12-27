package com.acme.slamonitor.persistence

import com.acme.slamonitor.persistence.domain.EndpointEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EndpointRepository : JpaRepository<EndpointEntity, UUID>
