package com.example.mykasir.feature_laporan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import com.example.mykasir.feature_transaksi.viewmodel.TransaksiViewModel
import java.io.OutputStream
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import com.example.mykasir.core_ui.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesReportPage(
    onBack: () -> Unit,
    viewModel: TransaksiViewModel
) {
    var mode by remember { mutableStateOf(ReportModeSR.Daily) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val sdf = remember { SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = selectedTime)
    val context = LocalContext.current

    // Agregasi data tabel sesuai mode dan tanggal terpilih
    val rows: List<List<String>> by remember(viewModel.transactions, selectedTime, mode) {
        mutableStateOf(buildRows(viewModel, selectedTime, mode))
    }

    // Export: CreateDocument launcher (CSV & PDF)
    var pendingCsv by remember { mutableStateOf<String?>(null) }
    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri: Uri? ->
        val csv = pendingCsv
        if (uri != null && csv != null) {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                os.write(csv.toByteArray())
            }
        }
        pendingCsv = null
    }

    var pendingPdf by remember { mutableStateOf<List<List<String>>?>(null) }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri: Uri? ->
        val data = pendingPdf
        if (uri != null && data != null) {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                writeSimplePdf(os, title = "Laporan Penjualan", subtitle = sdf.format(Date(selectedTime)), table = data)
            }
        }
        pendingPdf = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan Penjualan", color = MaterialTheme.colorScheme.onPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Segmented toggle Harian / Bulanan
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F1F1), RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TogglePill(text = "Harian", selected = mode == ReportModeSR.Daily) { mode = ReportModeSR.Daily }
                    Spacer(Modifier.width(8.dp))
                    TogglePill(text = "Bulanan", selected = mode == ReportModeSR.Monthly) { mode = ReportModeSR.Monthly }
                }

                // Date input dengan icon kalender
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = sdf.format(Date(selectedTime)),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Filled.CalendarMonth, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val picked = dateState.selectedDateMillis
                                if (picked != null) selectedTime = picked
                                showDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
                    ) {
                        DatePicker(
                            state = dateState,
                            showModeToggle = false
                        )
                    }
                }

                // Tabel contoh
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TableHeader()
                        LazyColumn { items(rows) { r -> TableRow(r) } }
                    }
                }

                // Ringkasan jumlah dan total
                val numericRows = remember(rows) { rows.filter { it.lastOrNull()?.toIntOrNull() != null } }
                val itemCount = numericRows.size
                val grandTotal = numericRows.sumOf { it.last().toInt() }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F7FA), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Jumlah: $itemCount", fontWeight = FontWeight.SemiBold)
                    Text("Total: ${formatRupiah(grandTotal)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                // Tombol aksi export
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            pendingPdf = rows
                            pdfLauncher.launch("Laporan_${sdf.format(Date(selectedTime))}.pdf")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) { Icon(Icons.Filled.Description, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("PDF") }

                    OutlinedButton(
                        onClick = {
                            val csv = buildCsv(rows)
                            pendingCsv = csv
                            csvLauncher.launch("Laporan_${sdf.format(Date(selectedTime))}.csv")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) { Icon(Icons.Filled.TableChart, contentDescription = null); Spacer(Modifier.width(8.dp)); Text("Excel") }
                }
            }
        }
    }
}

@Composable
fun TogglePill(text: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
    ) { Text(text) }
}

@Composable
private fun TableHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE9EEF3))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        listOf("Tanggal", "Produk", "Qty", "Harga", "Subtotal").forEach { h ->
            Text(h, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E2A3A))
        }
    }
}

@Composable
private fun TableRow(cols: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) { cols.forEach { c -> Text(c, color = MaterialTheme.colorScheme.onSurface) } }
}

enum class ReportModeSR { Daily, Monthly }

private fun buildRows(vm: TransaksiViewModel, selectedTime: Long, mode: ReportModeSR): List<List<String>> {
    val sdfDate = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dayStart = java.util.Calendar.getInstance().apply {
        timeInMillis = selectedTime
        set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
    }.timeInMillis
    return when (mode) {
        ReportModeSR.Daily -> {
            val dayEnd = dayStart + 24L*60*60*1000L
            val items = vm.transactions.filter { it.timestamp in dayStart until dayEnd }
            val rows = mutableListOf<List<String>>()
            items.forEach { tx ->
                tx.items.forEach { itx ->
                    rows.add(listOf(
                        sdfDate.format(Date(tx.timestamp)),
                        itx.productName,
                        itx.quantity.toString(),
                        itx.unitPrice.toString(),
                        (itx.unitPrice * itx.quantity).toString()
                    ))
                }
            }
            if (rows.isEmpty()) listOf(listOf("-","-","-","-","-")) else rows
        }
        ReportModeSR.Monthly -> {
            val cal = java.util.Calendar.getInstance().apply { timeInMillis = selectedTime; set(java.util.Calendar.DAY_OF_MONTH,1); set(java.util.Calendar.HOUR_OF_DAY,0); set(java.util.Calendar.MINUTE,0); set(java.util.Calendar.SECOND,0); set(java.util.Calendar.MILLISECOND,0) }
            val start = cal.timeInMillis
            cal.add(java.util.Calendar.MONTH, 1)
            val end = cal.timeInMillis
            val items = vm.transactions.filter { it.timestamp in start until end }
            val rows = mutableListOf<List<String>>()
            items.forEach { tx ->
                tx.items.forEach { itx ->
                    rows.add(listOf(
                        sdfDate.format(Date(tx.timestamp)),
                        itx.productName,
                        itx.quantity.toString(),
                        itx.unitPrice.toString(),
                        (itx.unitPrice * itx.quantity).toString()
                    ))
                }
            }
            if (rows.isEmpty()) listOf(listOf("-","-","-","-","-")) else rows
        }
    }
}

private fun buildCsv(rows: List<List<String>>): String {
    val header = listOf("Tanggal","Produk","Qty","Harga","Subtotal")
    val sb = StringBuilder()
    sb.append(header.joinToString(",")).append('\n')
    rows.forEach { r -> sb.append(r.joinToString(",")).append('\n') }
    return sb.toString()
}

private fun writeSimplePdf(os: OutputStream, title: String, subtitle: String, table: List<List<String>>) {
    val doc = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 approx
    val page = doc.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint().apply { isAntiAlias = true; textSize = 14f }
    var y = 40f
    paint.textSize = 18f; canvas.drawText(title, 40f, y, paint); y += 24f
    paint.textSize = 12f; canvas.drawText(subtitle, 40f, y, paint); y += 20f
    // header
    paint.textSize = 12f; paint.isFakeBoldText = true
    val header = listOf("Tanggal","Produk","Qty","Harga","Subtotal")
    var x = 40f
    val colW = floatArrayOf(90f, 180f, 50f, 80f, 90f)
    header.forEachIndexed { i, h -> canvas.drawText(h, x, y, paint); x += colW[i] }
    paint.isFakeBoldText = false
    y += 18f
    table.forEach { r ->
        x = 40f
        r.forEachIndexed { i, v -> canvas.drawText(v, x, y, paint); x += colW.getOrElse(i){80f} }
        y += 16f
        if (y > 800f) { // simple paginate ignore in demo
            return@forEach
        }
    }
    doc.finishPage(page)
    doc.writeTo(os)
    doc.close()
}
