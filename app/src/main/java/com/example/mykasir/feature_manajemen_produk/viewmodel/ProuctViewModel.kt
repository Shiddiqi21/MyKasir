package com.example.mykasir.feature_manajemen_produk.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykasir.core_data.local.TokenManager
import com.example.mykasir.core_data.remote.ProductRequest
import com.example.mykasir.core_data.remote.RetrofitClient
import com.example.mykasir.feature_manajemen_produk.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ProductViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(application)

    // State list supaya Compose recompose otomatis saat berubah
    var products = mutableStateListOf<Product>()
        private set

    // UI State untuk loading dan error
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        Log.d("ProductViewModel", "ViewModel initialized, loading products from API")
        loadProducts()
    }

    // Load semua produk dari API
    fun loadProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Log.d("ProductViewModel", "Starting to load products from API")

            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token tidak ditemukan, silakan login ulang"
                    Log.e("ProductViewModel", "Token is null or empty")
                    _isLoading.value = false
                    return@launch
                }

                Log.d("ProductViewModel", "Token: ${token.take(20)}...")
                val response = apiService.getAllProducts("Bearer $token")
                
                if (response.status == "success") {
                    val apiProducts = response.data ?: emptyList()
                    Log.d("ProductViewModel", "Successfully loaded ${apiProducts.size} products from API")
                    
                    products.clear()
                    products.addAll(apiProducts.map { apiProduct ->
                        Product(
                            id = apiProduct.id,
                            name = apiProduct.name,
                            category = apiProduct.category,
                            price = apiProduct.price,
                            stock = apiProduct.stock,
                            minStock = apiProduct.minStock,
                            imageUri = apiProduct.imageUri
                        )
                    })
                    Log.d("ProductViewModel", "Products list updated: ${products.size} items")
                } else {
                    _errorMessage.value = "Gagal memuat produk: ${response.message}"
                    Log.e("ProductViewModel", "API error: ${response.message}")
                }
                } catch (e: HttpException) {
                _errorMessage.value = "Error jaringan: ${e.message}"
                Log.e("ProductViewModel", "HttpException: ${e.message}", e)
            } catch (e: IOException) {
                _errorMessage.value = "Koneksi gagal, periksa internet Anda"
                Log.e("ProductViewModel", "IOException: ${e.message}", e)
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e("ProductViewModel", "Exception: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Tambah produk baru ke API
    fun addProduct(product: Product, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Log.d("ProductViewModel", "Adding product: ${product.name}")

            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    val error = "Token tidak ditemukan, silakan login ulang"
                    _errorMessage.value = error
                    onError(error)
                    Log.e("ProductViewModel", "Token is null or empty")
                    _isLoading.value = false
                    return@launch
                }

                val requestBody = ProductRequest(
                    name = product.name,
                    category = product.category,
                    price = product.price,
                    stock = product.stock,
                    minStock = product.minStock,
                    imageUri = product.imageUri
                )
                Log.d("ProductViewModel", "Request body: $requestBody")

                val response = apiService.createProduct("Bearer $token", requestBody)
                
                if (response.status == "success") {
                    val newProduct = response.data
                    Log.d("ProductViewModel", "Product created successfully with ID: ${newProduct?.id}")
                    
                    // Reload products dari server untuk sinkronisasi
                    loadProducts()
                    onSuccess()
                } else {
                    val error = "Gagal menambah produk: ${response.message}"
                    _errorMessage.value = error
                    onError(error)
                    Log.e("ProductViewModel", "API error: ${response.message}")
                }
            } catch (e: HttpException) {
                val error = "Error jaringan: ${e.message}"
                _errorMessage.value = error
                onError(error)
                Log.e("ProductViewModel", "HttpException: ${e.message}", e)
            } catch (e: IOException) {
                val error = "Koneksi gagal, periksa internet Anda"
                _errorMessage.value = error
                onError(error)
                Log.e("ProductViewModel", "IOException: ${e.message}", e)
            } catch (e: Exception) {
                val error = "Error: ${e.message}"
                _errorMessage.value = error
                onError(error)
                Log.e("ProductViewModel", "Exception: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update produk di API
    fun updateProduct(updated: Product, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Log.d("ProductViewModel", "Updating product ID: ${updated.id}")

            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    val error = "Token tidak ditemukan, silakan login ulang"
                    _errorMessage.value = error
                    onError(error)
                    Log.e("ProductViewModel", "Token is null or empty")
                    _isLoading.value = false
                    return@launch
                }

                val requestBody = ProductRequest(
                    name = updated.name,
                    category = updated.category,
                    price = updated.price,
                    stock = updated.stock,
                    minStock = updated.minStock,
                    imageUri = updated.imageUri
                )
                Log.d("ProductViewModel", "Update request body: $requestBody")

                val response = apiService.updateProduct("Bearer $token", updated.id, requestBody)
                
                if (response.status == "success") {
                    Log.d("ProductViewModel", "Product updated successfully")
                    
                    // Reload products dari server
                    loadProducts()
                    onSuccess()
                } else {
                    val error = "Gagal update produk: ${response.message}"
                    _errorMessage.value = error
                    onError(error)
                    Log.e("ProductViewModel", "API error: ${response.message}")
                }
            } catch (e: HttpException) {
                val error = "Error jaringan: ${e.message}"
                _errorMessage.value = error
                onError(error)
                Log.e("ProductViewModel", "HttpException: ${e.message}", e)
            } catch (e: IOException) {
                val error = "Koneksi gagal, periksa internet Anda"
                _errorMessage.value = error
                onError(error)
                Log.e("ProductViewModel", "IOException: ${e.message}", e)
            } catch (e: Exception) {
                val error = "Error: ${e.message}"
                _errorMessage.value = error
                onError(error)
                Log.e("ProductViewModel", "Exception: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Hapus produk dari API
    fun deleteProduct(product: Product, onSuccess: () -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Log.d("ProductViewModel", "Deleting product ID: ${product.id}")

            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    val error = "Token tidak ditemukan, silakan login ulang"
                    _errorMessage.value = error
                    onError(error)
                    Log.e("ProductViewModel", "Token is null or empty")
                    _isLoading.value = false
                    return@launch
                }

                val response = apiService.deleteProduct("Bearer $token", product.id)
                
                if (response.status == "success") {
                    Log.d("ProductViewModel", "Product deleted successfully")
                    
                    // Reload products dari server
                    loadProducts()
                    onSuccess()
                } else {
                    val error = "Gagal hapus produk: ${response.message}"
                    _errorMessage.value = error
                    onError(error)
                    Log.e("ProductViewModel", "API error: ${response.message}")
                }
            } catch (e: HttpException) {
                val error = "Error jaringan: ${e.message}"
                _errorMessage.value = error
                onError(error)
                Log.e("ProductViewModel", "HttpException: ${e.message}", e)
            } catch (e: IOException) {
                val error = "Koneksi gagal, periksa internet Anda"
                _errorMessage.value = error
                onError(error)
                Log.e("ProductViewModel", "IOException: ${e.message}", e)
            } catch (e: Exception) {
                val error = "Error: ${e.message}"
                _errorMessage.value = error
                onError(error)
                Log.e("ProductViewModel", "Exception: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Get produk dengan stok tipis
    fun getLowStockProducts(): List<Product> {
        return products.filter { it.stock <= it.minStock }
    }
}