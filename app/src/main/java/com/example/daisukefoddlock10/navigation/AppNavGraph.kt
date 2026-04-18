package com.example.daisukefoddlock10.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.daisukefoddlock10.ui.screens.SharedOrderViewModel
import com.example.daisukefoddlock10.ui.screens.checkout.CheckoutScreen
import com.example.daisukefoddlock10.ui.screens.history.OrderHistoryScreen
import com.example.daisukefoddlock10.ui.screens.order.OrderScreen
import com.example.daisukefoddlock10.ui.screens.payment.PaymentScreen
import com.example.daisukefoddlock10.ui.screens.payment.PaymentSuccessScreen

sealed class Screen(val route: String) {
    object Order : Screen("order")
    object Checkout : Screen("checkout")
    object Payment : Screen("payment/{totalPrice}") {
        fun createRoute(totalPrice: Int) = "payment/$totalPrice"
    }
    object PaymentSuccess : Screen("payment_success/{orderId}/{paymentMethod}") {
        fun createRoute(orderId: String, method: String) = "payment_success/$orderId/$method"
    }
    object History : Screen("history")
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    val viewModel: SharedOrderViewModel = viewModel(LocalActivity.current as ComponentActivity)

    NavHost(navController, startDestination = Screen.Order.route) {
        composable(Screen.Order.route) {
            OrderScreen(
                viewModel = viewModel,
                onCheckout = { navController.navigate(Screen.Checkout.route) },
                onHistoryClick = { navController.navigate(Screen.History.route) }
            )
        }
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onProceedPayment = { total ->
                    navController.navigate(Screen.Payment.createRoute(total))
                }
            )
        }
        composable(
            route = Screen.Payment.route,
            arguments = listOf(navArgument("totalPrice") { type = NavType.IntType })
        ) { backStackEntry ->
            val totalPrice = backStackEntry.arguments?.getInt("totalPrice") ?: 0
            PaymentScreen(
                totalPrice = totalPrice,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onPaymentSuccess = { orderId, method ->
                    navController.navigate(Screen.PaymentSuccess.createRoute(orderId, method)) {
                        popUpTo(Screen.Order.route) { inclusive = false }
                    }
                }
            )
        }
        composable(
            route = Screen.PaymentSuccess.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType },
                navArgument("paymentMethod") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val method = backStackEntry.arguments?.getString("paymentMethod") ?: ""
            PaymentSuccessScreen(
                orderId = orderId,
                paymentMethod = method,
                onBackToHome = {
                    navController.navigate(Screen.Order.route) {
                        popUpTo(Screen.Order.route) { inclusive = true }
                    }
                },
                onViewHistory = { navController.navigate(Screen.History.route) }
            )
        }
        composable(Screen.History.route) {
            OrderHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
