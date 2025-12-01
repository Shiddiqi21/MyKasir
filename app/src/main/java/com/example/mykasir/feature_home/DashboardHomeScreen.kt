package com.example.mykasir.feature_home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mykasir.R
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel
import com.example.mykasir.feature_transaksi.viewmodel.TransaksiViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun DashboardHomeScreen(
    transaksiViewModel: TransaksiViewModel,
    productViewModel: ProductViewModel,
    onOpenTransaksi: () -> Unit,
    onOpenProduk: () -> Unit,
    onOpenLaporan: () -> Unit
) {
    val calToday = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val startToday = calToday.timeInMillis
    val endToday = startToday + 24L * 60 * 60 * 1000

    val todayTx = remember(transaksiViewModel.transactions) {
        transaksiViewModel.transactions.filter { it.timestamp in startToday until endToday }
    }
    val todayTotal = remember(todayTx) { todayTx.sumOf { it.total }.toLong() }
    val todayCount = remember(todayTx) { todayTx.size }

    val rupiahFormatter = remember {
        NumberFormat.getNumberInstance(Locale("in", "ID"))
    }
    val formattedTodayTotal = remember(todayTotal) {
        "Rp " + rupiahFormatter.format(todayTotal)
    }

    // Data grafik 7 hari terakhir
    val weeklyPoints = remember(transaksiViewModel.transactions) {
        computeLast7Days(transaksiViewModel)
    }

    val lowStockProducts = remember(productViewModel.products) {
        productViewModel.products
            .filter { it.stock <= it.minStock }
            .sortedBy { it.stock }
            .take(3)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        // Header biru
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        text = "Beranda",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Ringkasan cepat usaha tokomu",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                }
                Spacer(modifier = Modifier.width(28.dp))
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Ringkasan Hari Ini
                Text(
                    text = "Ringkasan Hari Ini",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Pendapatan",
                        value = formattedTodayTotal,
                        accentColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Transaksi",
                        value = todayCount.toString(),
                        accentColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Grafik 7 hari terakhir
                Text(
                    text = "Penjualan 7 Hari Terakhir",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                WeeklySalesChart(points = weeklyPoints)

                // Aksi Cepat (dalam grid kecil, hemat ruang)
                Text(
                    text = "Aksi Cepat",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionChip(
                        icon = Icons.Filled.PointOfSale,
                        title = "Transaksi",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenTransaksi
                    )
                    QuickActionChip(
                        icon = Icons.Filled.Settings,
                        title = "Stok",
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenProduk
                    )
                    QuickActionChip(
                        icon = Icons.Filled.Description,
                        title = "Laporan",
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = onOpenLaporan
                    )
                }

                // Stok menipis
                Text(
                    text = "Perlu Perhatian",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (lowStockProducts.isEmpty()) {
                    Text(
                        text = "Belum ada produk dengan stok menipis.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        lowStockProducts.forEach { p ->
                            val isOutOfStock = p.stock <= 0
                            val containerColor = if (isOutOfStock) Color(0xFFFFE5E5) else Color(0xFFFFF4E5)
                            val statusText = if (isOutOfStock) "Habis" else "Hampir habis"

                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = containerColor
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = p.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Stok tersisa: ${p.stock}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                    }
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error
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

private data class DayPoint(
    val label: String,
    val total: Long
)

@Composable
private fun WeeklySalesChart(points: List<DayPoint>) {
    val maxTotal = (points.maxOfOrNull { it.total } ?: 0L).coerceAtLeast(1L)
    val graphColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F7FA)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            ) {
                val w = size.width
                val h = size.height

                // Garis grid horizontal tipis (konsisten dengan LaporanScreen/ChartPage)
                val gridColor = Color.Black.copy(alpha = 0.04f)
                val gridCount = 4
                val stepY = h / (gridCount + 1)
                for (i in 1..gridCount) {
                    val y = stepY * i
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                if (points.isNotEmpty()) {
                    val values = points.map { it.total.toFloat() / maxTotal.toFloat() }

                    fun getPoint(index: Int): Offset {
                        val iSafe = index.coerceIn(0, values.lastIndex)
                        val x = if (values.size == 1) {
                            w / 2f
                        } else {
                            (iSafe.toFloat() / (values.size - 1)) * (w - 32f) + 16f
                        }
                        val y = h - (values[iSafe] * (h - 24f) + 12f)
                        return Offset(x, y)
                    }

                    // Path halus (quadratic) seperti di ChartPage
                    val smoothPath = Path().apply {
                        var previous = getPoint(0)
                        moveTo(previous.x, previous.y)
                        for (i in 1..values.lastIndex) {
                            val current = getPoint(i)
                            val mid = Offset(
                                (previous.x + current.x) / 2f,
                                (previous.y + current.y) / 2f
                            )
                            quadraticBezierTo(previous.x, previous.y, mid.x, mid.y)
                            previous = current
                        }
                        lineTo(previous.x, previous.y)
                    }

                    // Area halus di bawah garis
                    val filledPath = Path().apply {
                        val first = getPoint(0)
                        val last = getPoint(values.lastIndex)
                        moveTo(first.x, h)
                        addPath(smoothPath)
                        lineTo(last.x, h)
                        close()
                    }

                    drawPath(
                        path = filledPath,
                        color = graphColor.copy(alpha = 0.12f)
                    )

                    drawPath(
                        path = smoothPath,
                        color = graphColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                    )

                    // Titik dua lapis (putih + warna utama)
                    values.forEachIndexed { index, _ ->
                        val center = getPoint(index)
                        drawCircle(
                            color = Color.White,
                            radius = 4f,
                            center = center
                        )
                        drawCircle(
                            color = graphColor,
                            radius = 2.6f,
                            center = center
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEach {
                    Text(
                        text = it.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(78.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF5F5F7)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(72.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F7)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(color.copy(alpha = 0.18f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun computeLast7Days(transaksiViewModel: TransaksiViewModel): List<DayPoint> {
    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val sdf = SimpleDateFormat("EE", Locale("in", "ID"))

    val days = mutableListOf<DayPoint>()

    // mundur 6 hari lalu maju sampai hari ini
    cal.add(Calendar.DAY_OF_MONTH, -6)
    repeat(7) {
        val start = cal.timeInMillis
        val end = start + 24L * 60 * 60 * 1000
        val total = transaksiViewModel.transactions
            .filter { it.timestamp in start until end }
            .sumOf { it.total }
            .toLong()
        val label = sdf.format(cal.time).take(3)
        days.add(DayPoint(label = label, total = total))
        cal.add(Calendar.DAY_OF_MONTH, 1)
    }
    return days
}
