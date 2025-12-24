package com.vogu.slapingmonitor.repository

import com.vogu.slapingmonitor.domain.EndpointEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface EndpointRepository : JpaRepository<EndpointEntity, UUID>
