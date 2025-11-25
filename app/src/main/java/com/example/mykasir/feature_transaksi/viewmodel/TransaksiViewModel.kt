package com.example.mykasir.feature_transaksi.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.mykasir.feature_transaksi.model.Customer
import com.example.mykasir.feature_transaksi.model.Transaction
import com.example.mykasir.feature_transaksi.model.TransactionItem

class TransaksiViewModel : ViewModel() {
    // Sample pelanggan
    var customers = mutableStateListOf(
        Customer(name = "Hamsi"),
        Customer(name = "Hazmi"),
        Customer(name = "Hismi")
    )
        private set

    // Form state
    var customerNameText by mutableStateOf("")
    var productName by mutableStateOf("")
    var unitPriceText by mutableStateOf("")
    var quantityText by mutableStateOf("1")

    // Computed total (in Int)
    val unitPrice: Int get() = unitPriceText.toIntOrNull() ?: 0
    val quantity: Int get() = quantityText.toIntOrNull() ?: 0

    // Keranjang item yang sedang dibuat
    var currentItems = mutableStateListOf<TransactionItem>()
        private set

    val total: Int
        get() = currentItems.sumOf { it.unitPrice * it.quantity }.coerceAtLeast(0)

    // In-memory saved transactions
    var transactions = mutableStateListOf<Transaction>()
        private set

    val hasTransactions: Boolean get() = transactions.isNotEmpty()

    fun resetForm() {
        customerNameText = ""
        productName = ""
        unitPriceText = ""
        quantityText = "1"
        currentItems.clear()
    }

    fun removeCustomer(customer: Customer) {
        customers.remove(customer)
    }

    private fun findOrCreateCustomerByName(name: String): Customer {
        val trimmed = name.trim()
        val existing = customers.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
        if (existing != null) return existing
        val newCust = Customer(name = trimmed.ifBlank { "Pelanggan" })
        customers.add(newCust)
        return newCust
    }

    fun addItemFromForm() {
        val name = productName.trim()
        val price = unitPrice
        val qty = quantity
        if (name.isBlank() || price <= 0 || qty <= 0) return
        currentItems.add(TransactionItem(productName = name, unitPrice = price, quantity = qty))
        // reset field produk agar siap input produk lain
        productName = ""
        unitPriceText = ""
        quantityText = "1"
    }

    fun removeItem(item: TransactionItem) {
        currentItems.remove(item)
    }

    fun changeQuantity(item: TransactionItem, delta: Int, maxStock: Int) {
        val idx = currentItems.indexOfFirst { it.productName == item.productName && it.unitPrice == item.unitPrice }
        if (idx >= 0) {
            val current = currentItems[idx]
            val newQty = (current.quantity + delta).coerceIn(1, if (maxStock > 0) maxStock else Int.MAX_VALUE)
            if (newQty != current.quantity) {
                currentItems[idx] = current.copy(quantity = newQty)
            }
        }
    }

    fun addOrIncreaseItem(productName: String, unitPrice: Int, maxStock: Int) {
        if (productName.isBlank() || unitPrice <= 0 || maxStock <= 0) return
        val idx = currentItems.indexOfFirst { it.productName.equals(productName, ignoreCase = true) && it.unitPrice == unitPrice }
        if (idx >= 0) {
            val item = currentItems[idx]
            val newQty = (item.quantity + 1).coerceAtMost(maxStock)
            currentItems[idx] = item.copy(quantity = newQty)
        } else {
            currentItems.add(TransactionItem(productName = productName, unitPrice = unitPrice, quantity = 1))
        }
    }

    fun saveTransaction(customerName: String): Long {
        val customer = findOrCreateCustomerByName(customerName)
        if (currentItems.isEmpty()) return customer.id
        val tx = Transaction(
            customerId = customer.id,
            items = currentItems.toList(),
            total = total
        )
        transactions.add(tx)
        currentItems.clear()
        return customer.id
    }

    fun transactionsFor(customerId: Long): List<Transaction> =
        transactions.filter { it.customerId == customerId }

    fun finalizeAllTransactions(): Int {
        // Di tahap ini kita hanya mengonfirmasi penyimpanan (tanpa backend),
        // jadi biarkan data tetap ada agar daftar pelanggan dengan transaksi tetap tampil.
        return transactions.size
    }
}
