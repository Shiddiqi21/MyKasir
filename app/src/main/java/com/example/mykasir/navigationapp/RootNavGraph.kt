package com.example.mykasir.navigationapp

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.mykasir.feature_auth.screen.LoginScreen
import com.example.mykasir.feature_auth.screen.RegisterScreen
import com.example.mykasir.feature_auth.viewmodel.LoginViewModel
import com.example.mykasir.feature_landing.LandingScreen
import com.example.mykasir.navigationapp.MainAppHost

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
        startDestination = "landing" // Mulai dari Landing terlebih dahulu
    ) {
        // Landing Screen
        composable(route = "landing") {
            LandingScreen(
                onFinished = {
                    navController.navigate(Graph.AUTH) {
                        popUpTo("landing") { inclusive = true }
                    }
                }
            )
        }
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
                    },
                    onSignUpClick = {
                        navController.navigate("register")
                    }
                )
            }
            composable(route = "register") {
                RegisterScreen(
                    onBack = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.popBackStack()
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
                MainAppHost(
                    onLogout = {
                        // Navigate back to login screen when user logs out
                        navController.navigate(Graph.AUTH) {
                            popUpTo(Graph.MAIN) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}