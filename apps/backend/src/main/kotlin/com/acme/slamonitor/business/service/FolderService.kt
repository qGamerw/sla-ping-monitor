package com.acme.slamonitor.business.service

import com.acme.slamonitor.api.dto.Message
import com.acme.slamonitor.api.dto.request.FolderRequest
import com.acme.slamonitor.api.dto.response.FolderResponse

interface FolderService {

    fun create(request: FolderRequest): Message

    fun update(request: FolderRequest): Message

    fun delete(request: FolderRequest): Message

    fun getFolders(): List<FolderResponse>

}
