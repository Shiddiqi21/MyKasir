package com.example.mykasir.core_data.local.dao

import androidx.room.*
import com.example.mykasir.core_data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object untuk operasi database customer
 */
@Dao
interface CustomerDao {
    
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerEntity>>
    
    @Query("SELECT * FROM customers ORDER BY name ASC")
    suspend fun getAllCustomersSync(): List<CustomerEntity>
    
    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): CustomerEntity?
    
    @Query("SELECT * FROM customers WHERE name LIKE '%' || :query || '%'")
    fun searchCustomers(query: String): Flow<List<CustomerEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<CustomerEntity>)
    
    @Update
    suspend fun updateCustomer(customer: CustomerEntity)
    
    @Delete
    suspend fun deleteCustomer(customer: CustomerEntity)
    
    @Query("DELETE FROM customers")
    suspend fun deleteAllCustomers()
}
