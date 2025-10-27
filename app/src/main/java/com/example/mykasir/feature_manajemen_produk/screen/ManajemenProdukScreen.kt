package com.example.mykasir.feature_manajemen_produk.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mykasir.feature_manajemen_produk.component.ProductCard
import com.example.mykasir.feature_manajemen_produk.component.ProductSearchBar
import com.example.mykasir.feature_manajemen_produk.navigation.Screen
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenProdukScreen(navController: NavController, viewModel: ProductViewModel) {
    var query by remember { mutableStateOf("") }

    // Latar belakang utama mengambil dari tema
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
                Icon(
                    imageVector = Icons.Filled.Store,
                    contentDescription = "Logo MyKasir",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(40.dp)
                )
                Text(
                    text = "Manajemen Produk",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { /* Arahkan ke profil */ }) {
                    Icon(
                        imageVector = Icons.Filled.Person,
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
                        // --- 1. UBAH DI SINI ---
                        tint = MaterialTheme.colorScheme.primary // (Jadi Biru)
                    )
                    Text(
                        "Stok Tipis",
                        // --- 2. UBAH DI SINI ---
                        color = MaterialTheme.colorScheme.primary, // (Jadi Biru)
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
                        // --- 3. UBAH DI SINI ---
                        tint = MaterialTheme.colorScheme.primary, // (Jadi Biru)
                    )
                    Text(
                        "Tambah",
                        // --- 4. UBAH DI SINI ---
                        color = MaterialTheme.colorScheme.primary, // (Jadi Biru)
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

                // --- Search Bar ---
                ProductSearchBar(
                    query = query,
                    onQueryChange = { query = it },
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // --- Daftar Produk (LazyColumn) ---
                val filteredProducts = viewModel.products.filter { product ->
                    product.name.contains(query, ignoreCase = true) ||
                            product.category.contains(query, ignoreCase = true)
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
                            onDelete = { viewModel.deleteProduct(product) }
                        )
                    }
                }
            }
        }
    }
}