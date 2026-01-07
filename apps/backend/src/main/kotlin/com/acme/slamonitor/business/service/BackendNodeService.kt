package com.acme.slamonitor.business.service

import com.acme.slamonitor.api.dto.response.BackendNodeResponse

/**
 * Сервис управления backend-нодами.
 */
interface BackendNodeService {

    /**
     * Обновляет информацию о ноде по heartbeat.
     */
    fun getNode(): BackendNodeResponse

}
