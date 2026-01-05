package com.acme.slamonitor.persistence.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

/**
 * JPA-сущность результата проверки эндпоинта.
 */
@Entity
@Table(name = "check_results")
class CheckResultEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    val endpoint: EndpointEntity,

    @Column(name = "started_at", nullable = false)
    val startedAt: Instant,

    @Column(name = "finished_at", nullable = false)
    val finishedAt: Instant,

    @Column(name = "latency_ms", nullable = false)
    val latencyMs: Int,

    @Column(name = "status_code")
    val statusCode: Int? = null,

    @Column(nullable = false)
    val success: Boolean,

    @Column(name = "error_type")
    val errorType: String? = null,

    @Column(name = "error_message")
    val errorMessage: String? = null
)
