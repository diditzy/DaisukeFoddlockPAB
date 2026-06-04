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

import com.example.daisukefoddlock10.data.model.UserRole
import com.example.daisukefoddlock10.ui.screens.auth.AuthViewModel
import com.example.daisukefoddlock10.ui.screens.auth.LoginScreen
import com.example.daisukefoddlock10.ui.screens.merchant.MerchantDashboardScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object MerchantDashboard : Screen("merchant_dashboard")
    object Order : Screen("order")
    object Checkout : Screen("checkout")
    object Payment : Screen("payment/{totalPrice}") {
        fun createRoute(totalPrice: Int) = "payment/$totalPrice"
    }
    object PaymentSuccess : Screen("payment_success/{orderId}/{paymentMethod}/{totalPrice}") {
        fun createRoute(orderId: String, method: String, totalPrice: Int) = "payment_success/$orderId/$method/$totalPrice"
    }
    object History : Screen("history")
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val viewModel: SharedOrderViewModel = viewModel(LocalActivity.current as ComponentActivity)

    NavHost(navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    val session = authViewModel.uiState.value.session
                    if (session?.role == UserRole.MERCHANT) {
                        navController.navigate(Screen.MerchantDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Order.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Screen.MerchantDashboard.route) {
            MerchantDashboardScreen(
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Order.route) {
            OrderScreen(
                viewModel = viewModel,
                onCheckout = { navController.navigate(Screen.Checkout.route) },
                onHistoryClick = { navController.navigate(Screen.History.route) },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
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
                    navController.navigate(Screen.PaymentSuccess.createRoute(orderId, method, totalPrice)) {
                        popUpTo(Screen.Order.route) { inclusive = false }
                    }
                }
            )
        }
        composable(
            route = Screen.PaymentSuccess.route,
            arguments = listOf(
                navArgument("orderId") { type = NavType.StringType },
                navArgument("paymentMethod") { type = NavType.StringType },
                navArgument("totalPrice") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            val method = backStackEntry.arguments?.getString("paymentMethod") ?: ""
            val totalPrice = backStackEntry.arguments?.getInt("totalPrice") ?: 0
            PaymentSuccessScreen(
                orderId = orderId,
                paymentMethod = method,
                totalPrice = totalPrice,
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
