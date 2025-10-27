package com.example.mykasir.feature_manajemen_produk.model

data class Product(
    val id: Long = System.currentTimeMillis(),
    val name: String,
    val category: String = "",
    val price: Int = 0,
    val stock: Int = 0,
    val minStock: Int = 0,
    val imageUri: String? = null
)
