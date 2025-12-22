package com.example.mykasir.core_data.local.dao

import androidx.room.*
import com.example.mykasir.core_data.local.entity.TransactionEntity
import com.example.mykasir.core_data.local.entity.TransactionItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object untuk operasi database transaksi
 */
@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>
    
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    suspend fun getAllTransactionsSync(): List<TransactionEntity>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?
    
    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun getTransactionItems(transactionId: Long): List<TransactionItemEntity>
    
    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    fun getTransactionItemsFlow(transactionId: Long): Flow<List<TransactionItemEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItem(item: TransactionItemEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItemEntity>)
    
    @Transaction
    suspend fun insertTransactionWithItems(
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>
    ) {
        insertTransaction(transaction)
        insertTransactionItems(items)
    }
    
    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
    
    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Long)
    
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
    
    @Query("DELETE FROM transaction_items")
    suspend fun deleteAllTransactionItems()
}
