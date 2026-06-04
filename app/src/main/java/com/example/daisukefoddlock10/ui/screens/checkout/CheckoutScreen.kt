package com.example.daisukefoddlock10.ui.screens.checkout

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.daisukefoddlock10.formatRupiah
import com.example.daisukefoddlock10.ui.screens.SharedOrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: SharedOrderViewModel,
    onBack: () -> Unit,
    onProceedPayment: (Int) -> Unit
) {
    val uiState by viewModel.orderState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Konfirmasi Pesanan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { onProceedPayment(uiState.totalPrice) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Pilih Pembayaran →", modifier = Modifier.padding(8.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SectionHeader("Ringkasan Pesanan") }
            item { OrderSummaryCard(uiState) }
            
            item { SectionHeader("Detail Pesanan") }
            item { OrderDetailCard(uiState) }

            item { SectionHeader("Mode Pengambilan") }
            item { DeliveryModeCard(uiState.isDelivery) }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun OrderSummaryCard(state: com.example.daisukefoddlock10.data.model.OrderState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = state.selectedFood.imageRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(state.selectedFood.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Ukuran: ${state.selectedSize.name.lowercase()}", style = MaterialTheme.typography.bodySmall)
                    if (state.toppings.isNotEmpty()) {
                        Text("Topping: ${state.toppings.joinToString { it.name.lowercase() }}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            PriceRow("Harga Dasar", state.selectedFood.basePrice)
            if (state.selectedSize == com.example.daisukefoddlock10.data.model.PortionSize.LARGE) {
                PriceRow("+ Ukuran Large", 5000)
            }
            state.toppings.forEach { topping ->
                PriceRow("+ ${topping.name.lowercase().replaceFirstChar { it.uppercase() }}", 3000)
            }
            if (state.isDelivery) {
                PriceRow("+ Biaya Antar", 5000)
            }
            state.appliedVoucher?.let { voucher ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🎟️ Diskon ${voucher.title}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32))
                    Text("-Rp${formatRupiah(state.discount)}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E7D32))
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 2.dp)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Rp${formatRupiah(state.totalPrice)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun PriceRow(label: String, amount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text("Rp${formatRupiah(amount)}", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun OrderDetailCard(state: com.example.daisukefoddlock10.data.model.OrderState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Catatan:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(
                text = state.notes.ifBlank { "Tidak ada catatan" },
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.notes.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Voucher:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(
                text = state.appliedVoucher?.title ?: "Tidak digunakan",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun DeliveryModeCard(isDelivery: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isDelivery) "🛵" else "🍽️",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isDelivery) "Delivery — Diantar ke lokasi" else "Dine In — Makan di tempat",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
