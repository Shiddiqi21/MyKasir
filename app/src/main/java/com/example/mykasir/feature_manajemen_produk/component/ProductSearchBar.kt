package com.example.mykasir.feature_manajemen_produk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ProductSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier // <-- 1. TAMBAHKAN PARAMETER INI
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Cari Produk") }, // Sesuai desain Anda
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Cari",
                tint = Color(0xFF6EC1E4)
            )
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF6EC1E4),
            unfocusedBorderColor = Color(0xFFE0E0E0),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = Color(0xFF6EC1E4)
        ),
        shape = RoundedCornerShape(20.dp),
        singleLine = true, // Tambahan bagus agar tidak jadi multi-baris
        modifier = modifier // <-- 2. TERAPKAN MODIFIER DARI PARAMETER
            .fillMaxWidth()
            .background(Color.Transparent)
    )
}