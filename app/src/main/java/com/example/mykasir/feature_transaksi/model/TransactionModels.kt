package com.example.mykasir.feature_transaksi.model

data class ProductRef(
    val name: String,
    val unitPrice: Int
)

data class TransactionItem(
    val productName: String,
    val unitPrice: Int,
    val quantity: Int
)

data class Transaction(
    val id: Long = 0,
    val customerId: Long,
    val items: List<TransactionItem>,
    val total: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val createdAt: String? = null
)
