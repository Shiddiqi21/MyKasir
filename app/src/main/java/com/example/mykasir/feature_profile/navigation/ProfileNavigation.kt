package com.example.mykasir.feature_profile.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mykasir.feature_profile.screen.EditProfileScreen
import com.example.mykasir.feature_profile.screen.ProfileScreen
import com.example.mykasir.feature_profile.viewmodel.ProfileViewModel

/**
 * Navigation graph untuk fitur Profile
 */
@Composable
fun ProfileNav(
    onLogout: () -> Unit,
    navController: NavHostController = rememberNavController(),
    viewModel: ProfileViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = "profile_main"
    ) {
        composable("profile_main") {
            ProfileScreen(
                onEditProfile = {
                    navController.navigate("profile_edit")
                },
                onLogout = onLogout,
                viewModel = viewModel
            )
        }

        composable("profile_edit") {
            EditProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
    }
}
