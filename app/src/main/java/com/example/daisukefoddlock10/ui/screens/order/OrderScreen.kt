package com.example.daisukefoddlock10.ui.screens.order

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import com.example.daisukefoddlock10.data.model.ToppingCategory
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
    onHistoryClick: () -> Unit,
    onSignOut: () -> Unit
) {
    val uiState by viewModel.orderState.collectAsStateWithLifecycle()
    val logisticsState by viewModel.activeOrderLogistics.collectAsStateWithLifecycle()
    val remainingMinutes by viewModel.remainingMinutes.collectAsStateWithLifecycle()
    var isVoucherSheetOpen by remember { mutableStateOf(false) }
    var isCartSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val cartSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daisuke Foodlocks 🍱", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onHistoryClick) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = onSignOut) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp, 
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
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
                Column {
                    AnimatedVisibility(visible = uiState.cartItems.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isCartSheetOpen = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Check, 
                                        contentDescription = null, 
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    AnimatedContent(
                                        targetState = uiState.cartItems.size,
                                        transitionSpec = {
                                            if (targetState > initialState) {
                                                (slideInVertically { height -> height } + fadeIn())
                                                    .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                                            } else {
                                                (slideInVertically { height -> -height } + fadeIn())
                                                    .togetherWith(slideOutVertically { height -> height } + fadeOut())
                                            }.using(
                                                SizeTransform(clip = false)
                                            )
                                        }, label = "CartCountAnimation"
                                    ) { count ->
                                        Text(
                                            "$count Item terpilih",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Text(
                                    "Lihat Keranjang 🛒",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .navigationBarsPadding(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Harga Menu Ini", style = MaterialTheme.typography.labelMedium)
                            AnimatedContent(
                                targetState = uiState.currentSelectionPrice,
                                transitionSpec = {
                                    fadeIn() togetherWith fadeOut()
                                }, label = "PriceAnimation"
                            ) { price ->
                                Text(
                                    "Rp ${formatRupiah(price)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            QuantitySelector(
                                quantity = uiState.quantity,
                                onQuantityChanged = { viewModel.updateQuantity(it) }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Button(
                                onClick = { viewModel.addToCart() },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                            ) {
                                Text("➕ Keranjang")
                            }
                        }
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
            item {
                AnimatedVisibility(visible = logisticsState != null) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        remainingMinutes.let { minutes ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (uiState.isDelivery) "🛵" else "⏳",
                                        style = MaterialTheme.typography.headlineMedium
                                    )
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = if (uiState.isDelivery) "Pesanan Sedang Diantar" else "Pesanan Sedang Disiapkan",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            text = if (uiState.isDelivery) "Estimasi Tiba: $minutes Menit" 
                                            else "Estimasi Selesai: $minutes Menit",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
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
                AnimatedVisibility(
                    visible = uiState.selectedFood.isSpicySupported,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    SpicyLevelSelector(
                        spicyLevel = uiState.spicyLevel,
                        onLevelChanged = { viewModel.updateSpicyLevel(it) }
                    )
                }
            }
            item {
                AnimatedVisibility(
                    visible = uiState.selectedFood.allowedToppingCategory != ToppingCategory.NONE,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    ToppingSelector(
                        allowedCategory = uiState.selectedFood.allowedToppingCategory,
                        selectedToppings = uiState.toppings,
                        onToppingToggled = { viewModel.toggleTopping(it) }
                    )
                }
            }
            item {
                DeliverySelector(
                    isDelivery = uiState.isDelivery,
                    onDeliveryChanged = { viewModel.updateDelivery(it) }
                )
            }
            item {
                AnimatedVisibility(
                    visible = uiState.isDelivery,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    OutlinedTextField(
                        value = uiState.deliveryAddress,
                        onValueChange = { viewModel.updateDeliveryAddress(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        label = { Text("📍 Detail Lokasi (Kelas/Ruangan)") },
                        placeholder = { Text("Contoh: Gedung A, Ruang 102") },
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )
                }
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
            item { Spacer(modifier = Modifier.height(100.dp)) }
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

        if (isCartSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { isCartSheetOpen = false },
                sheetState = cartSheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                CartBottomSheetContent(
                    orderState = uiState,
                    onRemoveItem = { viewModel.removeFromCart(it) },
                    onConfirmOrder = {
                        scope.launch { cartSheetState.hide() }.invokeOnCompletion {
                            isCartSheetOpen = false
                            onCheckout()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CartBottomSheetContent(
    orderState: com.example.daisukefoddlock10.data.model.OrderState,
    onRemoveItem: (String) -> Unit,
    onConfirmOrder: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            "Review Pesanan 🛒",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        
        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
            item {
                PaymentSummaryCard(
                    orderState = orderState,
                    onRemoveItem = onRemoveItem
                )
            }
        }
        
        Button(
            onClick = onConfirmOrder,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = orderState.cartItems.isNotEmpty()
        ) {
            Text("💳 Konfirmasi & Bayar (Rp ${formatRupiah(orderState.totalPrice)})")
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
                    overlineContent = { Text("Min. Order Rp ${formatRupiah(voucher.minOrder)}", color = MaterialTheme.colorScheme.primary) },
                    leadingContent = { 
                        RadioButton(selected = isSelected, onClick = null) 
                    },
                    modifier = Modifier.clickable { onVoucherSelected(voucher) }
                )
            }
        }
    }
}
