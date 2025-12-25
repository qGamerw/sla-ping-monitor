package com.slapingmonitor.repository

import com.slapingmonitor.repository.domain.EndpointEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface EndpointRepository : JpaRepository<EndpointEntity, UUID>
