package com.acme.slamonitor.business.scheduler.dto

import java.util.UUID

/**
 * Минимальные данные об эндпоинте для планировщика.
 */
interface EndpointMeta {
    /**
     * Идентификатор эндпоинта.
     */
    fun getId(): UUID

    /**
     * Версия записи для контроля конкурентных обновлений.
     */
    fun getVersion(): Long

    /**
     * Признак включенности эндпоинта.
     */
    fun getEnabled(): Boolean

    /**
     * Интервал проверки в секундах.
     */
    fun getIntervalSec(): Int
}
