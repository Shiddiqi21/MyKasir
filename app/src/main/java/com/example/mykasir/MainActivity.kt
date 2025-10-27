package com.example.mykasir

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.mykasir.navigationapp.MainAppHost // <-- IMPORT BARU
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
        // Surface, ViewModel, dan NavController sudah pindah ke dalam MainAppHost
        // Jadi di sini kita cukup memanggil MainAppHost
        MainAppHost()
    }
}