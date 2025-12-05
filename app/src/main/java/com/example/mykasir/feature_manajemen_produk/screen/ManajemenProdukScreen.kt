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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManajemenProdukScreen(navController: NavController, viewModel: ProductViewModel) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    val notifier = LocalNotifier.current
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        label = "stokContentAlpha"
    )
    val contentOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 12.dp,
        label = "stokContentOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        // HEADER BIRU (konsisten dengan beranda, tanpa ikon profil)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.mykasir_logo),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(70.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Stok Produk",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Pantau persediaan barang di tokomu",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.width(28.dp)) // ruang kosong pengganti ikon profil
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
                    shape = RoundedCornerShape(50.dp),
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
                    shape = RoundedCornerShape(50.dp),
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
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = contentOffset.toPx()
                },
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 20.dp)
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
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Deretan chip kategori (dengan opsi "Semua")
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Chip "Semua" untuk menampilkan semua produk
                        item {
                            FilterChip(
                                selected = true,
                                onClick = { selectedCategory = null },
                                shape = RoundedCornerShape(50.dp),
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
                                shape = RoundedCornerShape(50.dp),
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
                        modifier = Modifier
                            .fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Daftar semua produk difilter oleh query saja
                    val filteredProducts = viewModel.products.filter { product ->
                        query.isBlank() || product.name.contains(query, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
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
                            .fillMaxWidth(),
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
                                shape = RoundedCornerShape(50.dp),
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
                                shape = RoundedCornerShape(50.dp),
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
                        modifier = Modifier
                            .fillMaxWidth()
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

        // Konfirmasi hapus produk (popup modern)
        val pending = productToDelete
        if (pending != null) {
            AlertDialog(
                onDismissRequest = { productToDelete = null },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                title = {
                    // Kita akan bungkus konten di dalam Surface bercorak
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFFFDFBFF)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .padding(horizontal = 20.dp, vertical = 20.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Aksen garis kecil di atas
                                Box(
                                    modifier = Modifier
                                        .width(42.dp)
                                        .height(3.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                                            shape = RoundedCornerShape(50)
                                        )
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(
                                            brush = Brush.radialGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                                                    Color.Transparent
                                                )
                                            ),
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color.White.copy(alpha = 0.9f)
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Yakin hapus produk ini?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = pending.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Akan dihapus dari daftar stok.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Tindakan ini tidak dapat dibatalkan.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.9f)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { productToDelete = null },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("Batal")
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.deleteProduct(
                                                pending,
                                                onSuccess = {
                                                    notifier?.show("Produk dihapus", NotificationType.Success, 1500)
                                                    productToDelete = null
                                                },
                                                onError = { error ->
                                                    notifier?.show(error, NotificationType.Error, 2000)
                                                    productToDelete = null
                                                }
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text("Ya, hapus")
                                    }
                                }
                            }
                        }
                    }
                },
                text = {},
                confirmButton = {},
                dismissButton = {}
            )
        }
    }   
}

