package com.example.mykasir.navigationapp

import android.net.http.SslCertificate.saveState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// --- Import Fitur Anda ---
import com.example.mykasir.feature_manajemen_produk.navigation.ManajemenProdukNav
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel
import com.example.mykasir.feature_transaksi.navigation.TransaksiNav
import com.example.mykasir.feature_laporan.LaporanScreen
import com.example.mykasir.feature_home.DashboardHomeScreen
import com.example.mykasir.feature_transaksi.viewmodel.TransaksiViewModel
import com.example.mykasir.feature_laporan.SalesReportPage
import com.example.mykasir.feature_laporan.ChartPage
import com.example.mykasir.feature_profile.navigation.ProfileNav

// Definisikan item navigasi untuk Owner (semua menu)
val ownerNavItems = listOf(
    NavItem("Beranda", Icons.Filled.Home, "home"),
    NavItem("Stok", Icons.Filled.Inventory2, "package"),
    NavItem("Transaksi", Icons.Filled.Wallet, "wallet"),
    NavItem("Laporan", Icons.Filled.Description, "docs"),
    NavItem("Profil", Icons.Filled.Person, "profile")
)

// Definisikan item navigasi untuk Kasir (tanpa Laporan)
val cashierNavItems = listOf(
    NavItem("Beranda", Icons.Filled.Home, "home"),
    NavItem("Stok", Icons.Filled.Inventory2, "package"),
    NavItem("Transaksi", Icons.Filled.Wallet, "wallet"),
    NavItem("Profil", Icons.Filled.Person, "profile")
)

/**
 * Ini adalah Composable utama aplikasi Anda yang berisi Scaffold dan Navigasi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppHost(
    onLogout: () -> Unit = {}
) {
    // Check if user is owner
    val context = androidx.compose.ui.platform.LocalContext.current
    val tokenManager = remember { com.example.mykasir.core_data.local.TokenManager(context) }
    val isOwner = remember { tokenManager.isOwner() }
    
    // Pilih nav items berdasarkan role
    val navItems = if (isOwner) ownerNavItems else cashierNavItems
    
    // NavController ini untuk navigasi utama (Bottom Bar)
    val mainNavController = rememberNavController()
    // Hoist ViewModel agar dapat dibagikan lintas fitur
    val productViewModel: ProductViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val transaksiViewModel: TransaksiViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    Scaffold(
        bottomBar = {
            // Gunakan komponen Navbar reusable kita
            BottomNavBar(
                navController = mainNavController,
                items = navItems,
                isOwner = isOwner
            )
        }
    ) { innerPadding ->
        // NavHost utama (untuk berpindah antar tab)
        NavHost(
            navController = mainNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("home") {
                DashboardHomeScreen(
                    transaksiViewModel = transaksiViewModel,
                    productViewModel = productViewModel,
                    onOpenTransaksi = {
                        mainNavController.navigate("wallet") {
                            popUpTo(mainNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenProduk = {
                        mainNavController.navigate("package") {
                            popUpTo(mainNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenLaporan = {
                        mainNavController.navigate("docs") {
                            popUpTo(mainNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // Rute "package" akan menampilkan SELURUH alur fitur Manajemen Produk
            composable("package") {
                // Di sinilah kita memanggil NavGraph dari fitur Anda.
                // Kita buat NavController terpisah untuk alur internal fitur tsb.
                val featureNavController = rememberNavController()
                ManajemenProdukNav(
                    navController = featureNavController,
                    viewModel = productViewModel
                )
            }

            // Rute untuk tab-tab lainnya
            composable("wallet") {
                val txNavController = rememberNavController()
                TransaksiNav(
                    hostNavController = txNavController,
                    productViewModel = productViewModel,
                    transaksiViewModel = transaksiViewModel
                )
            }
            composable("docs") {
                val laporanNav = rememberNavController()
                NavHost(navController = laporanNav, startDestination = "laporan_home") {
                    composable("laporan_home") {
                        LaporanScreen(
                            viewModel = transaksiViewModel,
                            onOpenLaporan = { laporanNav.navigate("sales_report") },
                            onOpenGrafik = { laporanNav.navigate("chart_page") }
                        )
                    }
                    composable("sales_report") {
                        SalesReportPage(
                            onBack = { laporanNav.popBackStack() },
                            viewModel = transaksiViewModel
                        )
                    }
                    composable("chart_page") {
                        ChartPage(
                            onBack = { laporanNav.popBackStack() },
                            viewModel = transaksiViewModel
                        )
                    }
                }
            }
            // Rute profil dengan ProfileNav
            composable("profile") {
                ProfileNav(onLogout = onLogout)
            }

            // rute home kini di-handle oleh DashboardHomeScreen di atas
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