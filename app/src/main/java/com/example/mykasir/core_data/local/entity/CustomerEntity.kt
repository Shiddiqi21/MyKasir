package com.example.mykasir.core_data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity untuk menyimpan data customer secara lokal
 */
@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
