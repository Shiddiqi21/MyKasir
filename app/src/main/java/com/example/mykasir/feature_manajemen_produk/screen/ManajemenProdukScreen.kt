package com.example.mykasir.feature_manajemen_produk.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mykasir.R
import com.example.mykasir.feature_manajemen_produk.component.ProductCard
import com.example.mykasir.feature_manajemen_produk.component.ProductSearchBar
import com.example.mykasir.feature_manajemen_produk.navigation.Screen
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel

@Composable
fun ManajemenProdukScreen(navController: NavController, viewModel: ProductViewModel) {
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDF5F5))
    ) {
        // 🔹 Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6EC1E4))
                .padding(vertical = 16.dp, horizontal = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manajemen Produk",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                IconButton(onClick = { /* Arahkan ke profil */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = "Profil",
                        tint = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔹 Tombol Aksi (Stok Tipis dan Tambah)
        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { navController.navigate(Screen.StokTipis.route) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF87CEFA)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Warning, contentDescription = null, tint = Color.White)
                Text("Stok Tipis", color = Color.White, modifier = Modifier.padding(start = 6.dp))
            }

            Button(
                onClick = { navController.navigate(Screen.TambahProduk.route) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF87CEFA)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                Text("Tambah", color = Color.White, modifier = Modifier.padding(start = 6.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Search Bar
        ProductSearchBar(query = query, onQueryChange = { query = it })

        Spacer(modifier = Modifier.height(8.dp))

        // 🔹 Daftar Produk
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            viewModel.products
                .filter { it.name.contains(query, ignoreCase = true) }
                .forEach { product ->
                    ProductCard(
                        product = product,
                        onEdit = { /* Navigasi ke Edit Produk */ },
                        onDelete = { viewModel.deleteProduct(product) }
                    )
                }
        }
    }
}
