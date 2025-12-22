package com.example.mykasir.feature_transaksi.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mykasir.R
import com.example.mykasir.core_ui.LocalNotifier
import com.example.mykasir.core_ui.NotificationType
import com.example.mykasir.core_ui.formatRupiah
import com.example.mykasir.feature_transaksi.model.Customer
import com.example.mykasir.feature_transaksi.viewmodel.TransaksiViewModel

// Sort options enum
enum class TransactionSortOption(val label: String) {
    NEWEST("Terbaru"),
    OLDEST("Terlama"),
    HIGHEST_AMOUNT("Jumlah Tertinggi"),
    LOWEST_AMOUNT("Jumlah Terendah"),
    NAME_AZ("Nama A-Z"),
    NAME_ZA("Nama Z-A")
}

@Composable
fun TransaksiListScreen(
    viewModel: TransaksiViewModel,
    onTambahTransaksi: () -> Unit,
    onDetail: (Customer) -> Unit
) {
    val notifier = LocalNotifier.current
    var customerToDelete by remember { mutableStateOf<Customer?>(null) }
    
    // Search and Sort state
    var searchQuery by remember { mutableStateOf("") }
    var selectedSortOption by remember { mutableStateOf(TransactionSortOption.NEWEST) }
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        // HEADER dengan warna sama seperti header beranda + tombol tambah
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.mykasir_logo),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(70.dp)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Transaksi",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Kelola transaksi dan pelanggan",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.width(28.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onTambahTransaksi,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Tambah Transaksi",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Konten putih melengkung
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Riwayat Transaksi",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Daftar pelanggan yang pernah bertransaksi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari pelanggan...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Sort Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Urutkan: ${selectedSortOption.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Urutkan",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            TransactionSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        selectedSortOption = option
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (option == selectedSortOption) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Filter and Sort customers
                val transactedCustomers by remember(viewModel.customers, viewModel.transactions, searchQuery, selectedSortOption) {
                    derivedStateOf {
                        viewModel.customers
                            .filter { c -> viewModel.transactionsFor(c.id).isNotEmpty() }
                            .filter { c -> 
                                searchQuery.isBlank() || c.name.contains(searchQuery, ignoreCase = true)
                            }
                            .let { list ->
                                when (selectedSortOption) {
                                    TransactionSortOption.NEWEST -> list.sortedByDescending { c ->
                                        viewModel.transactionsFor(c.id).maxOfOrNull { it.timestamp } ?: 0L
                                    }
                                    TransactionSortOption.OLDEST -> list.sortedBy { c ->
                                        viewModel.transactionsFor(c.id).minOfOrNull { it.timestamp } ?: Long.MAX_VALUE
                                    }
                                    TransactionSortOption.HIGHEST_AMOUNT -> list.sortedByDescending { c ->
                                        viewModel.transactionsFor(c.id).sumOf { it.total }
                                    }
                                    TransactionSortOption.LOWEST_AMOUNT -> list.sortedBy { c ->
                                        viewModel.transactionsFor(c.id).sumOf { it.total }
                                    }
                                    TransactionSortOption.NAME_AZ -> list.sortedBy { it.name.lowercase() }
                                    TransactionSortOption.NAME_ZA -> list.sortedByDescending { it.name.lowercase() }
                                }
                            }
                    }
                }

                if (transactedCustomers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.mykasir_logo),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Belum ada transaksi yang tersimpan",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(transactedCustomers) { customer ->
                            val txs = viewModel.transactionsFor(customer.id)
                            CustomerRow(
                                customer = customer,
                                onDetail = { onDetail(customer) },
                                onDelete = { customerToDelete = customer },
                                txIds = txs.map { it.id }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog konfirmasi hapus riwayat transaksi pelanggan
    val pending = customerToDelete
    if (pending != null) {
        com.example.mykasir.core_ui.ConfirmationDialog(
            title = "Hapus Riwayat",
            message = "Apakah Anda yakin ingin menghapus riwayat transaksi ${pending.name}?",
            confirmText = "Hapus",
            dismissText = "Batal",
            type = com.example.mykasir.core_ui.DialogType.Delete,
            onConfirm = {
                viewModel.removeCustomer(pending)
                notifier?.show("Riwayat transaksi dihapus", NotificationType.Success, 1500)
                customerToDelete = null
            },
            onDismiss = { customerToDelete = null }
        )
    }
}

@Composable
private fun CustomerRow(
    customer: Customer,
    onDetail: () -> Unit,
    onDelete: () -> Unit,
    txIds: List<Long>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F7)),
        elevation = CardDefaults.cardElevation(0.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    customer.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val label = if (txIds.isEmpty()) {
                    "Nomor Transaksi: -"
                } else {
                    val short = txIds.map { "#" + it.toString().takeLast(6) }
                    val display = if (short.size > 3) short.takeLast(3) else short
                    "Nomor Transaksi: " + display.joinToString(", ") + if (short.size > 3) " +${short.size - 3} lagi" else ""
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (txIds.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "${txIds.size} transaksi",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.width(4.dp))
                FilledTonalIconButton(
                    onClick = onDetail,
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = "Detail",
                        modifier = Modifier.size(18.dp)
                    )
                }
                FilledTonalIconButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Hapus",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
