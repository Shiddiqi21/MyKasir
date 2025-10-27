package com.example.mykasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.mykasir.feature_manajemen_produk.navigation.ManajemenProdukNav
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel
import com.example.mykasir.ui.theme.MyKasirTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyKasirApp()
        }
    }
}

@Composable
fun MyKasirApp() {
    MyKasirTheme {
        // Latar belakang dan warna tema global
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()

            // Gunakan ViewModel dengan cara Compose yang benar
            val viewModel: ProductViewModel = viewModel()

            // Navigasi utama modul Manajemen Produk
            ManajemenProdukNav(
                navController = navController,
                viewModel = viewModel
            )
        }
    }
}
