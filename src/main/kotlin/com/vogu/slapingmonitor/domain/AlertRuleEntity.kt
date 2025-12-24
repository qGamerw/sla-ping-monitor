package com.vogu.slapingmonitor.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "alert_rules")
class AlertRuleEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endpoint_id", nullable = false)
    val endpoint: EndpointEntity,

    @Column(nullable = false)
    var type: String,

    @Column(nullable = false)
    var threshold: Double,

    @Column(name = "window_sec", nullable = false)
    var windowSec: Int,

    @Column(name = "trigger_for_sec", nullable = false)
    var triggerForSec: Int,

    @Column(name = "cooldown_sec", nullable = false)
    var cooldownSec: Int,

    @Column(name = "hysteresis_ratio", nullable = false)
    var hysteresisRatio: Double,

    @Column(nullable = false)
    var enabled: Boolean = true
)
