package com.example.mykasir.feature_home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mykasir.R
import com.example.mykasir.core_data.local.TokenManager
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
    // Get user role
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    val isOwner = remember { tokenManager.isOwner() }
    val userName = remember { tokenManager.getUserName() ?: "Pengguna" }
    val userRole = if (isOwner) "Pemilik Toko" else "Kasir"
    
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

    // Gunakan derivedStateOf agar otomatis recalculate saat transactions berubah
    val todayTx by remember {
        derivedStateOf {
            transaksiViewModel.transactions.filter { it.timestamp in startToday until endToday }
        }
    }
    val todayTotal by remember {
        derivedStateOf { todayTx.sumOf { it.total }.toLong() }
    }
    val todayCount by remember {
        derivedStateOf { todayTx.size }
    }

    val rupiahFormatter = remember {
        NumberFormat.getNumberInstance(Locale("in", "ID"))
    }
    val formattedTodayTotal by remember {
        derivedStateOf { "Rp " + rupiahFormatter.format(todayTotal) }
    }

    val weeklyPoints by remember {
        derivedStateOf { computeLast7Days(transaksiViewModel) }
    }

    val lowStockProducts by remember {
        derivedStateOf {
            productViewModel.products
                .filter { it.stock <= it.minStock }
                .sortedBy { it.stock }
                .take(3)
        }
    }

    var visible by remember { mutableStateOf(false) }

    // Load data saat dashboard pertama kali dibuka
    LaunchedEffect(Unit) {
        // Muat data transaksi dan produk
        transaksiViewModel.loadTransactions()
        productViewModel.loadProducts()
        visible = true
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        label = "homeContentAlpha"
    )
    val contentOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 12.dp,
        label = "homeContentOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.18f),
                                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.05f)
                                        ),
                                        tileMode = TileMode.Clamp
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                painter = painterResource(id = R.drawable.mykasir_logo),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Halo, $userRole!",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "Beranda MyKasir",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f)
                    ) {
                        Text(
                            text = "Hari ini",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = contentAlpha
                        translationY = contentOffset.toPx()
                    },
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Ringkasan Hari Ini",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Angka singkat untuk memantau performa tokomu hari ini.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryCard(
                            title = "Pendapatan",
                            value = formattedTodayTotal,
                            accentColor = MaterialTheme.colorScheme.primary,
                            icon = Icons.Filled.Money,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = "Transaksi",
                            value = todayCount.toString(),
                            accentColor = MaterialTheme.colorScheme.secondary,
                            icon = Icons.Filled.ShoppingCart,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // kartu kecil tambahan untuk stok tipis
                    SummaryCard(
                        title = "Produk Stok Tipis",
                        value = "${lowStockProducts.size}",
                        accentColor = MaterialTheme.colorScheme.error,
                        icon = Icons.Filled.Warning,
                        helper = if (lowStockProducts.isEmpty()) "Semua stok aman" else "Periksa daftar di bawah",
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Diagram hanya untuk Owner
                    if (isOwner) {
                        Text(
                            text = "Penjualan 7 Hari Terakhir",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        WeeklySalesChart(points = weeklyPoints)
                    }

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
                        // Laporan hanya untuk Owner
                        if (isOwner) {
                            QuickActionChip(
                                icon = Icons.Filled.Description,
                                title = "Laporan",
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f),
                                onClick = onOpenLaporan
                            )
                        }
                    }

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
                                    shape = RoundedCornerShape(16.dp),
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
                                            color = MaterialTheme.colorScheme.error,
                                            textAlign = TextAlign.End
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


private data class DayPoint(
    val label: String,
    val total: Long
)

@Composable
private fun WeeklySalesChart(points: List<DayPoint>) {
    val maxTotal = (points.maxOfOrNull { it.total } ?: 0L).coerceAtLeast(1L)
    val graphColor = MaterialTheme.colorScheme.primary
    val total7Days = points.sumOf { it.total }
    val formatter = remember {
        NumberFormat.getNumberInstance(Locale("in", "ID"))
    }
    val formattedTotal7 = remember(total7Days) {
        "Rp " + formatter.format(total7Days)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F4FA)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total 7 hari",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = formattedTotal7,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(graphColor, RoundedCornerShape(50))
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
                    .height(110.dp)
            ) {
                val w = size.width
                val h = size.height

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
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    helper: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(78.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF5F5F7)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                accentColor.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = accentColor
            )
            if (helper != null) {
                Text(
                    text = helper,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
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
                androidx.compose.material3.Icon(
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
                textAlign = TextAlign.Center,
                maxLines = 1
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
