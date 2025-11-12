package com.example.mykasir.feature_transaksi.model

data class Customer(
    val id: Long = System.currentTimeMillis(),
    val name: String
)
