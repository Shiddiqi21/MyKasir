package com.example.mykasir.feature_manajemen_produk.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.* // <-- Import semua runtime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mykasir.core_ui.SimpleTopBar
import com.example.mykasir.core_ui.LocalNotifier
import com.example.mykasir.core_ui.NotificationType
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel
import com.example.mykasir.feature_manajemen_produk.component.UpdateStockDialog // <-- 1. IMPORT DIALOG
import com.example.mykasir.feature_manajemen_produk.model.Product // <-- 2. IMPORT MODEL PRODUK

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StokTipisScreen(navController: NavController, viewModel: ProductViewModel) {
    val notifier = LocalNotifier.current
    val lowStockProducts = viewModel.products.filter { it.stock < it.minStock }

    // --- 3. STATE UNTUK DIALOG ---
    // State untuk tahu dialog harus muncul atau tidak
    var showUpdateDialog by remember { mutableStateOf(false) }
    // State untuk menyimpan produk mana yang sedang dipilih
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    // -----------------------------

    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "Stok Menipis",
                onBackPress = { navController.popBackStack() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        // --- 4. TAMPILKAN DIALOG JIKA 'showUpdateDialog' ADALAH TRUE ---
        if (showUpdateDialog && selectedProduct != null) {
            UpdateStockDialog(
                product = selectedProduct!!,
                onDismiss = {
                    showUpdateDialog = false // Tutup dialog
                    selectedProduct = null // Bersihkan pilihan
                },
                onConfirm = { newStockValue ->
                    // Ambil produk yang dipilih dan update stoknya
                    val updatedProduct = selectedProduct!!.copy(stock = newStockValue)
                    // Panggil ViewModel untuk menyimpan
                    viewModel.updateProduct(
                        updatedProduct,
                        onSuccess = {
                            showUpdateDialog = false
                            selectedProduct = null
                            notifier?.show("Stok berhasil diupdate", NotificationType.Success, 1500)
                        },
                        onError = { error ->
                            showUpdateDialog = false
                            selectedProduct = null
                            notifier?.show(error, NotificationType.Error, 2000)
                        }
                    )
                }
            )
        }
        // -----------------------------------------------------------

        if (lowStockProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Semua stok produk aman 😊",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(lowStockProducts) { product ->
                    val isVeryLow = product.stock <= product.minStock / 2
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    if (isVeryLow) {
                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                                    shape = RoundedCornerShape(50.dp)
                                                )
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Sangat rendah",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = "Stok Minimum : ${product.minStock}",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Stok Saat Ini : ${product.stock}",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            // 🔹 Tombol Update
                            Button(
                                // --- 5. UBAH AKSI ONCLICK ---
                                onClick = {
                                    selectedProduct = product // Set produk yang dipilih
                                    showUpdateDialog = true // Tampilkan dialog
                                },
                                // ----------------------------
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = RoundedCornerShape(50.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "Update",
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
                // ... (item 'Segera update stok...')
            }
        }
    }
}