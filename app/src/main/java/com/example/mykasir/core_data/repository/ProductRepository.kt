package com.example.mykasir.core_data.repository

import android.content.Context
import android.util.Log
import com.example.mykasir.core_data.local.TokenManager
import com.example.mykasir.core_data.local.dao.ProductDao
import com.example.mykasir.core_data.local.database.AppDatabase
import com.example.mykasir.core_data.local.entity.ProductEntity
import com.example.mykasir.core_data.remote.ProductRequest
import com.example.mykasir.core_data.remote.RetrofitClient
import com.example.mykasir.feature_manajemen_produk.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository untuk mengelola data produk dari local (Room) dan remote (API)
 * Menggunakan strategi: tampilkan data lokal dulu, lalu sync dengan server
 */
class ProductRepository(context: Context) {
    
    private val productDao: ProductDao = AppDatabase.getInstance(context).productDao()
    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(context)
    
    companion object {
        private const val TAG = "ProductRepository"
    }
    
    /**
     * Get semua produk sebagai Flow (otomatis update saat data berubah)
     */
    fun getAllProductsFlow(): Flow<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toProduct() }
        }
    }
    
    /**
     * Get produk dengan stok rendah
     */
    fun getLowStockProductsFlow(): Flow<List<Product>> {
        return productDao.getLowStockProducts().map { entities ->
            entities.map { it.toProduct() }
        }
    }
    
    /**
     * Sync data dari server ke lokal
     * @return Result dengan jumlah produk yang di-sync atau error message
     */
    suspend fun syncFromServer(): Result<Int> {
        return try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token tidak ditemukan"))
            }
            
            val response = apiService.getAllProducts("Bearer $token")
            
            if (response.status == "success") {
                val apiProducts = response.data ?: emptyList()
                val entities = apiProducts.map { apiProduct ->
                    ProductEntity(
                        id = apiProduct.id,
                        name = apiProduct.name,
                        category = apiProduct.category,
                        price = apiProduct.price,
                        stock = apiProduct.stock,
                        minStock = apiProduct.minStock,
                        imageUri = apiProduct.imageUri,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
                
                // Replace all local products with server data
                productDao.deleteAllProducts()
                productDao.insertProducts(entities)
                
                Log.d(TAG, "Synced ${entities.size} products from server")
                Result.success(entities.size)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP error during sync: ${e.message}")
            Result.failure(e)
        } catch (e: IOException) {
            Log.e(TAG, "Network error during sync: ${e.message}")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Error during sync: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Tambah produk baru (ke server dan lokal)
     */
    suspend fun addProduct(product: Product): Result<Product> {
        return try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token tidak ditemukan"))
            }
            
            val request = ProductRequest(
                name = product.name,
                category = product.category,
                price = product.price,
                stock = product.stock,
                minStock = product.minStock,
                imageUri = product.imageUri
            )
            
            val response = apiService.createProduct("Bearer $token", request)
            
            if (response.status == "success" && response.data != null) {
                val newProduct = response.data
                // Save to local database
                productDao.insertProduct(
                    ProductEntity(
                        id = newProduct.id,
                        name = newProduct.name,
                        category = newProduct.category,
                        price = newProduct.price,
                        stock = newProduct.stock,
                        minStock = newProduct.minStock,
                        imageUri = newProduct.imageUri
                    )
                )
                
                Result.success(
                    Product(
                        id = newProduct.id,
                        name = newProduct.name,
                        category = newProduct.category,
                        price = newProduct.price,
                        stock = newProduct.stock,
                        minStock = newProduct.minStock,
                        imageUri = newProduct.imageUri
                    )
                )
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error adding product: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Update produk (ke server dan lokal)
     */
    suspend fun updateProduct(product: Product): Result<Product> {
        return try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token tidak ditemukan"))
            }
            
            val request = ProductRequest(
                name = product.name,
                category = product.category,
                price = product.price,
                stock = product.stock,
                minStock = product.minStock,
                imageUri = product.imageUri
            )
            
            val response = apiService.updateProduct("Bearer $token", product.id, request)
            
            if (response.status == "success") {
                // Update local database
                productDao.updateProduct(product.toEntity())
                Result.success(product)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating product: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Hapus produk (dari server dan lokal)
     */
    suspend fun deleteProduct(product: Product): Result<Unit> {
        return try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token tidak ditemukan"))
            }
            
            val response = apiService.deleteProduct("Bearer $token", product.id)
            
            if (response.status == "success") {
                // Delete from local database
                productDao.deleteProductById(product.id)
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting product: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Get cached products (untuk offline mode)
     */
    suspend fun getCachedProducts(): List<Product> {
        return productDao.getAllProductsSync().map { it.toProduct() }
    }
    
    // Extension functions untuk konversi
    private fun ProductEntity.toProduct(): Product {
        return Product(
            id = this.id,
            name = this.name,
            category = this.category,
            price = this.price,
            stock = this.stock,
            minStock = this.minStock,
            imageUri = this.imageUri
        )
    }
    
    private fun Product.toEntity(): ProductEntity {
        return ProductEntity(
            id = this.id,
            name = this.name,
            category = this.category,
            price = this.price,
            stock = this.stock,
            minStock = this.minStock,
            imageUri = this.imageUri
        )
    }
}
