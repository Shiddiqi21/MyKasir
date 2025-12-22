package com.example.mykasir.feature_transaksi.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mykasir.core_data.local.TokenManager
import com.example.mykasir.core_data.remote.CustomerRequest
import com.example.mykasir.core_data.remote.RetrofitClient
import com.example.mykasir.core_data.remote.TransactionItemRequest
import com.example.mykasir.core_data.remote.TransactionRequest
import com.example.mykasir.core_ui.NotificationHelper
import com.example.mykasir.core_ui.formatRupiah
import com.example.mykasir.feature_transaksi.model.Customer
import com.example.mykasir.feature_transaksi.model.Transaction
import com.example.mykasir.feature_transaksi.model.TransactionItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class TransaksiViewModel(application: Application) : AndroidViewModel(application) {
    
    private val apiService = RetrofitClient.apiService
    private val tokenManager = TokenManager(application)

    // Sample pelanggan (akan diload dari API)
    var customers = mutableStateListOf<Customer>()
        private set

    // Form state
    var customerNameText by mutableStateOf("")
    var productName by mutableStateOf("")
    var unitPriceText by mutableStateOf("")
    var quantityText by mutableStateOf("1")

    // Computed total (in Int)
    val unitPrice: Int get() = unitPriceText.toIntOrNull() ?: 0
    val quantity: Int get() = quantityText.toIntOrNull() ?: 0

    // Keranjang item yang sedang dibuat
    var currentItems = mutableStateListOf<TransactionItem>()
        private set

    val total: Int
        get() = currentItems.sumOf { it.unitPrice * it.quantity }.coerceAtLeast(0)

    // In-memory saved transactions (akan diload dari API)
    var transactions = mutableStateListOf<Transaction>()
        private set

    val hasTransactions: Boolean get() = transactions.isNotEmpty()

    // UI State untuk loading dan error
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    companion object {
        private const val TAG = "TransaksiViewModel"
    }

    init {
        Log.d(TAG, "ViewModel initialized, loading customers and transactions from API")
        loadCustomers()
        loadTransactions()
    }

    // Load semua customer dari API
    fun loadCustomers() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Log.d(TAG, "Loading customers from API")

            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token tidak ditemukan, silakan login ulang"
                    Log.e(TAG, "Token is null or empty")
                    _isLoading.value = false
                    return@launch
                }

                val response = apiService.getAllCustomers("Bearer $token")
                
                if (response.status == "success") {
                    val apiCustomers = response.data ?: emptyList()
                    Log.d(TAG, "Successfully loaded ${apiCustomers.size} customers from API")
                    
                    customers.clear()
                    customers.addAll(apiCustomers.map { apiCustomer ->
                        Customer(
                            id = apiCustomer.id,
                            name = apiCustomer.name,
                            phone = apiCustomer.phone ?: "",
                            address = apiCustomer.address ?: ""
                        )
                    })
                    Log.d(TAG, "Customers list updated: ${customers.size} items")
                } else {
                    _errorMessage.value = "Gagal memuat pelanggan: ${response.message}"
                    Log.e(TAG, "API error: ${response.message}")
                }
            } catch (e: HttpException) {
                _errorMessage.value = "Error jaringan: ${e.message}"
                Log.e(TAG, "HttpException: ${e.message}", e)
            } catch (e: IOException) {
                _errorMessage.value = "Koneksi gagal, periksa internet Anda"
                Log.e(TAG, "IOException: ${e.message}", e)
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e(TAG, "Exception: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Load semua transactions dari API
    fun loadTransactions() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Log.d(TAG, "Loading transactions from API")

            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    _errorMessage.value = "Token tidak ditemukan, silakan login ulang"
                    Log.e(TAG, "Token is null or empty")
                    _isLoading.value = false
                    return@launch
                }

                val response = apiService.getAllTransactions("Bearer $token")
                
                if (response.status == "success") {
                    val apiTransactions = response.data ?: emptyList()
                    Log.d(TAG, "Successfully loaded ${apiTransactions.size} transactions from API")
                    
                    transactions.clear()
                    transactions.addAll(apiTransactions.map { apiTx ->
                        Transaction(
                            id = apiTx.id,
                            customerId = apiTx.customerId,
                            items = apiTx.items.map { item ->
                                TransactionItem(
                                    productName = item.productName,
                                    unitPrice = item.unitPrice,
                                    quantity = item.quantity
                                )
                            },
                            total = apiTx.total,
                            createdAt = apiTx.createdAt,
                            cashierName = apiTx.cashierName
                        )
                    })
                    Log.d(TAG, "Transactions list updated: ${transactions.size} items")
                } else {
                    _errorMessage.value = "Gagal memuat transaksi: ${response.message}"
                    Log.e(TAG, "API error: ${response.message}")
                }
            } catch (e: HttpException) {
                _errorMessage.value = "Error jaringan: ${e.message}"
                Log.e(TAG, "HttpException: ${e.message}", e)
            } catch (e: IOException) {
                _errorMessage.value = "Koneksi gagal, periksa internet Anda"
                Log.e(TAG, "IOException: ${e.message}", e)
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.message}"
                Log.e(TAG, "Exception: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetForm() {
        customerNameText = ""
        productName = ""
        unitPriceText = ""
        quantityText = "1"
        currentItems.clear()
    }

    // Create customer via API
    fun createCustomer(name: String, phone: String = "", address: String = "", onSuccess: (Customer) -> Unit = {}, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Log.d(TAG, "Creating customer: $name")

            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    val error = "Token tidak ditemukan, silakan login ulang"
                    _errorMessage.value = error
                    onError(error)
                    _isLoading.value = false
                    return@launch
                }

                val requestBody = CustomerRequest(
                    name = name
                )

                val response = apiService.createCustomer("Bearer $token", requestBody)
                
                if (response.status == "success") {
                    val newCustomer = response.data
                    Log.d(TAG, "Customer created successfully with ID: ${newCustomer?.id}")
                    
                    if (newCustomer != null) {
                        val customer = Customer(
                            id = newCustomer.id,
                            name = newCustomer.name,
                            phone = newCustomer.phone ?: "",
                            address = newCustomer.address ?: ""
                        )
                        customers.add(customer)
                        onSuccess(customer)
                    }
                } else {
                    val error = "Gagal membuat pelanggan: ${response.message}"
                    _errorMessage.value = error
                    onError(error)
                    Log.e(TAG, "API error: ${response.message}")
                }
            } catch (e: Exception) {
                val error = "Error: ${e.message}"
                _errorMessage.value = error
                onError(error)
                Log.e(TAG, "Exception: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeCustomer(customer: Customer) {
        viewModelScope.launch {
            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) return@launch

                // Hapus semua transaksi yang terkait dengan customer ini
                val customerTransactions = transactions.filter { it.customerId == customer.id }
                for (transaction in customerTransactions) {
                    try {
                        val txResponse = apiService.deleteTransaction("Bearer $token", transaction.id)
                        if (txResponse.status == "success") {
                            transactions.remove(transaction)
                            Log.d(TAG, "Transaction deleted: ${transaction.id}")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error deleting transaction ${transaction.id}: ${e.message}", e)
                    }
                }

                // Kemudian hapus customer
                val response = apiService.deleteCustomer("Bearer $token", customer.id)
                if (response.status == "success") {
                    customers.remove(customer)
                    Log.d(TAG, "Customer deleted: ${customer.id}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting customer: ${e.message}", e)
            }
        }
    }

    private fun findOrCreateCustomerByName(name: String): Customer {
        val trimmed = name.trim()
        // Selalu buat customer baru untuk setiap transaksi
        // Tidak menggabungkan dengan customer yang sudah ada
        val newCust = Customer(id = 0, name = trimmed.ifBlank { "Pelanggan" })
        return newCust
    }

    fun addItemFromForm() {
        val name = productName.trim()
        val price = unitPrice
        val qty = quantity
        if (name.isBlank() || price <= 0 || qty <= 0) return
        currentItems.add(TransactionItem(productName = name, unitPrice = price, quantity = qty))
        // reset field produk agar siap input produk lain
        productName = ""
        unitPriceText = ""
        quantityText = "1"
    }

    fun removeItem(item: TransactionItem) {
        currentItems.remove(item)
    }

    fun changeQuantity(item: TransactionItem, delta: Int, maxStock: Int) {
        val idx = currentItems.indexOfFirst { it.productName == item.productName && it.unitPrice == item.unitPrice }
        if (idx >= 0) {
            val current = currentItems[idx]
            val newQty = (current.quantity + delta).coerceIn(1, if (maxStock > 0) maxStock else Int.MAX_VALUE)
            if (newQty != current.quantity) {
                currentItems[idx] = current.copy(quantity = newQty)
            }
        }
    }

    fun addOrIncreaseItem(productName: String, unitPrice: Int, maxStock: Int) {
        if (productName.isBlank() || unitPrice <= 0 || maxStock <= 0) return
        val idx = currentItems.indexOfFirst { it.productName.equals(productName, ignoreCase = true) && it.unitPrice == unitPrice }
        if (idx >= 0) {
            val item = currentItems[idx]
            val newQty = (item.quantity + 1).coerceAtMost(maxStock)
            currentItems[idx] = item.copy(quantity = newQty)
        } else {
            currentItems.add(TransactionItem(productName = productName, unitPrice = unitPrice, quantity = 1))
        }
    }

    // Save transaction via API
    fun saveTransaction(customerName: String, onSuccess: (Long) -> Unit = {}, onError: (String) -> Unit = {}) {
        if (currentItems.isEmpty()) {
            onError("Keranjang kosong")
            return
        }
        
        // Simpan total sebelum clear
        val transactionTotal = total

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            Log.d(TAG, "Saving transaction for customer: $customerName")

            try {
                val token = tokenManager.getToken()
                if (token.isNullOrEmpty()) {
                    val error = "Token tidak ditemukan, silakan login ulang"
                    _errorMessage.value = error
                    onError(error)
                    _isLoading.value = false
                    return@launch
                }

                // Find or create customer
                var customer = findOrCreateCustomerByName(customerName)
                
                // Jika customer baru (id=0), buat dulu via API
                if (customer.id == 0L) {
                    Log.d(TAG, "Creating new customer: ${customer.name}")
                    val createResponse = apiService.createCustomer(
                        "Bearer $token",
                        CustomerRequest(name = customer.name)
                    )
                    
                    if (createResponse.status == "success") {
                        val newCustomer = createResponse.data
                        if (newCustomer != null) {
                            customer = Customer(
                                id = newCustomer.id,
                                name = newCustomer.name,
                                phone = newCustomer.phone ?: "",
                                address = newCustomer.address ?: ""
                            )
                            customers.add(customer)
                            Log.d(TAG, "New customer created with ID: ${customer.id}")
                        }
                    } else {
                        val error = "Gagal membuat pelanggan: ${createResponse.message}"
                        _errorMessage.value = error
                        onError(error)
                        _isLoading.value = false
                        return@launch
                    }
                }

                // Sekarang buat transaction
                val requestBody = TransactionRequest(
                    customerId = customer.id,
                    items = currentItems.map { item ->
                        TransactionItemRequest(
                            productName = item.productName,
                            unitPrice = item.unitPrice,
                            quantity = item.quantity
                        )
                    }
                )

                Log.d(TAG, "Transaction request: $requestBody")

                val response = apiService.createTransaction("Bearer $token", requestBody)
                
                if (response.status == "success") {
                    val newTransaction = response.data
                    Log.d(TAG, "Transaction created successfully with ID: ${newTransaction?.id}")
                    
                    // Tambahkan transaksi baru langsung ke list (tidak menunggu reload)
                    if (newTransaction != null) {
                        val transaction = Transaction(
                            id = newTransaction.id,
                            customerId = customer.id,
                            items = currentItems.map { item ->
                                TransactionItem(
                                    productName = item.productName,
                                    unitPrice = item.unitPrice,
                                    quantity = item.quantity
                                )
                            },
                            total = newTransaction.total,
                            createdAt = newTransaction.createdAt,
                            cashierName = newTransaction.cashierName
                        )
                        transactions.add(0, transaction) // Tambahkan di awal list
                        Log.d(TAG, "Transaction added to local list. Total: ${transactions.size}")
                    }
                    
                    // Clear cart
                    currentItems.clear()
                    
                    // Tampilkan notifikasi transaksi berhasil
                    NotificationHelper.showTransactionNotification(
                        getApplication(),
                        formatRupiah(transactionTotal)
                    )
                    
                    onSuccess(customer.id)
                } else {
                    val error = "Gagal menyimpan transaksi: ${response.message}"
                    _errorMessage.value = error
                    onError(error)
                    Log.e(TAG, error)
                }
            } catch (e: HttpException) {
                val error = "Error jaringan: ${e.message}"
                _errorMessage.value = error
                onError(error)
                Log.e(TAG, "HttpException: ${e.message}", e)
            } catch (e: IOException) {
                val error = "Koneksi gagal, periksa internet Anda"
                _errorMessage.value = error
                onError(error)
                Log.e(TAG, "IOException: ${e.message}", e)
            } catch (e: Exception) {
                val error = "Error: ${e.message}"
                _errorMessage.value = error
                onError(error)
                Log.e(TAG, "Exception: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun transactionsFor(customerId: Long): List<Transaction> =
        transactions.filter { it.customerId == customerId }

    fun finalizeAllTransactions(): Int {
        // Sudah tersimpan di backend, tidak perlu action tambahan
        return transactions.size
    }
}
