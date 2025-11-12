package com.example.mykasir.core_ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Types of notifications
enum class NotificationType { Success, Error, Info }

data class AppNotification(
    val message: String,
    val type: NotificationType = NotificationType.Success,
    val durationMs: Long = 2000
)

class NotifierState {
    var current by mutableStateOf<AppNotification?>(null)
        private set

    fun show(message: String, type: NotificationType = NotificationType.Success, durationMs: Long = 2000) {
        current = AppNotification(message, type, durationMs)
    }

    fun hide() { current = null }
}

@Composable
fun rememberNotifierState(): NotifierState = remember { NotifierState() }

val LocalNotifier = staticCompositionLocalOf<NotifierState?> { null }

@Composable
fun ProvideNotifier(content: @Composable () -> Unit) {
    val state = rememberNotifierState()
    Box {
        CompositionLocalProvider(LocalNotifier provides state) {
            content()
        }
        NotificationHost(state)
    }
}

@Composable
fun NotificationHost(state: NotifierState) {
    val notif = state.current
    AnimatedVisibility(
        visible = notif != null,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        if (notif != null) {
            LaunchedEffect(notif) {
                kotlinx.coroutines.delay(notif.durationMs)
                state.hide()
            }
            val (bg, icon, onColor) = when (notif.type) {
                NotificationType.Success -> Triple(Color(0xFF22C55E), Icons.Filled.CheckCircle, Color.White)
                NotificationType.Error -> Triple(Color(0xFFEF4444), Icons.Filled.Error, Color.White)
                NotificationType.Info -> Triple(Color(0xFF3B82F6), Icons.Filled.Info, Color.White)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .padding(top = 56.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    color = bg,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, contentDescription = null, tint = onColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = notif.message,
                            color = onColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
