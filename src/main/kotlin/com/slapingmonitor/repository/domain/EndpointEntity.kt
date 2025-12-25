package com.slapingmonitor.repository.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "endpoints")
class EndpointEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false)
    var url: String,

    @Column(nullable = false)
    var method: String = "GET",

    @JdbcTypeCode(SqlTypes.JSON)
    var headers: Map<String, String>? = null,

    @Column(name = "timeout_ms", nullable = false)
    var timeoutMs: Int = 3000,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expected_status", nullable = false)
    var expectedStatus: List<Int> = listOf(200, 399),

    @Column(name = "interval_sec", nullable = false)
    var intervalSec: Int = 60,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @JdbcTypeCode(SqlTypes.JSON)
    var tags: List<String>? = null,

    @Column(name = "next_run_at")
    var nextRunAt: Instant? = null,

    @Column(name = "lease_owner")
    var leaseOwner: String? = null,

    @Column(name = "lease_until")
    var leaseUntil: Instant? = null,

    @Column(name = "created_at", nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant = Instant.now()
) {
    /** Заполняет служебные поля при создании записи. */
    @PrePersist
    fun onCreate() {
        val now = Instant.now()
        createdAt = now
        updatedAt = now
    }

    /** Обновляет время изменения при сохранении. */
    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}
