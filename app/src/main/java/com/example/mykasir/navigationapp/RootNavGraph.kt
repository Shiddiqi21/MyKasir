package com.example.mykasir.navigationapp // <-- Pastikan package ini benar

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.mykasir.feature_auth.screen.LoginScreen
import com.example.mykasir.feature_auth.viewmodel.LoginViewModel

// Definisikan rute-rute utama Anda
object Graph {
    const val ROOT = "root_graph"
    const val AUTH = "auth_graph"
    const val MAIN = "main_graph"
}

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        route = Graph.ROOT, // Rute untuk seluruh NavHost
        startDestination = Graph.AUTH // Mulai dari alur Auth
    ) {
        // Alur Autentikasi (Login)
        navigation(
            route = Graph.AUTH,
            startDestination = "login" // Layar pertama di alur auth
        ) {
            composable(route = "login") {
                // Buat instance ViewModel di sini
                val loginViewModel: LoginViewModel = viewModel()

                LoginScreen(
                    viewModel = loginViewModel, // Kirim ViewModel ke Layar
                    onLoginSuccess = {
                        // Jika login sukses, navigasi ke Alur Utama
                        navController.navigate(Graph.MAIN) {
                            // Hapus alur auth dari back stack
                            popUpTo(Graph.AUTH) {
                                inclusive = true
                            }
                        }
                    }
                )
            }
            // (Anda bisa tambahkan composable("signup") di sini nanti)
        }

        // Alur Utama Aplikasi (Setelah Login)
        navigation(
            route = Graph.MAIN,
            startDestination = "main_host" // Layar pertama di alur main
        ) {
            composable(route = "main_host") {
                // MainAppHost adalah Composable Anda dari file
                // MainAppNavigation.kt (yang ada di package ini juga)
                MainAppHost()
            }
        }
    }
}