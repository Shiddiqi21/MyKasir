package com.example.mykasir.navigationapp // Package disesuaikan dengan kode Anda

import androidx.compose.foundation.layout.size
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState


data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun BottomNavBar(
    navController: NavController,
    items: List<NavItem>
) {
    NavigationBar(
        // Mengambil warna biru utama dari Tema Anda
        containerColor = MaterialTheme.colorScheme.primary,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label,
                        // Ukuran ikon tetap sesuai permintaan Anda
                        modifier = Modifier.size(37.dp)
                    )
                },
                label = null, // Tidak pakai label
                colors = NavigationBarItemDefaults.colors(
                    // Ikon aktif: Mengambil warna teks utama (hitam) dari Tema
                    selectedIconColor = MaterialTheme.colorScheme.onSurface,
                    // Ikon non-aktif: Mengambil warna teks di atas primary (putih) dari Tema
                    unselectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    // Indikator tetap transparan
                    indicatorColor = Color.Transparent // <-- 2. BARIS INI MEMBUTUHKAN IMPORT
                )
            )
        }
    }
}