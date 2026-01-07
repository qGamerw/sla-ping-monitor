package com.acme.slamonitor.api.dto.response

import java.util.UUID

/**
 * Ответ с данными папки эндпоинтов.
 */
data class FolderResponse(
    val id: UUID,
    val name: String,
    val endpoints: List<UUID>?
)
