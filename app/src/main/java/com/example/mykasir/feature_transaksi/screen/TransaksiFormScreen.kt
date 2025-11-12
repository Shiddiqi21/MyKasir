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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TextFieldDefaults

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransaksiFormScreen(
    viewModel: TransaksiViewModel,
    productViewModel: ProductViewModel,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    var showCustomerDialog by remember { mutableStateOf(false) }
    var customerNameInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showCartSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    Scaffold(
        topBar = {
            SimpleTopBar(
                title = "Transaksi",
                onBackPress = onBack,
                actions = {
                    BadgedBox(badge = {
                        if (viewModel.currentItems.isNotEmpty()) Badge { Text(viewModel.currentItems.size.toString()) }
                    }) {
                        IconButton(onClick = { showCartSheet = true }) {
                            Icon(Icons.Filled.ShoppingCart, contentDescription = "Keranjang", tint = MaterialTheme.colorScheme.onPrimary)
                        }
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
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Kategori & Galeri Produk
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LabeledText(text = "Kategori Produk")
                    val categories = productViewModel.products.map { it.category }.filter { it.isNotBlank() }.distinct()
                    if (categories.isNotEmpty()) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(categories) { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = {
                                        selectedCategory = if (selectedCategory == cat) null else cat
                                    },
                                    label = { Text(cat) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        labelColor = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }
                    }

                    // Search bar untuk filter produk
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Cari produk...") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = MaterialTheme.colorScheme.outline,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        )
                    )

                    val list = remember(selectedCategory, productViewModel.products, searchQuery) {
                        val base = if (selectedCategory.isNullOrBlank()) productViewModel.products else productViewModel.products.filter { it.category.equals(selectedCategory, true) }
                        if (searchQuery.isBlank()) base else base.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(120.dp),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(list) { p ->
                            ProductGridItem(
                                name = p.name,
                                price = p.price,
                                stock = p.stock,
                                imageUri = p.imageUri,
                                onAdd = { viewModel.addOrIncreaseItem(p.name, p.price, p.stock) }
                            )
                        }
                    }
                }
            }

            if (showCustomerDialog) {
                AlertDialog(
                    onDismissRequest = { showCustomerDialog = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val name = customerNameInput.trim()
                                if (name.isNotEmpty()) {
                                    val customerId = viewModel.saveTransaction(name)
                                    // Kurangi stok produk sesuai item di keranjang (yang barusan disimpan)
                                    viewModel.transactionsFor(customerId).lastOrNull()?.items?.forEach { item ->
                                        val p2 = productViewModel.products.firstOrNull { it.name.equals(item.productName, ignoreCase = true) }
                                        if (p2 != null) {
                                            val newStock = (p2.stock - item.quantity).coerceAtLeast(0)
                                            productViewModel.updateProduct(p2.copy(stock = newStock))
                                        }
                                    }
                                    viewModel.resetForm()
                                    showCustomerDialog = false
                                    onSaved(customerId)
                                }
                            }
                        ) { Text("Simpan") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCustomerDialog = false }) { Text("Batal") }
                    },
                    title = { Text("Nama Pelanggan") },
                    text = {
                        OutlinedTextField(
                            value = customerNameInput,
                            onValueChange = { customerNameInput = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Masukkan nama pelanggan") }
                        )
                    }
                )
            }

            if (showCartSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showCartSheet = false },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Keranjang", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        if (viewModel.currentItems.isEmpty()) {
                            Text("Keranjang masih kosong.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        } else {
                            androidx.compose.foundation.lazy.LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.heightIn(max = 360.dp)
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
                                            Text(it.productName, fontWeight = FontWeight.SemiBold)
                                            Text(formatRupiah(it.unitPrice))
                                            val p = productViewModel.products.firstOrNull { p -> p.name.equals(it.productName, true) }
                                            val max = p?.stock ?: Int.MAX_VALUE
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                                OutlinedIconButton(onClick = { viewModel.changeQuantity(it, -1, max) }, enabled = it.quantity > 1) {
                                                    Icon(Icons.Filled.Remove, contentDescription = null)
                                                }
                                                Text(it.quantity.toString())
                                                OutlinedIconButton(onClick = { viewModel.changeQuantity(it, +1, max) }, enabled = it.quantity < max) {
                                                    Icon(Icons.Filled.Add, contentDescription = null)
                                                }
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text("Subtotal", style = MaterialTheme.typography.labelSmall)
                                            Text(formatRupiah(it.unitPrice * it.quantity), fontWeight = FontWeight.Bold)
                                            TextButton(onClick = { viewModel.removeItem(it) }) { Text("Hapus", color = MaterialTheme.colorScheme.error) }
                                        }
                                    }
                                }
                            }
                            Divider()
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Total", style = MaterialTheme.typography.titleMedium)
                                Text(formatRupiah(viewModel.total), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Button(
                                onClick = {
                                    showCartSheet = false
                                    customerNameInput = ""
                                    showCustomerDialog = true
                                },
                                enabled = viewModel.currentItems.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("Lanjutkan") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductGridItem(
    name: String,
    price: Int,
    stock: Int,
    imageUri: String?,
    onAdd: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUri.isNullOrBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Text(name, maxLines = 1)
            Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(formatRupiah(price), style = MaterialTheme.typography.labelMedium)
                    val stockText = if (stock > 0) "Stok: $stock" else "Habis"
                    val stockColor = if (stock > 0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.error
                    Text(stockText, style = MaterialTheme.typography.labelSmall, color = stockColor)
                }
                Button(
                    onClick = onAdd,
                    enabled = stock > 0,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.heightIn(min = 32.dp)
                ) {
                    Text("Tambah")
                }
            }
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
