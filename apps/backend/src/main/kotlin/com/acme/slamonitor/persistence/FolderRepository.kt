package com.acme.slamonitor.persistence

import com.acme.slamonitor.persistence.domain.FolderEntity
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Репозиторий для отображения папок на фронте.
 */
interface FolderRepository : JpaRepository<FolderEntity, String>
