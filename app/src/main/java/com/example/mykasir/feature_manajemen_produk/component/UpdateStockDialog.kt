package com.example.mykasir.feature_manajemen_produk.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mykasir.feature_manajemen_produk.model.Product


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateStockDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    // State untuk menyimpan isi TextField
    var newStockText by remember { mutableStateOf(product.stock.toString()) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(
                text = "Update stok produk",
                style = MaterialTheme.typography.titleMedium
            )
        },

        text = {
            Column {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = androidx.compose.ui.Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Stok saat ini: ${product.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Min: ${product.minStock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = androidx.compose.ui.Modifier.height(10.dp))

                OutlinedTextField(
                    value = newStockText,
                    onValueChange = {
                        newStockText = it
                        // Cek apakah input valid (angka)
                        isError = it.toIntOrNull() == null
                    },
                    label = { Text("Jumlah stok baru") },
                    singleLine = true,
                    // Tampilkan keyboard angka
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    supportingText = {
                        if (isError) {
                            Text(
                                text = "Harap masukkan angka yang valid",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            }
        },

        // Tombol Konfirmasi ("Simpan")
        confirmButton = {
            Button(
                onClick = {
                    val newStockValue = newStockText.toIntOrNull()
                    if (newStockValue != null) {
                        onConfirm(newStockValue)
                        onDismiss()
                    } else {
                        isError = true
                    }
                }
            ) {
                Text("Simpan")
            }
        },

        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}