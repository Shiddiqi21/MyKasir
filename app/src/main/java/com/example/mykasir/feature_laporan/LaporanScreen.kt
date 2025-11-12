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
import com.example.mykasir.feature_transaksi.viewmodel.TransaksiViewModel
import java.util.concurrent.TimeUnit
import java.util.Calendar
 
@Composable
fun LaporanScreen(
    viewModel: TransaksiViewModel,
    onOpenLaporan: () -> Unit,
    onOpenGrafik: () -> Unit
) {
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        // Header biru konsisten
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
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
                Text(
                    text = "Grafik dan Laporan",
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                IconButton(onClick = { /* profil */ }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_person),
                        contentDescription = "Profil",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
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
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Penjualan Hari Ini",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                // Kartu grafik
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F1EE)),
                    shape = RoundedCornerShape(24.dp)
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
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        val points = hourlyFractions
                        val path = Path()
                        points.forEachIndexed { i, v ->
                            val x = (i.toFloat() / (points.size - 1)) * (w - 16f) + 8f
                            val y = h - (v * (h - 16f) + 8f)
                            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        }
                        drawPath(path = path, color = graphColor, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f))
                        // titik
                        points.forEachIndexed { i, v ->
                            val x = (i.toFloat() / (points.size - 1)) * (w - 16f) + 8f
                            val y = h - (v * (h - 16f) + 8f)
                            drawCircle(color = graphColor, radius = 4f, center = Offset(x, y))
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
                    subtitle = "Kelola dan ekspor laporan Penjualan",
                    onClick = { onOpenLaporan() }
                )

                // Card aksi 2: Grafik Penjualan
                ActionCard(
                    icon = Icons.Filled.ShowChart,
                    title = "Grafik Penjualan",
                    subtitle = "Visualisasi Data dalam Grafik",
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFEFEF)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Badge ikon kiri
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
            Icon(imageVector = Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
