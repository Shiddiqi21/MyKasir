package com.example.mykasir.core_data.repository

import android.content.Context
import android.util.Log
import com.example.mykasir.core_data.local.TokenManager
import com.example.mykasir.core_data.local.dao.CustomerDao
import com.example.mykasir.core_data.local.database.AppDatabase
import com.example.mykasir.core_data.local.entity.CustomerEntity
import com.example.mykasir.core_data.remote.RetrofitClient
import com.example.mykasir.feature_transaksi.model.Customer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository untuk mengelola data customer dari local (Room) dan remote (API)
 */
class CustomerRepository(context: Context) {
    
    private val customerDao: CustomerDao = AppDatabase.getInstance(context).customerDao()
    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(context)
    
    companion object {
        private const val TAG = "CustomerRepository"
    }
    
    /**
     * Get semua customer sebagai Flow
     */
    fun getAllCustomersFlow(): Flow<List<Customer>> {
        return customerDao.getAllCustomers().map { entities ->
            entities.map { it.toCustomer() }
        }
    }
    
    /**
     * Sync data customer dari server ke lokal
     */
    suspend fun syncFromServer(): Result<Int> {
        return try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token tidak ditemukan"))
            }
            
            val response = apiService.getAllCustomers("Bearer $token")
            
            if (response.status == "success") {
                val apiCustomers = response.data ?: emptyList()
                val entities = apiCustomers.map { apiCustomer ->
                    CustomerEntity(
                        id = apiCustomer.id,
                        name = apiCustomer.name,
                        lastUpdated = System.currentTimeMillis()
                    )
                }
                
                customerDao.deleteAllCustomers()
                customerDao.insertCustomers(entities)
                
                Log.d(TAG, "Synced ${entities.size} customers from server")
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
     * Get cached customers (untuk offline mode)
     */
    suspend fun getCachedCustomers(): List<Customer> {
        return customerDao.getAllCustomersSync().map { it.toCustomer() }
    }
    
    // Extension function untuk konversi
    private fun CustomerEntity.toCustomer(): Customer {
        return Customer(
            id = this.id,
            name = this.name
        )
    }
}
