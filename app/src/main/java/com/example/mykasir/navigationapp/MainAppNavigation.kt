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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// --- Import Fitur Anda ---
import com.example.mykasir.core_data.local.TokenManager
import com.example.mykasir.feature_manajemen_produk.navigation.ManajemenProdukNav
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel
import com.example.mykasir.feature_transaksi.navigation.TransaksiNav
import com.example.mykasir.feature_laporan.LaporanScreen
import com.example.mykasir.feature_home.DashboardHomeScreen
import com.example.mykasir.feature_transaksi.viewmodel.TransaksiViewModel
import com.example.mykasir.feature_laporan.SalesReportPage
import com.example.mykasir.feature_laporan.ChartPage
import com.example.mykasir.feature_profile.screen.ProfileScreen
import com.example.mykasir.feature_profile.viewmodel.ProfileViewModel
import com.example.mykasir.feature_collaborator.screen.CollaboratorScreen

// Nav items untuk Owner (akses penuh termasuk Laporan)
val ownerNavItems = listOf(
    NavItem("Beranda", Icons.Filled.Home, "home"),
    NavItem("Stok", Icons.Filled.Inventory2, "package"),
    NavItem("Transaksi", Icons.Filled.Wallet, "wallet"),
    NavItem("Laporan", Icons.Filled.Description, "docs"),
    NavItem("Profil", Icons.Filled.Person, "profile")
)

// Nav items untuk Cashier (tanpa Laporan)
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
fun MainAppHost(onLogout: () -> Unit = {}) {
    // Get user role from TokenManager
    val context = LocalContext.current
    val tokenManager = TokenManager(context)
    val isOwner = tokenManager.isOwner()
    
    // Select nav items based on role
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
                        // Hanya navigasi ke laporan jika owner
                        if (isOwner) {
                            mainNavController.navigate("docs") {
                                popUpTo(mainNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
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
            
            // Rute Laporan - hanya bisa diakses owner (tetap ada untuk keamanan)
            composable("docs") {
                if (isOwner) {
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
                } else {
                    // Jika kasir mencoba akses, tampilkan pesan
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Akses tidak tersedia untuk kasir")
                    }
                }
            }
            
            // Rute profil dengan data user yang login
            composable("profile") {
                val profileViewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                ProfileScreen(
                    viewModel = profileViewModel,
                    onLogout = onLogout,
                    onManageCollaborators = {
                        mainNavController.navigate("collaborators")
                    }
                )
            }
            
            // Rute collaborators (manage kasir) - hanya untuk owner
            composable("collaborators") {
                CollaboratorScreen(
                    onBack = { mainNavController.popBackStack() }
                )
            }
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
