package com.example.daisukefoddlock10.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daisukefoddlock10.data.model.*

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun FoodMenuSelector(
    selectedFood: FoodItem,
    onFoodSelected: (FoodItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SectionTitle("🍱 Pilih Menu")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(foodMenuList) { food ->
                    val isSelected = food.id == selectedFood.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .width(80.dp)
                            .clickable { onFoodSelected(food) }
                    ) {
                        Image(
                            painter = painterResource(id = food.imageRes),
                            contentDescription = food.name,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = food.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PortionSelector(
    selectedSize: PortionSize,
    onSizeSelected: (PortionSize) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SectionTitle("📏 Pilih Ukuran")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PortionSize.values().forEach { size ->
                    val isSelected = size == selectedSize
                    val priceLabel = if (size == PortionSize.LARGE) "(+Rp5.000)" else ""
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSizeSelected(size) },
                        label = { Text("${size.name.lowercase().replaceFirstChar { it.uppercase() }} $priceLabel") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SpicyLevelSelector(
    spicyLevel: Float,
    onLevelChanged: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SectionTitle("🌶️ Tingkat Pedas")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("😐", style = MaterialTheme.typography.headlineSmall)
                Slider(
                    value = spicyLevel,
                    onValueChange = onLevelChanged,
                    valueRange = 0f..5f,
                    steps = 4,
                    modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                )
                Text("🔥", style = MaterialTheme.typography.headlineSmall)
            }
            Text(
                text = "Level: ${spicyLevel.toInt()}",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun ToppingSelector(
    selectedToppings: Set<Topping>,
    onToppingToggled: (Topping) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            SectionTitle("➕ Tambah Topping (+Rp3.000/top)")
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Topping.values().forEach { topping ->
                    val isSelected = selectedToppings.contains(topping)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToppingToggled(topping) },
                        label = { Text(topping.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    maxItemsInEachRow: Int = Int.MAX_VALUE,
    content: @Composable () -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        maxItemsInEachRow = maxItemsInEachRow,
        content = { content() }
    )
}

@Composable
fun TakeawaySelector(
    isTakeaway: Boolean,
    onTakeawayChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🛍️ Bungkus / Takeaway", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text("(+Rp2.000)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Switch(checked = isTakeaway, onCheckedChange = onTakeawayChanged)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FoodMenuSelectorPreview() {
    FoodMenuSelector(selectedFood = foodMenuList[0], onFoodSelected = {})
}

@Preview(showBackground = true)
@Composable
fun PortionSelectorPreview() {
    PortionSelector(selectedSize = PortionSize.REGULAR, onSizeSelected = {})
}

@Preview(showBackground = true)
@Composable
fun SpicyLevelSelectorPreview() {
    SpicyLevelSelector(spicyLevel = 2f, onLevelChanged = {})
}

@Preview(showBackground = true)
@Composable
fun ToppingSelectorPreview() {
    ToppingSelector(selectedToppings = setOf(Topping.CHEESE), onToppingToggled = {})
}

@Preview(showBackground = true)
@Composable
fun TakeawaySelectorPreview() {
    TakeawaySelector(isTakeaway = true, onTakeawayChanged = {})
}
