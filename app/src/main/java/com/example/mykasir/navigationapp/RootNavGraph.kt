package com.example.mykasir.navigationapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mykasir.core_data.local.TokenManager
import com.example.mykasir.feature_auth.screen.LoginScreen
import com.example.mykasir.feature_auth.screen.RegisterScreen
import com.example.mykasir.feature_auth.viewmodel.LoginViewModel
import com.example.mykasir.feature_landing.LandingScreen

@Composable
fun RootNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val tokenManager = remember { TokenManager(context) }
    
    // Tentukan start destination - jika sudah login langsung ke main
    val startDest = if (tokenManager.isLoggedIn()) "main" else "landing"

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        // Landing Screen
        composable(route = "landing") {
            LandingScreen(
                onFinished = {
                    navController.navigate("login") {
                        popUpTo("landing") { inclusive = true }
                    }
                }
            )
        }
        
        // Login Screen
        composable(route = "login") {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onSignUpClick = {
                    navController.navigate("register")
                }
            )
        }
        
        // Register Screen
        composable(route = "register") {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // Main App Screen
        composable(route = "main") {
            MainAppHost(
                onLogout = {
                    tokenManager.clearToken()
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}