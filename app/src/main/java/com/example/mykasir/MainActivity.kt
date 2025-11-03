package com.example.mykasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
// --- UBAH IMPORT INI ---
import com.example.mykasir.navigationapp.RootNavGraph // <-- Ganti dari MainAppHost
import com.example.mykasir.ui.theme.MyKasirTheme

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
        // Panggil RootNavGraph sebagai titik masuk aplikasi
        // RootNavGraph yang akan memutuskan untuk menampilkan
        // LoginScreen atau MainAppHost
        RootNavGraph()
    }
}