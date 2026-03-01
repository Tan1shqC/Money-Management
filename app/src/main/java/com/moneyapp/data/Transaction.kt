package com.moneyapp.data

import java.util.UUID
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val amount: Long,  // signed
    val occurredAt: Long = System.currentTimeMillis(),
    val groupId: String,
    val sourceType: String,  // UPI, CARD, BANK_SYNC, MANUAL
    val state: String,  // LAUNCHED, SUCCESS, FAILED, PENDING, SYNCED
    val externalTxnId: String? = null,
    val syncedAt: Long? = null
)
