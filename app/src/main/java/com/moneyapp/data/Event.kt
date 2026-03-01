package com.moneyapp.data

import java.util.UUID
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the real life event where spend happened (or credit in some cases).
 * Eg: Your friend makes a payment at a cafe, would have a event created at the same time
 * even though the settlement transaction happens later.
 */
@Entity(tableName = "events")
data class Event(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val amount: Long,  // not null, signed
    val description: String, // not null
    val fundId: String,
    val state: String, // open, resolved // linking is not related to this, this field denotes whether fund balance reflects this event.
    val groupId: String,
    val createdAt: Long = System.currentTimeMillis(),
    // val category: String, // fundId should be enough
    // val type: String,  // fundId is enough
)

/**
 * Linking of many Events together into a Story say for a group trip
 * Multiple events and multiple transactions all linked together but are together part of a Story
 * 
 * An embedded spendType and spendEntity which will map to dynamic objects
 * Say for paying for a friend the entityType can be SPEND_ON_FRIEND and
 * spendEntity will be details like how much for their part and which friend
 * For card it can be which card etc.
 * 
 * These are only thoughts but it can help when writing code to make it extensible with future requirements
 */