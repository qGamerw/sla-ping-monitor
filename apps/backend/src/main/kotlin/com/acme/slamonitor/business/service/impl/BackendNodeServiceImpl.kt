package com.acme.slamonitor.business.service.impl

import com.acme.slamonitor.api.dto.response.BackendNodeResponse
import com.acme.slamonitor.business.service.BackendNodeService
import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory

open class BackendNodeServiceImpl : BackendNodeService {

    override fun getNode(): BackendNodeResponse {
        val os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean::class.java)

        val proc = os.processCpuLoad
            .takeIf { it >= 0.0 }
            ?.times(5000.0) ?: 0.0

        return BackendNodeResponse(
            cpuUsed = "%.2f".format(proc),
            ramUsed = "%.2f".format(heapUsage())
        )
    }

}

private fun heapUsage(): Double {
    val heap = ManagementFactory.getMemoryMXBean().heapMemoryUsage
    val max = heap.max
    return if (max > 0) heap.used.toDouble() / max * 100.0 else 0.0
}
