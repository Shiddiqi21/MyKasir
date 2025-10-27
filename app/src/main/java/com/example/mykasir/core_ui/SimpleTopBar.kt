package com.example.mykasir.core_ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Ini adalah TopAppBar (Header) simpel yang BISA DIPAKAI DI SEMUA FITUR.
 * Diletakkan di 'core_ui' agar bisa di-import oleh modul manapun.
 *
 * @param title Judul yang akan ditampilkan di tengah.
 * @param onBackPress Aksi yang akan dijalankan saat panah kembali diklik.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(
    title: String,
    onBackPress: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary // <-- GANTI
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackPress) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onPrimary // <-- GANTI
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary, // <-- GANTI
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary, // <-- GANTI
            titleContentColor = MaterialTheme.colorScheme.onPrimary // <-- GANTI
        )
    )
}