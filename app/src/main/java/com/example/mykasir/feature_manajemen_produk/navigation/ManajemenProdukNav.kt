package com.example.mykasir.feature_manajemen_produk.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mykasir.feature_manajemen_produk.screen.ManajemenProdukScreen
import com.example.mykasir.feature_manajemen_produk.screen.TambahProdukScreen
import com.example.mykasir.feature_manajemen_produk.screen.StokTipisScreen
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel

sealed class Screen(val route: String) {
    object ManajemenProduk : Screen("manajemen_produk")
    object TambahProduk : Screen("tambah_produk")
    object StokTipis : Screen("stok_tipis")
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
        composable(Screen.ManajemenProduk.route) {
            ManajemenProdukScreen(navController, viewModel)
        }
        composable(Screen.TambahProduk.route) {
            TambahProdukScreen(navController, viewModel)
        }
        composable(Screen.StokTipis.route) {
            StokTipisScreen(navController, viewModel)
        }
    }
}
