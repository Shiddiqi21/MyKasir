package com.example.mykasir.feature_manajemen_produk.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mykasir.feature_manajemen_produk.model.Product
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter

@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    // Tentukan warna kartu berdasarkan stok
    val cardColor = when {
        product.stock == 0 -> Color(0xFFFFCDD2) // Merah - stok habis
        product.stock <= product.minStock -> Color(0xFFFFE0E6) // Merah muda - stok tipis
        else -> Color.White // Normal
    }
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (!product.imageUri.isNullOrBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(product.imageUri),
                        contentDescription = null,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFECECEC))
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333)
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = "Rp${product.price}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    Text(
                        text = "Kategori: ${product.category}",
                        color = Color(0xFF6E6E6E),
                        style = MaterialTheme.typography.bodySmall
                    )
                    // Warna teks stok berdasarkan level
                    val stockColor = when {
                        product.stock == 0 -> Color(0xFFD32F2F) // Merah - habis
                        product.stock <= product.minStock -> Color(0xFFE65100) // Oranye - tipis
                        else -> Color.Gray
                    }
                    val stockText = when {
                        product.stock == 0 -> "Stok: Habis"
                        product.stock <= product.minStock -> "Stok: ${product.stock} (Tipis)"
                        else -> "Stok: ${product.stock}"
                    }
                    Text(
                        text = stockText,
                        color = stockColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (product.stock <= product.minStock) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFF6EC1E4))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color.Red)
                }
            }
        }
    }
}

