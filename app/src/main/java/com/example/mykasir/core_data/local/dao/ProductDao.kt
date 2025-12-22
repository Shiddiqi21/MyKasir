package com.example.mykasir.core_data.local.dao

import androidx.room.*
import com.example.mykasir.core_data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object untuk operasi database produk
 */
@Dao
interface ProductDao {
    
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products ORDER BY name ASC")
    suspend fun getAllProductsSync(): List<ProductEntity>
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): ProductEntity?
    
    @Query("SELECT * FROM products WHERE stock <= minStock")
    fun getLowStockProducts(): Flow<List<ProductEntity>>
    
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<ProductEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)
    
    @Update
    suspend fun updateProduct(product: ProductEntity)
    
    @Delete
    suspend fun deleteProduct(product: ProductEntity)
    
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProductById(id: Long)
    
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}
