package com.acme.slamonitor.persistence

import com.acme.slamonitor.persistence.domain.EndpointEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface EndpointRepository : JpaRepository<EndpointEntity, UUID>
