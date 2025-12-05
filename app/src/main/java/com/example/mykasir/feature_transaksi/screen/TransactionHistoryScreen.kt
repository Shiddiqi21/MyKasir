package com.example.mykasir.feature_transaksi.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.speech.tts.TextToSpeech
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
    val latest = transactions.maxByOrNull { it.timestamp }
    val context = LocalContext.current
    
    // State untuk TTS
    val tts = remember { TextToSpeech(context, null) }
    var isSpeaking by remember { mutableStateOf(false) }
    
    // Teks yang akan dibaca
    val receiptText = latest?.let { tx ->
        buildString {
            append("Pelanggan $customerName. ")
            append("Barang dibeli: ")
            tx.items.forEachIndexed { index, item ->
                append("${item.productName} ")
                append("${formatRupiah(item.unitPrice * item.quantity)}")
                if (index != tx.items.lastIndex) append(", ")
            }
            append(". Total ${formatRupiah(tx.total)}.")
        }
    } ?: ""
    
    // Fungsi untuk mengontrol pembacaan
    fun toggleSpeak() {
        if (isSpeaking) {
            tts.stop()
            isSpeaking = false
        } else if (receiptText.isNotEmpty()) {
            tts.language = Locale("id", "ID")
            tts.speak(receiptText, TextToSpeech.QUEUE_FLUSH, null, "receipt_${latest?.id}")
            isSpeaking = true
        }
    }
    
    // Auto-baca saat layar pertama dibuka
    LaunchedEffect(Unit) {
        if (receiptText.isNotEmpty()) {
            tts.language = Locale("id", "ID")
            tts.speak(receiptText, TextToSpeech.QUEUE_FLUSH, null, "receipt_${latest?.id}")
            isSpeaking = true
        }
    }
    
    // Cleanup TTS saat komponen dihancurkan
    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    Scaffold(
        topBar = { 
            TopAppBar(
                title = { 
                    Text(
                        text = "Riwayat Transaksi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary 
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onPrimary 
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { toggleSpeak() },
                        enabled = latest != null
                    ) {
                        Icon(
                            if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isSpeaking) "Berhenti" else "Bacakan struk",
                            tint = if (latest != null) 
                                if (isSpeaking) MaterialTheme.colorScheme.error 
                                else MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary, 
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary, 
                    titleContentColor = MaterialTheme.colorScheme.onPrimary 
                ),
                windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.primary
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Belum ada transaksi",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(transactions) { tx ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F7)),
                            elevation = CardDefaults.cardElevation(0.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    customerName,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 16.sp
                                )

                                Text(
                                    text = "Detail transaksi",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )

                                val ts = sdf.format(Date(tx.timestamp))
                                val txNum = "#" + tx.id.toString().takeLast(6)
                                Text(
                                    text = "Waktu: $ts",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "Nomor Transaksi: $txNum",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Header tabel dengan background lembut
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.04f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Produk",
                                            modifier = Modifier.weight(1.9f),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Harga",
                                            modifier = Modifier.weight(1.5f),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        Text(
                                            text = "Jml",
                                            modifier = Modifier.weight(0.8f),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Subtotal",
                                            modifier = Modifier.weight(1.4f),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                // Baris-baris item flat tanpa garis grid
                                tx.items.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.productName,
                                            modifier = Modifier.weight(1.9f),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = formatRupiah(item.unitPrice),
                                            modifier = Modifier.weight(1.5f),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                        )
                                        Text(
                                            text = item.quantity.toString(),
                                            modifier = Modifier.weight(0.8f),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                        )
                                        Text(
                                            text = formatRupiah(item.unitPrice * item.quantity),
                                            modifier = Modifier.weight(1.4f),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "Total",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                            )
                                            Text(
                                                text = formatRupiah(tx.total),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
