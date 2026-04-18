package com.example.daisukefoddlock10.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.ui.graphics.vector.ImageVector

enum class PaymentCategory { E_WALLET, QRIS, BANK_TRANSFER }

data class PaymentMethod(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val category: PaymentCategory,
    val description: String,
    val processingTime: String
)

val paymentMethodList = listOf(
    // E-Wallet
    PaymentMethod("gopay", "GoPay", Icons.Default.AccountBalanceWallet, PaymentCategory.E_WALLET, "Bayar via GoPay", "Instan"),
    PaymentMethod("ovo", "OVO", Icons.Default.AccountBalanceWallet, PaymentCategory.E_WALLET, "Bayar via OVO", "Instan"),
    PaymentMethod("dana", "DANA", Icons.Default.AccountBalanceWallet, PaymentCategory.E_WALLET, "Bayar via DANA", "Instan"),
    PaymentMethod("shopeepay", "ShopeePay", Icons.Default.AccountBalanceWallet, PaymentCategory.E_WALLET, "Bayar via ShopeePay", "Instan"),
    // QRIS
    PaymentMethod("qris", "QRIS", Icons.Default.QrCode, PaymentCategory.QRIS, "Scan QR Code dari aplikasi apapun", "Instan"),
    // Bank Transfer
    PaymentMethod("bca", "Transfer BCA", Icons.Default.AccountBalance, PaymentCategory.BANK_TRANSFER, "Virtual Account BCA", "1-5 menit"),
    PaymentMethod("mandiri", "Transfer Mandiri", Icons.Default.AccountBalance, PaymentCategory.BANK_TRANSFER, "Virtual Account Mandiri", "1-5 menit"),
    PaymentMethod("bni", "Transfer BNI", Icons.Default.AccountBalance, PaymentCategory.BANK_TRANSFER, "Virtual Account BNI", "1-5 menit"),
    PaymentMethod("bri", "Transfer BRI", Icons.Default.AccountBalance, PaymentCategory.BANK_TRANSFER, "Virtual Account BRI", "1-5 menit")
)
