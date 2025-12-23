package com.vogu.slapingmonitor.repository

import com.vogu.slapingmonitor.domain.BackendNodeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface BackendNodeRepository : JpaRepository<BackendNodeEntity, String>
