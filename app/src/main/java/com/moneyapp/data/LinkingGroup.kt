package com.moneyapp.data

import java.util.UUID
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "linking_groups")
data class LinkingGroup(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val linkedAt: Long = System.currentTimeMillis(),
    val notes: String? = null
)
