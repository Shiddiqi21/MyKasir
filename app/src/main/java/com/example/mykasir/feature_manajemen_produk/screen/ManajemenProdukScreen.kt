package com.example.mykasir.feature_manajemen_produk.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
// import androidx.compose.material.icons.filled.Person // Tidak dipakai, kita pakai drawable
// import androidx.compose.material.icons.filled.Store // Tidak dipakai, kita pakai drawable
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource // <-- IMPORT INI
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mykasir.R // <-- IMPORT R (Resource)
import com.example.mykasir.feature_manajemen_produk.component.ProductCard
import com.example.mykasir.feature_manajemen_produk.component.ProductSearchBar
import com.example.mykasir.feature_manajemen_produk.navigation.Screen
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel
import com.example.mykasir.core_ui.LocalNotifier
import com.example.mykasir.core_ui.NotificationType
import com.example.mykasir.feature_manajemen_produk.model.Product
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenProdukScreen(navController: NavController, viewModel: ProductViewModel) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val notifier = LocalNotifier.current
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        // --- 1. HEADER KUSTOM ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // --- Baris Atas: Logo, Judul, Profil ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // --- 1. UBAH DI SINI: Gunakan painterResource ---
                Icon(
                    // Pastikan Anda punya file 'mykasir_logo' di drawable
                    painter = painterResource(id = R.drawable.mykasir_logo),
                    contentDescription = "Logo MyKasir",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(70.dp)
                )

                Text(
                    text = "Manajemen Produk",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { /* Arahkan ke profil */ }) {
                    // --- 2. UBAH DI SINI: Gunakan painterResource ---
                    Icon(
                        // Pastikan Anda punya file 'ic_person' di drawable
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = "Profil",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // --- Baris Bawah: Tombol Aksi ---
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tombol "Stok Tipis"
                Button(
                    onClick = { navController.navigate(Screen.StokTipis.route) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Stok Tipis",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }

                // Tombol "Tambah"
                Button(
                    onClick = { navController.navigate(Screen.TambahProduk.ROUTE_BASE) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        "Tambah",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        } // Akhir dari Header

        // --- 2. AREA KONTEN (PUTIH MELENGKUNG) ---
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 20.dp)
            ) {
                // Kategori unik dari produk
                val categories = viewModel.products
                    .map { it.category }
                    .filter { it.isNotBlank() }
                    .distinct()

                // Jika kategori belum dipilih, tampilkan pemilihan kategori terlebih dahulu
                if (selectedCategory == null) {
                    Text(
                        text = "Pilih Kategori",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Deretan chip kategori (dengan opsi "Semua")
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Chip "Semua" untuk menampilkan semua produk
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { selectedCategory = null },
                                label = { Text("Semua") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        // Chip kategori lainnya
                        items(categories) { category ->
                            FilterChip(
                                selected = false,
                                onClick = { selectedCategory = category },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search Bar untuk menampilkan semua produk saat "Semua" aktif
                    ProductSearchBar(
                        query = query,
                        onQueryChange = { query = it },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Daftar semua produk difilter oleh query saja
                    val filteredProducts = viewModel.products.filter { product ->
                        query.isBlank() || product.name.contains(query, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredProducts) { product ->
                            ProductCard(
                                product = product,
                                onEdit = {
                                    navController.navigate(Screen.TambahProduk.createRoute(product.id))
                                },
                                onDelete = { productToDelete = product }
                            )
                        }
                    }
                } else {
                    // Deretan chip kategori (tetap tampil) dengan state terpilih + opsi "Semua"
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Chip "Semua"
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = {
                                    selectedCategory = null
                                    query = ""
                                },
                                label = { Text("Semua") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = {
                                    selectedCategory = if (selectedCategory == category) null else category
                                    if (selectedCategory == null) query = ""
                                },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Search Bar (aktif setelah kategori dipilih)
                    ProductSearchBar(
                        query = query,
                        onQueryChange = { query = it },
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Daftar produk berdasarkan kategori (atau semua) + query
                    val filteredProducts = viewModel.products.filter { product ->
                        (selectedCategory == null || product.category.equals(selectedCategory, ignoreCase = true)) &&
                                (query.isBlank() || product.name.contains(query, ignoreCase = true))
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(filteredProducts) { product ->
                            ProductCard(
                                product = product,
                                onEdit = {
                                    navController.navigate(Screen.TambahProduk.createRoute(product.id))
                                },
                                onDelete = { productToDelete = product }
                            )
                        }
                    }
                }
            }
        }

        // Konfirmasi hapus produk (lebih kontekstual & menarik)
        val pending = productToDelete
        if (pending != null) {
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteProduct(pending)
                            notifier?.show("Produk dihapus", NotificationType.Success, 1500)
                            productToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Hapus") }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { productToDelete = null },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Batal") }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = { Text("Hapus Produk", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                text = { Text("Apakah Anda yakin ingin menghapus ${pending.name} (Kategori: ${pending.category})?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White,
                tonalElevation = 6.dp
            )
        }
    }
}

