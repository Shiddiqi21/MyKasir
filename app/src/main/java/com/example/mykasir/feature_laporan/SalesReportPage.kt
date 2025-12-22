package com.example.mykasir.feature_laporan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.mykasir.core_ui.ReceiptPdfGenerator
import com.example.mykasir.core_ui.NotificationHelper
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp

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
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val monthKeyFormat = remember { SimpleDateFormat("yyyy-MM", Locale.getDefault()) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = selectedTime)
    val context = LocalContext.current

    // Agregasi data tabel sesuai mode dan tanggal terpilih
    val rows: List<List<String>> by remember(viewModel.transactions, selectedTime, mode) {
        mutableStateOf(buildRows(viewModel, selectedTime, mode))
    }

    // Export: CreateDocument launcher (CSV only - PDF uses ReceiptPdfGenerator)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Laporan Penjualan",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (mode == ReportModeSR.Daily) "Ringkasan penjualan harian" else "Ringkasan penjualan bulanan",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
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
            // Seluruh isi laporan bisa discroll sampai tombol ekspor
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Segmented toggle Harian / Bulanan + tanggal dalam satu kartu filter
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Periode Laporan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFE3ECF7),
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TogglePill(
                                text = "Harian",
                                selected = mode == ReportModeSR.Daily
                            ) { mode = ReportModeSR.Daily }
                            Spacer(Modifier.width(8.dp))
                            TogglePill(
                                text = "Bulanan",
                                selected = mode == ReportModeSR.Monthly
                            ) { mode = ReportModeSR.Monthly }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = if (mode == ReportModeSR.Daily) "Tanggal" else "Bulan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            val displayDate = if (mode == ReportModeSR.Daily) {
                                sdf.format(Date(selectedTime))
                            } else {
                                monthFormat.format(Date(selectedTime))
                            }
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = displayDate,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        Icons.Filled.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                if (showDatePicker) {
                    if (mode == ReportModeSR.Daily) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    val picked = dateState.selectedDateMillis
                                    if (picked != null) {
                                        selectedTime = picked
                                    }
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
                    } else {
                        MonthYearPickerDialog(
                            initialTime = selectedTime,
                            onDismiss = { showDatePicker = false },
                            onConfirm = { millis ->
                                selectedTime = millis
                                showDatePicker = false
                            }
                        )
                    }
                }

                // Tabel laporan dengan striping baris halus
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFBFD)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        TableHeader()
                        Divider(color = Color(0xFFE0E6F0), thickness = 1.dp)
                        rows.forEachIndexed { index, r ->
                            val bg = if (index % 2 == 0) Color(0xFFFFFFFF) else Color(0xFFF4F6FB)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(bg)
                            ) {
                                TableRow(r)
                            }
                            Divider(color = Color(0xFFE9EDF5), thickness = 0.5.dp)
                        }
                    }
                }

                // Ringkasan jumlah dan total
                val numericRows = remember(rows) { rows.filter { it.lastOrNull()?.toIntOrNull() != null } }
                val itemCount = numericRows.size
                val grandTotal = numericRows.sumOf { it.last().toInt() }

                if (mode == ReportModeSR.Monthly) {
                    Text(
                        text = monthFormat.format(Date(selectedTime)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEFF4FB), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Jumlah transaksi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text("$itemCount", fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text(
                            formatRupiah(grandTotal),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 18.sp
                        )
                    }
                }

                // Tombol aksi export
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Ekspor Laporan",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    // Generate PDF menggunakan ReceiptPdfGenerator (sama seperti struk)
                                    val pdfFile = ReceiptPdfGenerator.generateReport(
                                        context = context,
                                        title = "Laporan Penjualan",
                                        subtitle = if (mode == ReportModeSR.Daily) {
                                            "Tanggal: ${sdf.format(Date(selectedTime))}"
                                        } else {
                                            "Bulan: ${monthFormat.format(Date(selectedTime))}"
                                        },
                                        table = rows,
                                        totalTransactions = itemCount,
                                        grandTotal = grandTotal
                                    )
                                    pdfFile?.let { file ->
                                        // Simpan ke Downloads
                                        val saved = ReceiptPdfGenerator.saveToDownloads(context, file)
                                        if (saved) {
                                            NotificationHelper.showPdfNotification(context)
                                        }
                                        // Tampilkan share dialog
                                        ReceiptPdfGenerator.sharePdf(context, file)
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD32F2F),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Description,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("PDF", style = MaterialTheme.typography.labelMedium)
                            }

                            Button(
                                onClick = {
                                    val csv = buildCsv(rows)
                                    pendingCsv = csv
                                    csvLauncher.launch("Laporan_${sdf.format(Date(selectedTime))}.csv")
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF388E3C),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.TableChart,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Excel", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
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
        verticalAlignment = Alignment.CenterVertically
    ) {
        val headers = listOf("Tanggal", "Produk", "Qty", "Harga", "Subtotal")
        val weights = listOf(1.2f, 1.55f, 0.75f, 0.9f, 1.1f)

        headers.forEachIndexed { index, h ->
            val align = if (index >= 2) androidx.compose.ui.text.style.TextAlign.End else androidx.compose.ui.text.style.TextAlign.Start
            Text(
                text = h,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E2A3A),
                style = MaterialTheme.typography.labelSmall,
                textAlign = align,
                modifier = Modifier
                    .weight(weights[index])
            )
        }
    }
}

@Composable
private fun TableRow(cols: List<String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val weights = listOf(1.2f, 1.55f, 0.75f, 0.9f, 1.1f)

        cols.forEachIndexed { index, c ->
            val align = if (index >= 2) androidx.compose.ui.text.style.TextAlign.End else androidx.compose.ui.text.style.TextAlign.Start
            Text(
                text = c,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
                textAlign = align,
                modifier = Modifier
                    .weight(weights.getOrElse(index) { 1f })
            )
        }
    }
}

enum class ReportModeSR { Daily, Monthly }

private fun buildRows(vm: TransaksiViewModel, selectedTime: Long, mode: ReportModeSR): List<List<String>> {
    val sdfDate = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
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

@Composable
private fun MonthYearPickerDialog(
    initialTime: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val cal = remember(initialTime) {
        java.util.Calendar.getInstance().apply { timeInMillis = initialTime }
    }
    var selectedYear by remember { mutableStateOf(cal.get(java.util.Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(cal.get(java.util.Calendar.MONTH)) }

    val years = remember { (selectedYear - 5..selectedYear + 5).toList() }
    val monthNames = remember {
        (0..11).map { index ->
            java.text.DateFormatSymbols().months[index].take(3)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .widthIn(min = 260.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Pilih bulan dan tahun",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Pilih bulan
                Text(
                    text = "Bulan",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(12) { index ->
                        val isSelected = index == selectedMonth
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF1F1F1),
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable { selectedMonth = index }
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = monthNames[index],
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Pilih tahun
                Text(
                    text = "Tahun",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(years) { year ->
                        val isSelected = year == selectedYear
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF1F1F1),
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .clickable { selectedYear = year }
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = year.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        val calendar = java.util.Calendar.getInstance().apply {
                            set(java.util.Calendar.YEAR, selectedYear)
                            set(java.util.Calendar.MONTH, selectedMonth)
                            set(java.util.Calendar.DAY_OF_MONTH, 1)
                            set(java.util.Calendar.HOUR_OF_DAY, 0)
                            set(java.util.Calendar.MINUTE, 0)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                        }
                        onConfirm(calendar.timeInMillis)
                    }) {
                        Text("OK")
                    }
                }
            }
        }
    }
}
