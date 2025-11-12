package com.example.mykasir.feature_transaksi.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.mykasir.feature_transaksi.screen.TransaksiFormScreen
import com.example.mykasir.feature_transaksi.screen.TransaksiListScreen
import com.example.mykasir.feature_transaksi.screen.TransactionHistoryScreen
import com.example.mykasir.feature_transaksi.viewmodel.TransaksiViewModel
import com.example.mykasir.feature_manajemen_produk.viewmodel.ProductViewModel

sealed class TxRoute(val route: String) {
    data object List : TxRoute("tx_list")
    data object Form : TxRoute("tx_form")
    data object History : TxRoute("tx_history/{customerId}") {
        const val ARG_CUSTOMER_ID = "customerId"
        fun build(customerId: Long) = "tx_history/$customerId"
    }
}

@Composable
fun TransaksiNav(hostNavController: NavHostController, productViewModel: ProductViewModel) {
    val txViewModel: TransaksiViewModel = viewModel()

    NavHost(
        navController = hostNavController,
        startDestination = TxRoute.List.route
    ) {
        composable(TxRoute.List.route) {
            TransaksiListScreen(
                viewModel = txViewModel,
                onTambahTransaksi = {
                    hostNavController.navigate(TxRoute.Form.route)
                },
                onDetail = {
                    hostNavController.navigate(TxRoute.History.build(it.id))
                }
            )
        }
        composable(TxRoute.Form.route) {
            TransaksiFormScreen(
                viewModel = txViewModel,
                productViewModel = productViewModel,
                onBack = { hostNavController.popBackStack() },
                onSaved = { customerId ->
                    hostNavController.navigate(TxRoute.History.build(customerId)) {
                        popUpTo(TxRoute.List.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = TxRoute.History.route,
            arguments = listOf(navArgument(TxRoute.History.ARG_CUSTOMER_ID) { type = NavType.LongType })
        ) { backStack ->
            val customerId = backStack.arguments?.getLong(TxRoute.History.ARG_CUSTOMER_ID) ?: -1L
            TransactionHistoryScreen(
                viewModel = txViewModel,
                customerId = customerId,
                onBack = { hostNavController.popBackStack() }
            )
        }
    }
}
