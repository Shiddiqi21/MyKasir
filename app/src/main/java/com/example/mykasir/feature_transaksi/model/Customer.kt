package com.example.mykasir.feature_transaksi.model

data class Customer(
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val address: String = ""
)
