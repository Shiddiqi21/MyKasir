package com.example.mykasir.feature_manajemen_produk.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
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

        // Judul Pop-up
        title = {
            Text(text = "Update Stok ${product.name}")
        },

        // Konten (TextField)
        text = {
            Column {
                Text(text = "Masukkan jumlah stok yang baru.")
                OutlinedTextField(
                    value = newStockText,
                    onValueChange = {
                        newStockText = it
                        // Cek apakah input valid (angka)
                        isError = it.toIntOrNull() == null
                    },
                    label = { Text("Jumlah Stok Baru") },
                    singleLine = true,
                    // Tampilkan keyboard angka
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("Harap masukkan angka")
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
                        onConfirm(newStockValue) // Kirim nilai baru
                        onDismiss() // Tutup dialog
                    } else {
                        isError = true // Tampilkan error jika masih salah
                    }
                }
            ) {
                Text("Simpan")
            }
        },

        // Tombol Batal
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}