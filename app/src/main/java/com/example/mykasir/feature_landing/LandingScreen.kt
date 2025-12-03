package com.example.mykasir.feature_landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mykasir.R
import kotlinx.coroutines.delay

@Composable
fun LandingScreen(onFinished: () -> Unit) {
    // Setelah jeda singkat, otomatis lanjut ke layar berikutnya (login)
    LaunchedEffect(Unit) {
        delay(1500)
        onFinished()
    }

    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        label = "landingContentAlpha"
    )
    val contentOffset by animateDpAsState(
        targetValue = if (visible) 0.dp else 12.dp,
        label = "landingContentOffset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.graphicsLayer {
                alpha = contentAlpha
                translationY = contentOffset.toPx()
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.mykasir_logo),
                contentDescription = "Logo MyKasir",
                modifier = Modifier.size(260.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "MyKasir",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "UMKM Digital",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}
