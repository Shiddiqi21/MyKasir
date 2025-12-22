package com.example.mykasir.feature_manajemen_produk.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykasir.core_ui.NotificationHelper
import com.example.mykasir.core_data.repository.ProductRepository
import com.example.mykasir.feature_manajemen_produk.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * ViewModel untuk manajemen produk dengan dukungan offline via Room Database
 */
class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ProductRepository(application)

    // State list supaya Compose recompose otomatis saat berubah
    var products = mutableStateListOf<Product>()
        private set

    // UI State untuk loading dan error
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage
    
    // State untuk menandakan apakah data dari cache
    private val _isFromCache = MutableStateFlow(false)
    val isFromCache: StateFlow<Boolean> = _isFromCache

    init {
        Log.d(TAG, "ViewModel initialized")
        // Observe local database changes
        observeLocalProducts()
        // Sync from server
        syncProducts()
    }
    
    /**
     * Observe perubahan data dari Room Database
     */
    private fun observeLocalProducts() {
        viewModelScope.launch {
            repository.getAllProductsFlow().collectLatest { localProducts ->
                products.clear()
                products.addAll(localProducts)
                Log.d(TAG, "Local products updated: ${localProducts.size} items")
            }
        }
    }

    /**
     * Sync produk dari server ke local database
     */
    fun syncProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Log.d(TAG, "Syncing products from server...")

            val result = repository.syncFromServer()
            
            result.onSuccess { count ->
                Log.d(TAG, "Successfully synced $count products from server")
                _isFromCache.value = false
                
                // Cek produk dengan stok rendah dan tampilkan notifikasi
                checkLowStockAndNotify()
            }.onFailure { error ->
                Log.e(TAG, "Sync failed: ${error.message}")
                // Jika sync gagal, gunakan data dari cache
                val cachedProducts = repository.getCachedProducts()
                if (cachedProducts.isNotEmpty()) {
                    _isFromCache.value = true
                    Log.d(TAG, "Using ${cachedProducts.size} cached products")
                } else {
                    _errorMessage.value = "Gagal memuat produk: ${error.message}"
                }
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * Load products - untuk backward compatibility
     */
    fun loadProducts() {
        syncProducts()
    }

    /**
     * Tambah produk baru
     */
    fun addProduct(product: Product, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Log.d(TAG, "Adding product: ${product.name}")

            val result = repository.addProduct(product)
            
            result.onSuccess {
                Log.d(TAG, "Product added successfully")
                // Tampilkan notifikasi produk ditambahkan
                NotificationHelper.showProductAddedNotification(getApplication(), product.name)
                onSuccess()
            }.onFailure { error ->
                val errorMsg = "Gagal menambah produk: ${error.message}"
                _errorMessage.value = errorMsg
                onError(errorMsg)
                Log.e(TAG, errorMsg)
            }
            
            _isLoading.value = false
        }
    }

    /**
     * Update produk
     */
    fun updateProduct(updated: Product, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Log.d(TAG, "Updating product ID: ${updated.id}")

            val result = repository.updateProduct(updated)
            
            result.onSuccess {
                Log.d(TAG, "Product updated successfully")
                onSuccess()
            }.onFailure { error ->
                val errorMsg = "Gagal update produk: ${error.message}"
                _errorMessage.value = errorMsg
                onError(errorMsg)
                Log.e(TAG, errorMsg)
            }
            
            _isLoading.value = false
        }
    }

    /**
     * Hapus produk
     */
    fun deleteProduct(product: Product, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Log.d(TAG, "Deleting product ID: ${product.id}")

            val result = repository.deleteProduct(product)
            
            result.onSuccess {
                Log.d(TAG, "Product deleted successfully")
                onSuccess()
            }.onFailure { error ->
                val errorMsg = "Gagal hapus produk: ${error.message}"
                _errorMessage.value = errorMsg
                onError(errorMsg)
                Log.e(TAG, errorMsg)
            }
            
            _isLoading.value = false
        }
    }

    /**
     * Get produk dengan stok rendah
     */
    fun getLowStockProducts(): List<Product> {
        return products.filter { it.stock <= it.minStock }
    }
    
    /**
     * Cek produk dengan stok rendah dan tampilkan notifikasi
     */
    private fun checkLowStockAndNotify() {
        val lowStockProducts = getLowStockProducts()
        // Tampilkan notifikasi untuk maksimal 3 produk pertama
        lowStockProducts.take(3).forEach { product ->
            NotificationHelper.showLowStockNotification(getApplication(), product.name, product.stock)
        }
    }
    
    companion object {
        private const val TAG = "ProductViewModel"
    }
}