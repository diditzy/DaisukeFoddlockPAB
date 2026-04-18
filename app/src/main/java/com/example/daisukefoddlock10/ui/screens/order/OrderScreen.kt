package com.example.daisukefoddlock10.ui.screens.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.daisukefoddlock10.data.model.PromoVoucher
import com.example.daisukefoddlock10.data.model.promoVoucherList
import com.example.daisukefoddlock10.formatRupiah
import com.example.daisukefoddlock10.ui.components.*
import com.example.daisukefoddlock10.ui.screens.SharedOrderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    viewModel: SharedOrderViewModel,
    onCheckout: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val uiState by viewModel.orderState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daisuke Foodlocks 🍱", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.shadow(8.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Harga", style = MaterialTheme.typography.labelMedium)
                        Text(
                            "Rp${formatRupiah(uiState.totalPrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onCheckout,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text("🛒 Pesan Sekarang")
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            item { ProductHeader(food = uiState.selectedFood) }
            item {
                FoodMenuSelector(
                    selectedFood = uiState.selectedFood,
                    onFoodSelected = { viewModel.selectFood(it) }
                )
            }
            item {
                PortionSelector(
                    selectedSize = uiState.selectedSize,
                    onSizeSelected = { viewModel.updateSize(it) }
                )
            }
            item {
                SpicyLevelSelector(
                    spicyLevel = uiState.spicyLevel,
                    onLevelChanged = { viewModel.updateSpicyLevel(it) }
                )
            }
            item {
                ToppingSelector(
                    selectedToppings = uiState.toppings,
                    onToppingToggled = { viewModel.toggleTopping(it) }
                )
            }
            item {
                TakeawaySelector(
                    isTakeaway = uiState.isTakeaway,
                    onTakeawayChanged = { viewModel.updateTakeaway(it) }
                )
            }
            item {
                VoucherSelector(
                    appliedVoucher = uiState.appliedVoucher,
                    onVoucherSelected = { viewModel.applyVoucher(it) }
                )
            }
            item {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.updateNotes(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    label = { Text("Catatan Pesanan (Opsional)") },
                    placeholder = { Text("Contoh: Jangan pakai bawang") },
                    shape = RoundedCornerShape(12.dp)
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun VoucherSelector(
    appliedVoucher: PromoVoucher?,
    onVoucherSelected: (PromoVoucher?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SectionTitle("🎟️ Promo Tersedia")
            
            if (appliedVoucher != null) {
                InputChip(
                    selected = true,
                    onClick = { onVoucherSelected(null) },
                    label = { Text(appliedVoucher.title) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(promoVoucherList) { voucher ->
                    val isSelected = voucher.id == appliedVoucher?.id
                    Card(
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { onVoucherSelected(voucher) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                             else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(voucher.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            Text(voucher.description, style = MaterialTheme.typography.labelSmall, maxLines = 2)
                            Text("Min. Rp${formatRupiah(voucher.minOrder)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
