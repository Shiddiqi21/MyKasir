package com.example.mykasir

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import com.example.mykasir.navigationapp.RootNavGraph
import com.example.mykasir.ui.theme.MyKasirTheme
import com.example.mykasir.core_ui.ProvideNotifier
import com.example.mykasir.core_ui.NotificationHelper

class MainActivity : ComponentActivity() {
    
    // Launcher untuk request permission notifikasi
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Permission granted atau denied, app tetap lanjut
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup notification channels
        NotificationHelper.createNotificationChannels(this)
        
        // Request notification permission untuk Android 13+
        requestNotificationPermission()
        
        enableEdgeToEdge()
        setContent {
            MyKasirApp()
        }
    }
    
    private fun requestNotificationPermission() {
        // Hanya untuk Android 13 (API 33) ke atas
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            when {
                ContextCompat.checkSelfPermission(this, permission) == 
                    PackageManager.PERMISSION_GRANTED -> {
                    // Permission sudah diberikan
                }
                else -> {
                    // Request permission
                    notificationPermissionLauncher.launch(permission)
                }
            }
        }
    }
}

@Composable
fun MyKasirApp() {
    MyKasirTheme {
        ProvideNotifier {
            RootNavGraph()
        }
    }
}