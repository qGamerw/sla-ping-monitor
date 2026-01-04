package com.acme.slamonitor.api.dto.response

import java.util.UUID

data class FolderResponse(
    val id: UUID,
    val name: String,
    val folderIds: List<UUID>
)
