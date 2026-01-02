package com.acme.slamonitor.persistence

import com.acme.slamonitor.persistence.domain.CheckResultEntity
import java.time.Instant
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CheckResultRepository : JpaRepository<CheckResultEntity, UUID> {
    @Query(
        """
        select cr
        from CheckResultEntity cr
        where cr.endpoint.id = :endpointId
          and cr.startedAt between :from and :to
        order by cr.startedAt asc
        """
    )
    fun findByEndpointIdAndWindow(
        @Param("endpointId") endpointId: UUID,
        @Param("from") from: Instant,
        @Param("to") to: Instant
    ): List<CheckResultEntity>

    @Query(
        """
        select cr
        from CheckResultEntity cr
        where cr.startedAt between :from and :to
        order by cr.startedAt asc
        """
    )
    fun findByWindow(
        @Param("from") from: Instant,
        @Param("to") to: Instant
    ): List<CheckResultEntity>

    fun findTopByEndpoint_IdOrderByFinishedAtDesc(id: UUID): CheckResultEntity?

}
