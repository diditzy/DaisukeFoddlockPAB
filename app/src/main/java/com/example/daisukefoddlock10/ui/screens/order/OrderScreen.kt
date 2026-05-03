package com.example.daisukefoddlock10.ui.screens.order

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    viewModel: SharedOrderViewModel,
    onCheckout: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val uiState by viewModel.orderState.collectAsStateWithLifecycle()
    var isVoucherSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daisuke Foodlocks 🍱", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    onVoucherClick = { isVoucherSheetOpen = true }
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

        if (isVoucherSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { isVoucherSheetOpen = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                VoucherBottomSheetContent(
                    selectedVoucher = uiState.appliedVoucher,
                    onVoucherSelected = { 
                        viewModel.applyVoucher(it)
                        scope.launch { sheetState.hide() }.invokeOnCompletion { 
                            isVoucherSheetOpen = false 
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun VoucherSelector(
    appliedVoucher: PromoVoucher?,
    onVoucherClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = onVoucherClick
    ) {
        ListItem(
            headlineContent = { Text("🎟️ Promo & Voucher") },
            supportingContent = { 
                Text(appliedVoucher?.title ?: "Gunakan voucher biar lebih hemat!") 
            },
            trailingContent = { 
                if (appliedVoucher != null) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32))
                } else {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            },
            colors = ListItemDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

@Composable
fun VoucherBottomSheetContent(
    selectedVoucher: PromoVoucher?,
    onVoucherSelected: (PromoVoucher?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            "Pilih Voucher",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        LazyColumn {
            item {
                ListItem(
                    headlineContent = { Text("Tidak Menggunakan Voucher") },
                    leadingContent = { 
                        RadioButton(selected = selectedVoucher == null, onClick = null) 
                    },
                    modifier = Modifier.clickable { onVoucherSelected(null) }
                )
            }
            items(promoVoucherList) { voucher ->
                val isSelected = voucher.id == selectedVoucher?.id
                ListItem(
                    headlineContent = { Text(voucher.title, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text(voucher.description) },
                    overlineContent = { Text("Min. Order Rp${formatRupiah(voucher.minOrder)}", color = MaterialTheme.colorScheme.primary) },
                    leadingContent = { 
                        RadioButton(selected = isSelected, onClick = null) 
                    },
                    modifier = Modifier.clickable { onVoucherSelected(voucher) }
                )
            }
        }
    }
}
