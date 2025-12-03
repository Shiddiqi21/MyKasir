package com.example.mykasir.feature_laporan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mykasir.R
import com.example.mykasir.feature_transaksi.viewmodel.TransaksiViewModel
import java.util.concurrent.TimeUnit
import java.util.Calendar
import java.text.NumberFormat
import java.util.Locale

@Composable
fun LaporanScreen(
    viewModel: TransaksiViewModel,
    onOpenLaporan: () -> Unit,
    onOpenGrafik: () -> Unit
) {
    // Hitung total pendapatan hari ini
    val totalHariIni = remember(viewModel.transactions) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = cal.timeInMillis
        val end = start + TimeUnit.DAYS.toMillis(1)
        viewModel.transactions
            .filter { it.timestamp in start until end }
            .sumOf { it.total }
    }

    // Format angka ke dalam format Rupiah sederhana
    val formattedTotalHariIni = remember(totalHariIni) {
        val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
        "Rp " + formatter.format(totalHariIni)
    }

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        label = "laporanContentAlpha"
    )
    val contentOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 12.dp,
        label = "laporanContentOffset"
    )

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
        // Header biru (konsisten dengan beranda, tanpa ikon profil) + ringkasan singkat
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
                        text = "Laporan",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Pantau performa penjualan tokomu",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                Spacer(modifier = Modifier.width(28.dp)) // ruang kosong pengganti ikon profil
            }

            // Ringkasan total pendapatan hari ini dalam kontainer
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.1f)
                ),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total pendapatan hari ini",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        )
                    }
                    Text(
                        text = formattedTotalHariIni,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        // Konten putih melengkung
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = contentAlpha
                    translationY = contentOffset.toPx()
                },
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Header kecil untuk section penjualan hari ini
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Penjualan Hari Ini",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                       
                    }
                }

                // Kartu grafik
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F1EE)),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    val graphColor = MaterialTheme.colorScheme.primary
                    // Hitung total penjualan HARI INI per-jam (0..23)
                    val hourlyFractions = remember(viewModel.transactions) {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        val start = cal.timeInMillis
                        val hourMs = TimeUnit.HOURS.toMillis(1)
                        val buckets = LongArray(24) { 0L }
                        viewModel.transactions.forEach { tx ->
                            val idx = (((tx.timestamp - start) / hourMs).toInt())
                            if (idx in 0..23) buckets[idx] = buckets[idx] + tx.total
                        }
                        val min = buckets.minOrNull() ?: 0L
                        val max = buckets.maxOrNull() ?: 0L
                        val range = (max - min).coerceAtLeast(1L).toFloat()
                        List(24) { i -> ((buckets[i] - min).toFloat() / range).coerceIn(0f, 1f) }
                    }
                    // Header kecil di dalam kartu grafik
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Grafik Penjualan Hari Ini",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "Per jam, 00 - 23",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.White.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = graphColor,
                                            shape = RoundedCornerShape(50)
                                        )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Penjualan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val points = hourlyFractions

                        // Garis grid horizontal tipis untuk tampilan modern
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
                            // Bangun path halus (cubic Bezier) agar garis tidak kaku
                            fun getPoint(index: Int): Offset {
                                val iSafe = index.coerceIn(0, points.lastIndex)
                                val x = (iSafe.toFloat() / (points.size - 1)) * (w - 32f) + 16f
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

                                // Tarik sampai titik terakhir
                                lineTo(previousPoint.x, previousPoint.y)
                            }

                            // Area halus di bawah garis
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
                                color = graphColor.copy(alpha = 0.12f)
                            )

                            // Garis utama yang lebih fleksibel (smooth)
                            drawPath(
                                path = smoothPath,
                                color = graphColor,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                            )

                            // Titik di setiap jam (sedikit lebih kecil agar lembut)
                            points.forEachIndexed { i, _ ->
                                val center = getPoint(i)
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
                    // Label jam (contoh titik: 0, 4, 8, 12, 16, 20, 23)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf("00","04","08","12","16","20","23").forEach { h ->
                            Text(h, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Card aksi 1: Laporan Penjualan
                ActionCard(
                    icon = Icons.Filled.Description,
                    title = "Laporan Penjualan",
                    subtitle = "Ringkasan dan detail transaksi",
                    onClick = { onOpenLaporan() }
                )

                // Card aksi 2: Grafik Penjualan
                ActionCard(
                    icon = Icons.Filled.ShowChart,
                    title = "Grafik Penjualan",
                    subtitle = "Tren penjualan dalam grafik",
                    onClick = { onOpenGrafik() }
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F7)),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge ikon kiri
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
