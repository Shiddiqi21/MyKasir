package com.example.mykasir.navigationapp

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.mykasir.core_data.local.TokenManager
import com.example.mykasir.core_data.remote.TokenExpiredEvent
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
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    
    // Check if user is already logged in
    val isLoggedIn = remember { tokenManager.isLoggedIn() }
    
    // State untuk trigger token expired
    var tokenExpired by remember { mutableStateOf(false) }
    
    // Setup listener untuk token expired
    DisposableEffect(Unit) {
        TokenExpiredEvent.setListener {
            tokenExpired = true
        }
        
        onDispose {
            TokenExpiredEvent.removeListener()
        }
    }
    
    // Handle token expired - auto logout
    LaunchedEffect(tokenExpired) {
        if (tokenExpired) {
            tokenManager.clearToken()
            Toast.makeText(context, "Sesi Anda telah berakhir, silakan login ulang", Toast.LENGTH_LONG).show()
            navController.navigate(Graph.AUTH) {
                popUpTo(Graph.ROOT) { inclusive = true }
            }
            tokenExpired = false
        }
    }

    NavHost(
        navController = navController,
        route = Graph.ROOT,
        startDestination = "landing"  // Selalu mulai dari landing page
    ) {
        // Landing Screen
        composable(route = "landing") {
            LandingScreen(
                onFinished = {
                    // Jika sudah login, langsung ke main, jika belum ke auth
                    if (isLoggedIn) {
                        navController.navigate(Graph.MAIN) {
                            popUpTo("landing") { inclusive = true }
                        }
                    } else {
                        navController.navigate(Graph.AUTH) {
                            popUpTo("landing") { inclusive = true }
                        }
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