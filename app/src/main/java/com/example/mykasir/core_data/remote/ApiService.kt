package com.example.mykasir.core_data.remote

import com.example.mykasir.feature_auth.model.LoginResponse
import com.example.mykasir.feature_manajemen_produk.model.Product
import com.example.mykasir.feature_transaksi.model.Customer
import com.example.mykasir.feature_transaksi.model.Transaction
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTH ====================
    @FormUrlEncoded
    @POST("api/auth/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @FormUrlEncoded
    @POST("api/auth/register")
    suspend fun register(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("name") name: String,
        @Field("role") role: String = "kasir"
    ): ApiResponse<UserData>

    @GET("api/auth/profile")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): ApiResponse<UserData>

    @PUT("api/auth/profile")
    suspend fun updateProfile(
        @Header("Authorization") token: String,
        @Body updates: Map<String, String>
    ): ApiResponse<UserData>

    // ==================== PRODUCTS ====================
    @GET("api/products")
    suspend fun getAllProducts(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("lowStock") lowStock: Boolean? = null
    ): ApiResponse<List<Product>>

    @GET("api/products/{id}")
    suspend fun getProductById(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): ApiResponse<Product>

    @GET("api/products/low-stock")
    suspend fun getLowStockProducts(
        @Header("Authorization") token: String
    ): ApiResponse<List<Product>>

    @POST("api/products")
    suspend fun createProduct(
        @Header("Authorization") token: String,
        @Body product: ProductRequest
    ): ApiResponse<Product>

    @PUT("api/products/{id}")
    suspend fun updateProduct(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body product: ProductRequest
    ): ApiResponse<Product>

    @PATCH("api/products/{id}/stock")
    suspend fun updateStock(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body request: StockUpdateRequest
    ): ApiResponse<Product>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): ApiResponse<Unit>

    // ==================== CUSTOMERS ====================
    @GET("api/customers")
    suspend fun getAllCustomers(
        @Header("Authorization") token: String
    ): ApiResponse<List<Customer>>

    @GET("api/customers/{id}")
    suspend fun getCustomerById(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): ApiResponse<Customer>

    @POST("api/customers")
    suspend fun createCustomer(
        @Header("Authorization") token: String,
        @Body customer: CustomerRequest
    ): ApiResponse<Customer>

    @PUT("api/customers/{id}")
    suspend fun updateCustomer(
        @Header("Authorization") token: String,
        @Path("id") id: Long,
        @Body customer: CustomerRequest
    ): ApiResponse<Customer>

    @DELETE("api/customers/{id}")
    suspend fun deleteCustomer(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): ApiResponse<Unit>

    // ==================== TRANSACTIONS ====================
    @GET("api/transactions")
    suspend fun getAllTransactions(
        @Header("Authorization") token: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("customerId") customerId: Long? = null
    ): ApiResponse<List<Transaction>>

    @GET("api/transactions/{id}")
    suspend fun getTransactionById(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): ApiResponse<Transaction>

    @POST("api/transactions")
    suspend fun createTransaction(
        @Header("Authorization") token: String,
        @Body transaction: TransactionRequest
    ): ApiResponse<Transaction>

    @DELETE("api/transactions/{id}")
    suspend fun deleteTransaction(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): ApiResponse<Unit>

    // ==================== REPORTS ====================
    @GET("api/reports/sales")
    suspend fun getSalesReport(
        @Header("Authorization") token: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("period") period: String? = null // "today", "week", "month"
    ): ApiResponse<SalesReport>

    @GET("api/reports/detailed")
    suspend fun getDetailedReport(
        @Header("Authorization") token: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): ApiResponse<List<Transaction>>
}

// ==================== DATA CLASSES ====================
data class ApiResponse<T>(
    val status: String,
    val message: String? = null,
    val data: T? = null
)

data class UserData(
    val id: Long,
    val email: String,
    val name: String,
    val role: String,
    val token: String? = null
)

data class ProductRequest(
    val name: String,
    val category: String = "",
    val price: Int,
    val stock: Int,
    val minStock: Int = 0,
    val imageUri: String? = null
)

data class StockUpdateRequest(
    val stock: Int
)

data class CustomerRequest(
    val name: String
)

data class TransactionRequest(
    val customerId: Long,
    val items: List<TransactionItemRequest>
)

data class TransactionItemRequest(
    val productName: String,
    val unitPrice: Int,
    val quantity: Int
)

data class SalesReport(
    val summary: SalesSummary,
    val topProducts: List<TopProduct>,
    val dailySales: List<DailySale>
)

data class SalesSummary(
    val totalSales: Int,
    val totalTransactions: Int,
    val averageTransaction: Double
)

data class TopProduct(
    val productName: String,
    val totalQuantity: Int,
    val totalRevenue: Int
)

data class DailySale(
    val date: String,
    val count: Int,
    val total: Int
)
