package com.example.mykasir.feature_transaksi.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mykasir.R
import com.example.mykasir.core_ui.SimpleTopBar
import com.example.mykasir.core_ui.formatRupiah
import com.example.mykasir.feature_transaksi.viewmodel.TransaksiViewModel
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransaksiFormScreen(
    viewModel: TransaksiViewModel,
    productViewModel: ProductViewModel,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    val canSaveTop = viewModel.customerNameText.isNotBlank() && viewModel.currentItems.isNotEmpty()
    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "Transaksi",
                onBackPress = onBack,
                actions = {
                    TextButton(
                        onClick = {
                            val customerId = viewModel.saveTransaction(viewModel.customerNameText)
                            viewModel.resetForm()
                            onSaved(customerId)
                        },
                        enabled = canSaveTop,
                    ) {
                        Icon(imageVector = Icons.Filled.Save, contentDescription = "Simpan", tint = MaterialTheme.colorScheme.onPrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Kartu input
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LabeledText(text = "Nama Pelanggan")
                    OutlinedTextField(
                        value = viewModel.customerNameText,
                        onValueChange = { viewModel.customerNameText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    LabeledText(text = "Nama Produk")
                    var menuExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = menuExpanded,
                        onExpandedChange = { menuExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = viewModel.productName,
                            onValueChange = {
                                viewModel.productName = it
                                // reset harga jika user mengubah ketikan manual
                                viewModel.unitPriceText = ""
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            singleLine = true,
                            readOnly = false,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuExpanded)
                            }
                        )

                        ExposedDropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            val query = viewModel.productName.trim()
                            val filtered = if (query.isEmpty()) productViewModel.products
                            else productViewModel.products.filter { p ->
                                p.name.contains(query, ignoreCase = true)
                            }
                            filtered.forEach { p ->
                                DropdownMenuItem(
                                    text = { Text(p.name) },
                                    onClick = {
                                        viewModel.productName = p.name
                                        viewModel.unitPriceText = p.price.toString()
                                        menuExpanded = false
                                    }
                                )
                            }
                            if (filtered.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Tidak ada hasil") },
                                    onClick = { },
                                    enabled = false
                                )
                            }
                        }
                    }

                    LabeledText(text = "Harga Satuan")
                    OutlinedTextField(
                        value = viewModel.unitPriceText,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true,
                        placeholder = { Text("Pilih produk terlebih dahulu") }
                    )

                    LabeledText(text = "Jumlah Barang")
                    OutlinedTextField(
                        value = viewModel.quantityText,
                        onValueChange = { viewModel.quantityText = it.filter { ch -> ch.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // Tombol tambah item ke keranjang
                    val canAddItem = viewModel.productName.isNotBlank() && viewModel.unitPrice > 0 && viewModel.quantity > 0
                    Button(
                        onClick = { viewModel.addItemFromForm() },
                        enabled = canAddItem,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Tambah ke Daftar") }
                }
            }

            // Ringkasan: daftar item yang ditambahkan dan total
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (viewModel.currentItems.isEmpty()) {
                        Text("Belum ada item dalam daftar.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    } else {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.heightIn(max = 260.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(viewModel.currentItems.size) { index ->
                                val it = viewModel.currentItems[index]
                                Divider()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        SummaryRow(label = "Nama Produk", value = it.productName)
                                        SummaryRow(label = "Harga Satuan", value = formatRupiah(it.unitPrice))
                                        SummaryRow(label = "Jumlah Barang", value = it.quantity.toString())
                                        SummaryRow(label = "Subtotal", value = formatRupiah(it.unitPrice * it.quantity))
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.removeItem(it) },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Hapus")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Hapus")
                                    }
                                }
                            }
                        }
                        Divider()
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatRupiah(viewModel.total),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LabeledText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
    )
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
