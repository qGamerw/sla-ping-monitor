package com.acme.slamonitor.api.dto.request

import java.util.UUID

/**
 * Запрос на создание/обновление/удаление папки.
 */
data class FolderRequest(
    val name: String,
    val endpoints: List<UUID>? = null,
    val newName: String? = null,
)
