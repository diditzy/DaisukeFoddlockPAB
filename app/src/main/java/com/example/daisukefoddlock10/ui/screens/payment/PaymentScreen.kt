package com.example.daisukefoddlock10.ui.screens.payment

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.daisukefoddlock10.R
import com.example.daisukefoddlock10.data.model.*
import com.example.daisukefoddlock10.formatRupiah
import com.example.daisukefoddlock10.ui.screens.SharedOrderViewModel
import com.example.daisukefoddlock10.ui.theme.*
import com.example.daisukefoddlock10.ui.components.SectionTitle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PaymentScreen(
    totalPrice: Int,
    viewModel: SharedOrderViewModel,
    onBack: () -> Unit,
    onPaymentSuccess: (orderId: String, paymentMethod: String) -> Unit
) {
    var selectedPayment by remember { mutableStateOf<PaymentMethod?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showQrisDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pilih Pembayaran") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 8.dp) {
                Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                    if (isProcessing) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(
                            "Memproses pembayaran...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Button(
                        onClick = {
                            if (selectedPayment?.id == "qris") {
                                showQrisDialog = true
                            } else {
                                isProcessing = true
                                scope.launch {
                                    delay(2000)
                                    val orderId = viewModel.confirmOrder(selectedPayment!!.name)
                                    viewModel.resetOrder()
                                    onPaymentSuccess(orderId, selectedPayment!!.name)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedPayment != null && !isProcessing,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Bayar Rp${formatRupiah(totalPrice)}", modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("💳 Total yang harus dibayar", style = MaterialTheme.typography.labelLarge)
                        Text(
                            "Rp${formatRupiah(totalPrice)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item { SectionTitle("🔵 E-Wallet") }
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 2
                ) {
                    paymentMethodList.filter { it.category == PaymentCategory.E_WALLET }.forEach { method ->
                        PaymentMethodItem(
                            method = method,
                            isSelected = selectedPayment?.id == method.id,
                            onClick = { selectedPayment = method },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item { SectionTitle("📷 QRIS") }
            item {
                val qrisMethod = paymentMethodList.first { it.id == "qris" }
                PaymentMethodItem(
                    method = qrisMethod,
                    isSelected = selectedPayment?.id == qrisMethod.id,
                    onClick = { selectedPayment = qrisMethod },
                    isFullWidth = true
                )
            }

            item { SectionTitle("🏦 Transfer Bank") }
            items(paymentMethodList.filter { it.category == PaymentCategory.BANK_TRANSFER }) { method ->
                PaymentMethodItem(
                    method = method,
                    isSelected = selectedPayment?.id == method.id,
                    onClick = { selectedPayment = method },
                    isFullWidth = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showQrisDialog) {
        AlertDialog(
            onDismissRequest = { showQrisDialog = false },
            title = { Text("Scan QRIS") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Image(
                        painter = painterResource(id = R.drawable.qrishdaisuke),
                        contentDescription = "QRIS Daisuke",
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Scan QR di atas menggunakan aplikasi GoPay, OVO, DANA, dll.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Batas waktu: 15:00", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showQrisDialog = false
                    isProcessing = true
                    scope.launch {
                        delay(1500)
                        val orderId = viewModel.confirmOrder("QRIS")
                        viewModel.resetOrder()
                        onPaymentSuccess(orderId, "QRIS")
                    }
                }) { Text("Konfirmasi Sudah Bayar") }
            },
            dismissButton = {
                TextButton(onClick = { showQrisDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
fun PaymentMethodItem(
    method: PaymentMethod,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFullWidth: Boolean = false
) {
    val brandColor = when (method.id) {
        "gopay" -> ColorGoPay
        "shopeepay" -> ColorShopeePay
        "ovo" -> ColorOVO
        "qris" -> ColorQRIS
        "dana" -> ColorDANA
        "bca" -> ColorBCA
        "mandiri" -> ColorMandiri
        "bni" -> ColorBNI
        "bri" -> ColorBRI
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth(if (isFullWidth) 1f else 0.5f)
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(brandColor, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(method.icon, contentDescription = null, tint = Color.White)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(method.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Text(method.processingTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun PaymentSuccessScreen(
    orderId: String,
    paymentMethod: String,
    onBackToHome: () -> Unit,
    onViewHistory: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFF2E7D32)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Pembayaran Berhasil!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text("Pesananmu sedang diproses", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SuccessInfoRow("No. Order", "#$orderId")
                SuccessInfoRow("Metode", paymentMethod)
                SuccessInfoRow("Status", "✅ Dikonfirmasi", color = Color(0xFF2E7D32))
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onBackToHome,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("🏠 Kembali ke Beranda", modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onViewHistory,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("📋 Lihat Riwayat Pesanan", modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun SuccessInfoRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}
