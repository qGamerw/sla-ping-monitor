package com.acme.slamonitor.business.service.impl

import com.acme.slamonitor.api.dto.response.BackendNodeResponse
import com.acme.slamonitor.business.service.BackendNodeService
import com.acme.slamonitor.business.service.dto.CpuSnapshot
import com.acme.slamonitor.business.service.dto.HeapSnapshot
import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory

/**
 * Реализация сервиса метрик текущего backend-узла.
 */
open class BackendNodeServiceImpl : BackendNodeService {

    override fun getNode(): BackendNodeResponse {
        val cpu = readProcessCpuPercent(500)

        val proc = if (cpu == null) "JVM process CPU: n/a"
        else "JVM process CPU: %.1f%%".format(cpu)

        val heapPct = readHeapPercent()
        val snap = takeHeapSnapshot()

        val maxOrCommitted = if (snap.maxBytes > 0) snap.maxBytes else snap.committedBytes

        val heapUsed = if (heapPct == null) "Heap: n/a"
        else "Heap: %.1f%% (used=%dMB, cap=%dMB)".format(
            heapPct,
            snap.usedBytes / (1024 * 1024),
            maxOrCommitted / (1024 * 1024)
        )

        return BackendNodeResponse(
            cpuUsed = proc,
            ramUsed = heapUsed
        )
    }

}

/**
 * Делает 2 снимка с паузой и возвращает процент.
 */
fun readProcessCpuPercent(intervalMs: Long = 500): Double? {
    val s1 = takeCpuSnapshot() ?: return null
    Thread.sleep(intervalMs)
    val s2 = takeCpuSnapshot() ?: return null
    return calcProcessCpuPercent(s1, s2)
}

/**
 * Снимок для расчёта CPU текущего JVM-процесса.
 */
fun takeCpuSnapshot(): CpuSnapshot? {
    val os = ManagementFactory.getOperatingSystemMXBean() as? OperatingSystemMXBean ?: return null
    val cpu = os.processCpuTime
    if (cpu < 0) return null
    return CpuSnapshot(
        wallTimeNs = System.nanoTime(),
        processCpuTimeNs = cpu,
        cores = os.availableProcessors.coerceAtLeast(1)
    )
}

/**
 * CPU usage JVM-процесса в процентах (0..100) между двумя снимками.
 * Возвращает null если снимки некорректны.
 */
fun calcProcessCpuPercent(prev: CpuSnapshot, next: CpuSnapshot): Double? {
    val wallDelta = next.wallTimeNs - prev.wallTimeNs
    val cpuDelta = next.processCpuTimeNs - prev.processCpuTimeNs
    if (wallDelta <= 0 || cpuDelta < 0) return null

    val cores = next.cores.coerceAtLeast(1)
    val load = (cpuDelta.toDouble() / wallDelta.toDouble()) / cores.toDouble()
    return (load * 100.0).coerceIn(0.0, 100.0)
}

/** Удобная функция: сразу возвращает heap %. */
fun readHeapPercent(): Double? = calcHeapPercent(takeHeapSnapshot())

/**
 * Снимок для расчёта Heap текущего JVM-процесса.
 */
fun takeHeapSnapshot(): HeapSnapshot {
    val heap = ManagementFactory.getMemoryMXBean().heapMemoryUsage
    return HeapSnapshot(
        usedBytes = heap.used,
        maxBytes = heap.max,
        committedBytes = heap.committed
    )
}

/**
 * Heap usage в процентах.
 * По умолчанию считаем от max (если известен), иначе от committed.
 */
fun calcHeapPercent(snapshot: HeapSnapshot): Double? {
    val denom = when {
        snapshot.maxBytes > 0 -> snapshot.maxBytes
        snapshot.committedBytes > 0 -> snapshot.committedBytes
        else -> return null
    }
    val percent = snapshot.usedBytes.toDouble() / denom.toDouble() * 100.0
    return percent.coerceIn(0.0, 100.0)
}
