package com.acme.slamonitor.business.service.impl

import com.acme.slamonitor.api.dto.Message
import com.acme.slamonitor.api.dto.request.FolderRequest
import com.acme.slamonitor.api.dto.response.FolderResponse
import com.acme.slamonitor.business.service.FolderService
import com.acme.slamonitor.persistence.FolderRepository
import com.acme.slamonitor.persistence.domain.FolderEntity
import com.acme.slamonitor.persistence.mapper.FolderMapper.Companion.MAPPER
import com.acme.slamonitor.scope.JpaIoWorkerCoroutineDispatcher
import kotlinx.coroutines.runBlocking

/**
 * Реализация сервиса управления папками эндпоинтов.
 */
open class FolderServiceImpl(
    private val folderRepository: FolderRepository,
    private val jpaAsyncIoWorker: JpaIoWorkerCoroutineDispatcher
) : FolderService {

    override fun create(request: FolderRequest): Message {
        jpaAsyncIoWorker.executeWithTransactionalConsumer("Create folder: ${request.name}") {
            folderRepository.save(FolderEntity(name = request.name))
        }

        return Message("Folder has been added to the creation queue")
    }

    override fun update(request: FolderRequest): Message {
        val entity = runBlocking {
            jpaAsyncIoWorker.executeWithTransactionalSupplier("Find folder: ${request.name}") {
                folderRepository.findByName(request.name)
            }
        } ?: FolderEntity(name = request.name)

        request.endpoints.takeIf { !it.isNullOrEmpty() }?.also {
            entity.endpoints.clear()
            entity.endpoints.addAll(it)
        }

        request.newName.takeIf { !it.isNullOrBlank() }?.also {
            entity.name = it
        }

        jpaAsyncIoWorker.executeWithTransactionalConsumer("Update endpoint ${request.name}") {
            folderRepository.save(entity)
        }

        return Message("Folder with name ${request.name} has been added to the update queue")
    }

    override fun delete(request: FolderRequest): Message {
        val entity = runBlocking {
            jpaAsyncIoWorker.executeWithTransactionalSupplier("Find folder: ${request.name}") {
                folderRepository.findByName(request.name)
            }
        } ?: return Message("Folder not found")

        jpaAsyncIoWorker.executeWithTransactionalConsumer("Delete endpoint: ${request.name}") {
            folderRepository.delete(entity)
        }

        return Message("Deletion process is queued for folder with name: ${request.name}")
    }

    override fun getFolders(): List<FolderResponse> {
        val entities = runBlocking {
            jpaAsyncIoWorker.executeWithTransactionalSupplier("Get folders") {
                folderRepository.findAll()
            }
        }

        return MAPPER.toResponses(entities)
    }

}
