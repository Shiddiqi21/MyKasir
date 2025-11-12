package com.example.mykasir.feature_laporan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.example.mykasir.feature_transaksi.viewmodel.TransaksiViewModel
import java.text.SimpleDateFormat
import java.util.*
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grafik Penjualan", color = MaterialTheme.colorScheme.onPrimary) },
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
                // Segmented Harian/Bulanan
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

                // Date input & picker
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
                                dateState.selectedDateMillis?.let { selectedTime = it }
                                showDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
                    ) {
                        DatePicker(state = dateState, showModeToggle = false)
                    }
                }

                // Grafik kartu
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
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
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val path = Path()
                        points.forEachIndexed { i, v ->
                            val x = if (points.size == 1) w/2f else (i.toFloat() / (points.size - 1)) * (w - 16f) + 8f
                            val y = h - (v * (h - 16f) + 8f)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path = path, color = color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
                        points.forEachIndexed { i, v ->
                            val x = if (points.size == 1) w/2f else (i.toFloat() / (points.size - 1)) * (w - 16f) + 8f
                            val y = h - (v * (h - 16f) + 8f)
                            drawCircle(color = color, radius = 4f, center = Offset(x, y))
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val labels = when (mode) {
                            ReportModeSR.Daily -> listOf("00","04","08","12","16","20","23")
                            ReportModeSR.Monthly -> listOf("1","6","11","16","21","26","31")
                        }
                        labels.forEach { t -> Text(t, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
}

private fun hourlySeries(vm: TransaksiViewModel, selected: Long): List<Float> {
    val cal = Calendar.getInstance().apply {
        timeInMillis = selected
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    val hourMs = TimeUnit.HOURS.toMillis(1)
    val buckets = LongArray(24)
    vm.transactions.forEach { tx ->
        val idx = (((tx.timestamp - start) / hourMs).toInt())
        if (idx in 0..23) buckets[idx] += tx.total
    }
    val min = buckets.minOrNull() ?: 0L
    val max = buckets.maxOrNull() ?: 0L
    val range = (max - min).coerceAtLeast(1L).toFloat()
    return List(24) { ((buckets[it] - min).toFloat() / range).coerceIn(0f, 1f) }
}

private fun dailySeriesInMonth(vm: TransaksiViewModel, selected: Long): List<Float> {
    val cal = Calendar.getInstance().apply {
        timeInMillis = selected
        set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }
    val start = cal.timeInMillis
    cal.add(Calendar.MONTH, 1)
    val end = cal.timeInMillis
    val dayMs = TimeUnit.DAYS.toMillis(1)
    val days = ((end - start) / dayMs).toInt()
    val buckets = LongArray(days)
    vm.transactions.forEach { tx ->
        val idx = (((tx.timestamp - start) / dayMs).toInt())
        if (idx in buckets.indices) buckets[idx] += tx.total
    }
    val min = buckets.minOrNull() ?: 0L
    val max = buckets.maxOrNull() ?: 0L
    val range = (max - min).coerceAtLeast(1L).toFloat()
    return List(days) { ((buckets[it] - min).toFloat() / range).coerceIn(0f, 1f) }
}
