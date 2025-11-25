package com.example.mykasir.feature_transaksi.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mykasir.core_ui.SimpleTopBar
import com.example.mykasir.core_ui.formatRupiah
import com.example.mykasir.feature_transaksi.viewmodel.TransaksiViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: TransaksiViewModel,
    customerId: Long,
    onBack: () -> Unit
) {
    val transactions = viewModel.transactionsFor(customerId)
    val customerName = viewModel.customers.firstOrNull { it.id == customerId }?.name ?: "Pelanggan"
    val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())

    Scaffold(
        topBar = { SimpleTopBar(title = "Riwayat Transaksi", onBackPress = onBack) }
    ) { innerPadding ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada transaksi", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(transactions) { tx ->
                    Card(
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(customerName, fontWeight = FontWeight.Bold)
                            val ts = sdf.format(Date(tx.timestamp))
                            val txNum = "#" + tx.id.toString().takeLast(6)
                            Text("Waktu: $ts")
                            Text("Nomor Transaksi: $txNum")
                            Divider()
                            tx.items.forEachIndexed { index, item ->
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("Produk: ${item.productName}")
                                    Text("Harga Satuan: ${formatRupiah(item.unitPrice)}")
                                    Text("Jumlah: ${item.quantity}")
                                    Text("Subtotal: ${formatRupiah(item.unitPrice * item.quantity)}")
                                }
                                if (index != tx.items.lastIndex) {
                                    Divider(modifier = Modifier.padding(vertical = 6.dp))
                                }
                            }
                            Divider()
                            Text("Total: ${formatRupiah(tx.total)}", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
