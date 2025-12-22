package com.example.mykasir.core_data.repository

import android.content.Context
import android.util.Log
import com.example.mykasir.core_data.local.TokenManager
import com.example.mykasir.core_data.local.dao.TransactionDao
import com.example.mykasir.core_data.local.database.AppDatabase
import com.example.mykasir.core_data.local.entity.TransactionEntity
import com.example.mykasir.core_data.local.entity.TransactionItemEntity
import com.example.mykasir.core_data.remote.RetrofitClient
import com.example.mykasir.feature_transaksi.model.Transaction
import com.example.mykasir.feature_transaksi.model.TransactionItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.HttpException
import java.io.IOException

/**
 * Repository untuk mengelola data transaksi dari local (Room) dan remote (API)
 */
class TransactionRepository(context: Context) {
    
    private val transactionDao: TransactionDao = AppDatabase.getInstance(context).transactionDao()
    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(context)
    
    companion object {
        private const val TAG = "TransactionRepository"
    }
    
    /**
     * Get semua transaksi sebagai Flow
     */
    fun getAllTransactionsFlow(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { entities ->
            entities.map { entity ->
                val items = transactionDao.getTransactionItems(entity.id)
                entity.toTransaction(items)
            }
        }
    }
    
    /**
     * Sync data transaksi dari server ke lokal
     */
    suspend fun syncFromServer(): Result<Int> {
        return try {
            val token = tokenManager.getToken()
            if (token.isNullOrEmpty()) {
                return Result.failure(Exception("Token tidak ditemukan"))
            }
            
            val response = apiService.getAllTransactions("Bearer $token")
            
            if (response.status == "success") {
                val apiTransactions = response.data ?: emptyList()
                
                // Clear existing data
                transactionDao.deleteAllTransactionItems()
                transactionDao.deleteAllTransactions()
                
                // Insert new data
                apiTransactions.forEach { apiTransaction ->
                    val transactionEntity = TransactionEntity(
                        id = apiTransaction.id,
                        customerId = apiTransaction.customerId,
                        total = apiTransaction.total,
                        timestamp = apiTransaction.timestamp,
                        cashierName = apiTransaction.cashierName,
                        createdAt = apiTransaction.createdAt,
                        lastUpdated = System.currentTimeMillis()
                    )
                    
                    val itemEntities = apiTransaction.items.map { item ->
                        TransactionItemEntity(
                            transactionId = apiTransaction.id,
                            productName = item.productName,
                            unitPrice = item.unitPrice,
                            quantity = item.quantity
                        )
                    }
                    
                    transactionDao.insertTransactionWithItems(transactionEntity, itemEntities)
                }
                
                Log.d(TAG, "Synced ${apiTransactions.size} transactions from server")
                Result.success(apiTransactions.size)
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
     * Get cached transactions (untuk offline mode)
     */
    suspend fun getCachedTransactions(): List<Transaction> {
        return try {
            val transactions = transactionDao.getAllTransactionsSync()
            transactions.map { entity ->
                val items = transactionDao.getTransactionItems(entity.id)
                entity.toTransaction(items)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cached transactions: ${e.message}")
            emptyList()
        }
    }
    
    /**
     * Save transaction to local cache after successful API call
     */
    suspend fun cacheTransaction(transaction: Transaction) {
        try {
            val entity = TransactionEntity(
                id = transaction.id,
                customerId = transaction.customerId,
                total = transaction.total,
                timestamp = transaction.timestamp,
                cashierName = transaction.cashierName,
                createdAt = transaction.createdAt
            )
            
            val itemEntities = transaction.items.map { item ->
                TransactionItemEntity(
                    transactionId = transaction.id,
                    productName = item.productName,
                    unitPrice = item.unitPrice,
                    quantity = item.quantity
                )
            }
            
            transactionDao.insertTransactionWithItems(entity, itemEntities)
        } catch (e: Exception) {
            Log.e(TAG, "Error caching transaction: ${e.message}")
        }
    }
    
    // Extension function untuk konversi
    private fun TransactionEntity.toTransaction(items: List<TransactionItemEntity>): Transaction {
        return Transaction(
            id = this.id,
            customerId = this.customerId,
            items = items.map { 
                TransactionItem(
                    productName = it.productName,
                    unitPrice = it.unitPrice,
                    quantity = it.quantity
                )
            },
            total = this.total,
            timestamp = this.timestamp,
            createdAt = this.createdAt,
            cashierName = this.cashierName
        )
    }
}
