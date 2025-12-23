package com.vogu.slapingmonitor.repository

import com.vogu.slapingmonitor.domain.CheckResultEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface CheckResultRepository : JpaRepository<CheckResultEntity, UUID> {
    @Query("""
        select cr from CheckResultEntity cr
        where cr.endpoint.id = :endpointId
        and cr.startedAt between :from and :to
        order by cr.startedAt asc
    """)
    fun findByEndpointIdAndWindow(
        @Param("endpointId") endpointId: UUID,
        @Param("from") from: Instant,
        @Param("to") to: Instant
    ): List<CheckResultEntity>

    fun findTop1ByEndpointIdOrderByFinishedAtDesc(endpointId: UUID): CheckResultEntity?
}
