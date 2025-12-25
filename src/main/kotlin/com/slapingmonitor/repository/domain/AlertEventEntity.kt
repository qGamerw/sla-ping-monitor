package com.slapingmonitor.repository.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "alert_events")
class AlertEventEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", nullable = false)
    val rule: AlertRuleEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    val endpoint: EndpointEntity,

    @Column(nullable = false)
    var state: String,

    @Column(name = "opened_at", nullable = false)
    var openedAt: Instant,

    @Column(name = "resolved_at")
    var resolvedAt: Instant? = null,

    @Column(name = "last_notified_at")
    var lastNotifiedAt: Instant? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    var details: Map<String, Any>? = null
)
