package com.slapingmonitor.repository

import com.slapingmonitor.repository.domain.BackendNodeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface BackendNodeRepository : JpaRepository<BackendNodeEntity, String>
