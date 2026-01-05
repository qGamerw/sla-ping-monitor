package com.acme.slamonitor.persistence.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "folders")
class FolderEntity(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "name", nullable = false, unique = true)
    var name: String,

    @Column(name = "endpoints")
    val endpoints: MutableList<UUID> = mutableListOf()
)