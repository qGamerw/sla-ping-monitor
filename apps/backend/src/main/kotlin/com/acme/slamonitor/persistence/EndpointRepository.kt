package com.acme.slamonitor.persistence

import com.acme.slamonitor.business.scheduler.dto.EndpointMeta
import com.acme.slamonitor.persistence.domain.EndpointEntity
import java.util.UUID
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * Репозиторий для эндпоинтов.
 */
interface EndpointRepository : JpaRepository<EndpointEntity, UUID> {

    /**
     * Возвращает метаданные эндпоинтов для планировщика.
     */
    @Query(
        """
        select e.id as id,
               e.version as version,
               e.enabled as enabled,
               e.intervalSec as intervalSec,
               e.updatedAt as updatedAt
        from EndpointEntity e
        order by e.id
    """
    )
    fun findAllMeta(pageable: Pageable): List<EndpointMeta>
}
