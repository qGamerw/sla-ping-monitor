package com.vogu.slapingmonitor.repository

import com.vogu.slapingmonitor.domain.EndpointEntity
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository

interface EndpointRepository : JpaRepository<EndpointEntity, UUID>
