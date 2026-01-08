package com.acme.slamonitor.business.service.dto

data class CpuSnapshot(
    val wallTimeNs: Long,
    val processCpuTimeNs: Long,
    val cores: Int
)
