package com.example.mykasir.core_data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity untuk menyimpan data transaksi secara lokal
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: Long,
    val customerId: Long,
    val total: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val cashierName: String? = null,
    val createdAt: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
