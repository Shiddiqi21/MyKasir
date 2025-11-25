package com.example.mykasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.mykasir.navigationapp.RootNavGraph // <-- Ganti dari MainAppHost
import com.example.mykasir.ui.theme.MyKasirTheme
import com.example.mykasir.core_ui.ProvideNotifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyKasirApp()
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