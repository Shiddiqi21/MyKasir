package com.example.mykasir.core_data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mykasir.core_data.local.dao.CustomerDao
import com.example.mykasir.core_data.local.dao.ProductDao
import com.example.mykasir.core_data.local.dao.TransactionDao
import com.example.mykasir.core_data.local.entity.CustomerEntity
import com.example.mykasir.core_data.local.entity.ProductEntity
import com.example.mykasir.core_data.local.entity.TransactionEntity
import com.example.mykasir.core_data.local.entity.TransactionItemEntity

/**
 * Room Database utama untuk aplikasi MyKasir
 * Menyimpan data produk, customer, dan transaksi secara lokal
 */
@Database(
    entities = [
        ProductEntity::class,
        CustomerEntity::class,
        TransactionEntity::class,
        TransactionItemEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun productDao(): ProductDao
    abstract fun customerDao(): CustomerDao
    abstract fun transactionDao(): TransactionDao
    
    companion object {
        private const val DATABASE_NAME = "mykasir_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
