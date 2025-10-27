package com.example.mykasir.navigationapp

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// --- Import Fitur Anda ---
import com.example.mykasir.feature_manajemen_produk.navigation.ManajemenProdukNav
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel

// Definisikan item navigasi Anda
val kasirNavItems = listOf(
    NavItem("Home", Icons.Filled.Home, "home"),
    NavItem("Wallet", Icons.Filled.Wallet, "wallet"),
    NavItem("Docs", Icons.Filled.Description, "docs"),
    NavItem("Package", Icons.Filled.Inventory2, "package")
)

/**
 * Ini adalah Composable utama aplikasi Anda yang berisi Scaffold dan Navigasi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppHost() {
    // NavController ini untuk navigasi utama (Bottom Bar)
    val mainNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            // Gunakan komponen Navbar reusable kita
            BottomNavBar(
                navController = mainNavController,
                items = kasirNavItems
            )
        }
    ) { innerPadding ->
        // NavHost utama (untuk berpindah antar tab)
        NavHost(
            navController = mainNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding) // Padding dari Scaffold
        ) {

            // Rute "home" akan menampilkan SELURUH alur fitur Manajemen Produk
            composable("package") {
                // Di sinilah kita memanggil NavGraph dari fitur Anda.
                // Kita buat NavController terpisah untuk alur internal fitur tsb.
                val featureNavController = rememberNavController()
                val productViewModel: ProductViewModel = viewModel()

                ManajemenProdukNav(
                    navController = featureNavController,
                    viewModel = productViewModel
                )
            }

            // Rute untuk tab-tab lainnya
            composable("wallet") { PlaceholderScreen("Halaman Dompet") }
            composable("docs") { PlaceholderScreen("Halaman Dokumen") }
            composable("home") { PlaceholderScreen("Halaman Home") }
        }
    }
}

/**
 * Composable sederhana untuk mengisi layar lain
 */
@Composable
fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text)
    }
}