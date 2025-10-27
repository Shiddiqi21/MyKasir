package com.example.mykasir.feature_manajemen_produk.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.mykasir.feature_manajemen_produk.model.Product

class ProductViewModel : ViewModel() {

    // State list supaya Compose recompose otomatis saat berubah
    var products = mutableStateListOf<Product>()
        private set

    init {
        // contoh data awal lengkap dengan minStock
        products.addAll(
            listOf(
                Product(id = 1L, name = "Susu", category = "Minuman", price = 10000, stock = 10, minStock = 5),
                Product(id = 2L, name = "Roti", category = "Makanan", price = 8000, stock = 1, minStock = 10),
                Product(id = 3L, name = "Air Mineral", category = "Minuman", price = 5000, stock = 20, minStock = 5),
                Product(id = 4L, name = "Jus Alpukat", category = "Minuman", price = 15000, stock = 3, minStock = 5)
            )
        )
    }

    fun addProduct(product: Product) {
        products.add(product)
    }

    fun deleteProduct(product: Product) {
        products.remove(product)
    }

    fun editProduct(updated: Product) {
        val idx = products.indexOfFirst { it.id == updated.id }
        if (idx >= 0) products[idx] = updated
    }
}
