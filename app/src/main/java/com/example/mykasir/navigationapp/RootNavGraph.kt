package com.example.mykasir.navigationapp

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.mykasir.feature_auth.screen.LoginScreen
import com.example.mykasir.feature_auth.viewmodel.LoginViewModel

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
        route = Graph.ROOT,
        startDestination = Graph.AUTH // Mulai dari alur Auth
    ) {
        // Alur Autentikasi (Login)
        navigation(
            route = Graph.AUTH,
            startDestination = "login"
        ) {
            composable(route = "login") {
                val loginViewModel: LoginViewModel = viewModel()
                LoginScreen(
                    viewModel = loginViewModel,
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
        }

        // Alur Utama Aplikasi (Setelah Login)
        navigation(
            route = Graph.MAIN,
            startDestination = "main_host"
        ) {
            composable(route = "main_host") {
                // MainAppHost adalah Composable Anda dari file
                // MainAppNavigation.kt yang berisi BottomNavBar
                MainAppHost()
            }
        }
    }
}