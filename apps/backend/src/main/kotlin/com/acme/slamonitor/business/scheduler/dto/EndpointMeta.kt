package com.acme.slamonitor.business.scheduler.dto

import java.util.UUID

interface EndpointMeta {
    fun getId(): UUID
    fun getVersion(): Long
    fun getEnabled(): Boolean
    fun getIntervalSec(): Int
}
