package com.example.mykasir.feature_manajemen_produk.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mykasir.feature_manajemen_produk.screen.ManajemenProdukScreen
import com.example.mykasir.feature_manajemen_produk.screen.TambahProdukScreen
import com.example.mykasir.feature_manajemen_produk.screen.StokTipisScreen
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel

// --- 1. UBAH 'TambahProduk' UNTUK MENANGANI ARGUMEN ---
sealed class Screen(val route: String) {
    object ManajemenProduk : Screen("manajemen_produk")
    object StokTipis : Screen("stok_tipis")

    // Rute ini sekarang mendefinisikan argumen opsional 'productId'
    object TambahProduk : Screen("tambah_produk?productId={productId}") {
        // Nama argumen untuk referensi
        const val ARG_PRODUCT_ID = "productId"
        // Rute dasar tanpa argumen (untuk mode Tambah)
        const val ROUTE_BASE = "tambah_produk"

        // Fungsi untuk membuat rute Edit
        fun createRoute(productId: Long) = "tambah_produk?productId=$productId"
    }
}

@Composable
fun ManajemenProdukNav(
    navController: NavHostController,
    viewModel: ProductViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ManajemenProduk.route
    ) {
        // Layar Utama
        composable(Screen.ManajemenProduk.route) {
            ManajemenProdukScreen(navController, viewModel)
        }

        // Layar Stok Tipis
        composable(Screen.StokTipis.route) {
            StokTipisScreen(navController, viewModel)
        }

        // --- 2. UBAH COMPOABLE 'TambahProduk' ---
        composable(
            route = Screen.TambahProduk.route, // Menggunakan rute dengan definisi argumen
            arguments = listOf(
                navArgument(Screen.TambahProduk.ARG_PRODUCT_ID) {
                    type = NavType.LongType
                    defaultValue = -1L // Nilai default jika tidak ada ID (mode Tambah)
                }
            )
        ) { backStackEntry ->
            // Ambil productId dari argumen
            val productId = backStackEntry.arguments?.getLong(Screen.TambahProduk.ARG_PRODUCT_ID)

            // Kirim productId ke layar
            TambahProdukScreen(
                navController = navController,
                viewModel = viewModel,
                productId = if (productId == -1L) null else productId
            )
        }
    }
}