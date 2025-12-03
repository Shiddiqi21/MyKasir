package com.example.mykasir.feature_transaksi.screen

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.TextFieldDefaults
import com.example.mykasir.core_ui.LocalNotifier
import com.example.mykasir.core_ui.NotificationType
import com.example.mykasir.feature_transaksi.model.TransactionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransaksiFormScreen(
    viewModel: TransaksiViewModel,
    productViewModel: ProductViewModel,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    val notifier = LocalNotifier.current
    var showCustomerDialog by remember { mutableStateOf(false) }
    var customerNameInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showCartSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }
    var itemToDelete by remember { mutableStateOf<TransactionItem?>(null) }
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
            // Kontainer besar putih untuk area kategori + produk (desain awal)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                LabeledText(text = "Kategori Produk")
                val categories = productViewModel.products.map { it.category }.filter { it.isNotBlank() }.distinct()
                if (categories.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Chip "Semua"
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("Semua") },
                                shape = RoundedCornerShape(50),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                        // Chip kategori lain
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    selectedCategory = if (selectedCategory == cat) null else cat
                                },
                                label = { Text(cat) },
                                shape = RoundedCornerShape(50),
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
                            onAdd = {
                                viewModel.addOrIncreaseItem(p.name, p.price, p.stock)
                                notifier?.show("Produk ditambahkan ke keranjang", NotificationType.Success, 1500)
                            }
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
                                    notifier?.show("Transaksi berhasil disimpan", NotificationType.Success, 2000)
                                    onSaved(customerId)
                                }
                            }
                        ) {
                            Text(
                                "Simpan",
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCustomerDialog = false }) {
                            Text("Batal", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    title = {
                        Text(
                            "Nama Pelanggan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = customerNameInput,
                                onValueChange = { customerNameInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Masukkan nama pelanggan") }
                            )
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    containerColor = Color.White,
                    tonalElevation = 8.dp
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
                        Text(
                            "Keranjang",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (viewModel.currentItems.isNotEmpty()) {
                            Text(
                                "${viewModel.currentItems.size} item",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        if (viewModel.currentItems.isEmpty()) {
                            Text("Keranjang masih kosong.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        } else {
                            androidx.compose.foundation.lazy.LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.heightIn(max = 360.dp)
                            ) {
                                items(viewModel.currentItems.size) { index ->
                                    val it = viewModel.currentItems[index]
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(it.productName, fontWeight = FontWeight.SemiBold)
                                                Text(
                                                    formatRupiah(it.unitPrice),
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                                val p = productViewModel.products.firstOrNull { p ->
                                                    p.name.equals(it.productName, true)
                                                }
                                                val max = p?.stock ?: Int.MAX_VALUE
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier.padding(top = 4.dp)
                                                ) {
                                                    OutlinedIconButton(
                                                        onClick = { viewModel.changeQuantity(it, -1, max) },
                                                        enabled = it.quantity > 1,
                                                        shape = RoundedCornerShape(50)
                                                    ) {
                                                        Icon(Icons.Filled.Remove, contentDescription = null)
                                                    }
                                                    Text(it.quantity.toString())
                                                    OutlinedIconButton(
                                                        onClick = { viewModel.changeQuantity(it, +1, max) },
                                                        enabled = it.quantity < max,
                                                        shape = RoundedCornerShape(50)
                                                    ) {
                                                        Icon(Icons.Filled.Add, contentDescription = null)
                                                    }
                                                }
                                            }
                                            Column(horizontalAlignment = Alignment.End) {
                                                Text("Subtotal", style = MaterialTheme.typography.labelSmall)
                                                Text(
                                                    formatRupiah(it.unitPrice * it.quantity),
                                                    fontWeight = FontWeight.Bold
                                                )
                                                TextButton(onClick = { itemToDelete = it }) {
                                                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        formatRupiah(viewModel.total),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                            Button(
                                onClick = {
                                    showCartSheet = false
                                    customerNameInput = ""
                                    showCustomerDialog = true
                                },
                                enabled = viewModel.currentItems.isNotEmpty(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                shape = RoundedCornerShape(50.dp)
                            ) {
                                Text("Lanjutkan", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // Dialog konfirmasi hapus item dari keranjang
        val pendingToDelete = itemToDelete
        if (pendingToDelete != null) {
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.removeItem(pendingToDelete)
                            itemToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Hapus", fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { itemToDelete = null },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }
                },
                title = {
                    Text(
                        text = "Hapus dari keranjang",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                text = {
                    Text(
                        text = "Yakin ingin menghapus ${pendingToDelete.productName} dari keranjang?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                tonalElevation = 0.dp
            )
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(3.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
            Text(
                name,
                maxLines = 1,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(formatRupiah(price), style = MaterialTheme.typography.labelMedium)
                    val stockText = if (stock > 0) "Stok: $stock" else "Habis"
                    val stockColor = if (stock > 0) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.error
                    Text(stockText, style = MaterialTheme.typography.labelSmall, color = stockColor)
                }
                Button(
                    onClick = onAdd,
                    enabled = stock > 0,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.heightIn(min = 32.dp)
                ) {
                    Text("Tambah", fontWeight = FontWeight.SemiBold)
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
