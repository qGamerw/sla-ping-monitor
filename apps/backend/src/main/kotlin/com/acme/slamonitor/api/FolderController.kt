package com.acme.slamonitor.api

import com.acme.slamonitor.api.dto.BaseResponse
import com.acme.slamonitor.api.dto.request.FolderRequest
import com.acme.slamonitor.business.service.FolderService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/folders")
class FolderController(
    private val folderService: FolderService
) {
    @PostMapping
    fun createFolder(@RequestBody request: FolderRequest) = BaseResponse(folderService.create(request))

    @PutMapping
    fun updateFolder(@RequestBody request: FolderRequest) = BaseResponse(folderService.update(request))

    @DeleteMapping
    fun deleteFolder(@RequestBody request: FolderRequest) = BaseResponse(folderService.delete(request))

    @GetMapping
    fun getFolders() = BaseResponse(folderService.getFolders())

}
