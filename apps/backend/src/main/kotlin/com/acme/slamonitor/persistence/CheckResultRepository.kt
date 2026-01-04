package com.acme.slamonitor.persistence

import com.acme.slamonitor.persistence.domain.CheckResultEntity
import java.time.Instant
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

/**
 * Репозиторий для результатов проверок.
 */
interface CheckResultRepository : JpaRepository<CheckResultEntity, UUID> {
    /**
     * Возвращает проверки эндпоинта за период.
     */
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
            /**
             * Возвращает проверки за период по всем эндпоинтам.
             */
    fun findByWindow(
        @Param("from") from: Instant,
        @Param("to") to: Instant
    ): List<CheckResultEntity>

    /**
     * Возвращает последний результат проверки эндпоинта.
     */
    fun findTopByEndpointIdOrderByFinishedAtDesc(id: UUID): CheckResultEntity?

    @Modifying(clearAutomatically = true)
    @Query(
        value = """
        DELETE FROM check_results
        WHERE id IN (
            SELECT id
            FROM check_results
            WHERE started_at < (now() - interval '1 day')
            ORDER BY started_at
            LIMIT 100
        )
        """,
        nativeQuery = true
    )
    fun deleteExpiredBatch(): Int
}
