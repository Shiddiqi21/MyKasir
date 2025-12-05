package com.example.mykasir.feature_profil.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import com.example.mykasir.feature_profil.screen.ProfileScreen

fun NavGraphBuilder.profileNav(
    navController: NavHostController,
    onLogoutClick: () -> Unit
) {
    composable("profile") {
        ProfileScreen(
            onLogoutClick = onLogoutClick,
            onEditClick = {
                // TODO: Navigate to edit profile screen
            }
        )
    }
}
