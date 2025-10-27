package com.example.mykasir.feature_manajemen_produk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack // <-- Ganti ke .filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
// import com.example.mykasir.ui.theme.PrimaryColor // <-- 1. HAPUS IMPORT INI

@Composable
fun HeaderBar(
    title: String,
    navController: NavController,
    showBackButton: Boolean = true,
    // --- 2. UBAH DEFAULT WARNA DI SINI ---
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 16.dp, horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBackButton) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        // Saya ganti ke 'ArrowBack' standar, 'AutoMirrored' lebih baik
                        // tapi 'filled' juga bisa
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = contentColor // <-- 3. Warna sudah benar dari parameter
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = contentColor, // <-- 4. Warna sudah benar dari parameter
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}