package com.acme.slamonitor.business.service.dto


data class HeapSnapshot(
    val usedBytes: Long,
    val maxBytes: Long,
    val committedBytes: Long
)
