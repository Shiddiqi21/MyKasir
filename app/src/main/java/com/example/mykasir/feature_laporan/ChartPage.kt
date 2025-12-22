package com.example.mykasir.feature_laporan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.mykasir.feature_transaksi.viewmodel.TransaksiViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartPage(
    onBack: () -> Unit,
    viewModel: TransaksiViewModel
) {
    var mode by remember { mutableStateOf(ReportModeSR.Daily) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf(System.currentTimeMillis()) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = selectedTime)
    val sdf = remember { SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()) }
    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Grafik Penjualan",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (mode == ReportModeSR.Daily) {
                                "Tren penjualan per jam"
                            } else {
                                "Tren penjualan per hari"
                            },
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Kartu filter periode
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

                            Spacer(modifier = Modifier.width(8.dp))

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

                // Dialog tanggal / bulan
                if (showDatePicker) {
                    if (mode == ReportModeSR.Daily) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        dateState.selectedDateMillis?.let { picked ->
                                            selectedTime = picked
                                        }
                                        showDatePicker = false
                                    }
                                ) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Cancel")
                                }
                            }
                        ) {
                            DatePicker(
                                state = dateState,
                                showModeToggle = false
                            )
                        }
                    } else {
                        MonthYearPickerDialogChart(
                            initialTime = selectedTime,
                            onDismiss = { showDatePicker = false },
                            onConfirm = { millis ->
                                selectedTime = millis
                                showDatePicker = false
                            }
                        )
                    }
                }

                // Kartu grafik
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F1EE)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    val color = MaterialTheme.colorScheme.primary

                    val points = remember(viewModel.transactions, selectedTime, mode) {
                        when (mode) {
                            ReportModeSR.Daily -> hourlySeries(viewModel, selectedTime)
                            ReportModeSR.Monthly -> dailySeriesInMonth(viewModel, selectedTime)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Header + ringkasan
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (mode == ReportModeSR.Daily) {
                                    "Grafik Penjualan Harian"
                                } else {
                                    "Grafik Penjualan Bulanan"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = if (mode == ReportModeSR.Daily) {
                                    sdf.format(Date(selectedTime))
                                } else {
                                    monthFormat.format(Date(selectedTime))
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            val totals = remember(viewModel.transactions, selectedTime, mode) {
                                when (mode) {
                                    ReportModeSR.Daily -> hourlyRawTotals(viewModel, selectedTime)
                                    ReportModeSR.Monthly -> dailyRawTotals(viewModel, selectedTime)
                                }
                            }

                            val sum: Long = totals.sum()
                            val avg: Long = if (totals.isNotEmpty()) {
                                sum / totals.size
                            } else {
                                0L
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total: Rp$sum",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Grafik
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        ) {
                            val w = size.width
                            val h = size.height

                            val gridColor = Color.Black.copy(alpha = 0.04f)
                            val gridCount = 4
                            for (i in 1..gridCount) {
                                val y = h * i / (gridCount + 1)
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1f
                                )
                            }

                            if (points.isNotEmpty()) {
                                fun getPoint(index: Int): Offset {
                                    val iSafe = index.coerceIn(0, points.lastIndex)
                                    val x = if (points.size == 1) {
                                        w / 2f
                                    } else {
                                        (iSafe.toFloat() / (points.size - 1)) * (w - 32f) + 16f
                                    }
                                    val y = h - (points[iSafe] * (h - 24f) + 12f)
                                    return Offset(x, y)
                                }

                                val smoothPath = Path().apply {
                                    var previousPoint = getPoint(0)
                                    moveTo(previousPoint.x, previousPoint.y)

                                    for (i in 1..points.lastIndex) {
                                        val currentPoint = getPoint(i)
                                        val midPoint = Offset(
                                            (previousPoint.x + currentPoint.x) / 2f,
                                            (previousPoint.y + currentPoint.y) / 2f
                                        )
                                        quadraticBezierTo(
                                            previousPoint.x,
                                            previousPoint.y,
                                            midPoint.x,
                                            midPoint.y
                                        )
                                        previousPoint = currentPoint
                                    }

                                    lineTo(previousPoint.x, previousPoint.y)
                                }

                                val filledPath = Path().apply {
                                    val first = getPoint(0)
                                    val last = getPoint(points.lastIndex)
                                    moveTo(first.x, h)
                                    addPath(smoothPath)
                                    lineTo(last.x, h)
                                    close()
                                }

                                drawPath(
                                    path = filledPath,
                                    color = color.copy(alpha = 0.12f)
                                )

                                drawPath(
                                    path = smoothPath,
                                    color = color,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                                )

                                points.forEachIndexed { i, _ ->
                                    val center = getPoint(i)
                                    drawCircle(
                                        color = Color.White,
                                        radius = 4f,
                                        center = center
                                    )
                                    drawCircle(
                                        color = color,
                                        radius = 2.6f,
                                        center = center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val labels = when (mode) {
                                ReportModeSR.Daily -> listOf("00", "04", "08", "12", "16", "20", "23")
                                ReportModeSR.Monthly -> listOf("1", "6", "11", "16", "21", "26", "31")
                            }
                            labels.forEach { t ->
                                Text(
                                    text = t,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun hourlySeries(vm: TransaksiViewModel, selected: Long): List<Float> {
    val cal = Calendar.getInstance().apply {
        timeInMillis = selected
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    val hourMs = TimeUnit.HOURS.toMillis(1)
    val buckets = LongArray(24)
    vm.transactions.forEach { tx ->
        val idx = ((tx.timestamp - start) / hourMs).toInt()
        if (idx in 0..23) buckets[idx] += tx.total
    }
    val min = buckets.minOrNull() ?: 0L
    val max = buckets.maxOrNull() ?: 0L
    val range = (max - min).coerceAtLeast(1L).toFloat()
    return List(24) { ((buckets[it] - min).toFloat() / range).coerceIn(0f, 1f) }
}

private fun hourlyRawTotals(vm: TransaksiViewModel, selected: Long): List<Long> {
    val cal = Calendar.getInstance().apply {
        timeInMillis = selected
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    val hourMs = TimeUnit.HOURS.toMillis(1)
    val buckets = LongArray(24)
    vm.transactions.forEach { tx ->
        val idx = ((tx.timestamp - start) / hourMs).toInt()
        if (idx in 0..23) buckets[idx] += tx.total
    }
    return buckets.toList()
}

private fun dailySeriesInMonth(vm: TransaksiViewModel, selected: Long): List<Float> {
    val cal = Calendar.getInstance().apply {
        timeInMillis = selected
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    val end = cal.timeInMillis
    val dayMs = TimeUnit.DAYS.toMillis(1)
    val days = ((end - start) / dayMs).toInt()
    val buckets = LongArray(days)
    vm.transactions.forEach { tx ->
        val idx = ((tx.timestamp - start) / dayMs).toInt()
        if (idx in buckets.indices) buckets[idx] += tx.total
    }
    val min = buckets.minOrNull() ?: 0L
    val max = buckets.maxOrNull() ?: 0L
    val range = (max - min).coerceAtLeast(1L).toFloat()
    return List(days) { ((buckets[it] - min).toFloat() / range).coerceIn(0f, 1f) }
}

private fun dailyRawTotals(vm: TransaksiViewModel, selected: Long): List<Long> {
    val cal = Calendar.getInstance().apply {
        timeInMillis = selected
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    val end = cal.timeInMillis
    val dayMs = TimeUnit.DAYS.toMillis(1)
    val days = ((end - start) / dayMs).toInt()
    val buckets = LongArray(days)
    vm.transactions.forEach { tx ->
        val idx = ((tx.timestamp - start) / dayMs).toInt()
        if (idx in buckets.indices) buckets[idx] += tx.total
    }
    return buckets.toList()
}

@Composable
private fun MonthYearPickerDialogChart(
    initialTime: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val cal = remember(initialTime) {
        Calendar.getInstance().apply { timeInMillis = initialTime }
    }
    var selectedYear by remember { mutableStateOf(cal.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(cal.get(Calendar.MONTH)) }

    val years = remember(selectedYear) { (selectedYear - 5..selectedYear + 5).toList() }
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
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color(0xFFF1F1F1)
                            },
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
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }

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
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                Color(0xFFF1F1F1)
                            },
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
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.onPrimary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
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
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = {
                            val calendar = Calendar.getInstance().apply {
                                set(Calendar.YEAR, selectedYear)
                                set(Calendar.MONTH, selectedMonth)
                                set(Calendar.DAY_OF_MONTH, 1)
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onConfirm(calendar.timeInMillis)
                        }
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

