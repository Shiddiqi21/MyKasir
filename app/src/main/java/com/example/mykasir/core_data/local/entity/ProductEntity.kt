package com.example.mykasir.core_data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Entity untuk menyimpan data produk secara lokal
 */
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val category: String = "",
    val price: Int = 0,
    val stock: Int = 0,
    val minStock: Int = 0,
    val imageUri: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)
