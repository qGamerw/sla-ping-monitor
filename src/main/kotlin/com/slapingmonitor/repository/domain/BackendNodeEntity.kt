package com.slapingmonitor.repository.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "backend_nodes")
class BackendNodeEntity(
    @Id
    @Column(name = "node_id")
    val nodeId: String,

    @Column(name = "base_url", nullable = false)
    var baseUrl: String,

    @Column(name = "started_at", nullable = false)
    var startedAt: Instant,

    @Column(name = "last_heartbeat_at", nullable = false)
    var lastHeartbeatAt: Instant,

    @JdbcTypeCode(SqlTypes.JSON)
    var meta: Map<String, Any>? = null
)
