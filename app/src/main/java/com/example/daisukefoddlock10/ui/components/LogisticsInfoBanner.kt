package com.example.daisukefoddlock10.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daisukefoddlock10.data.model.OrderLogistics
import com.example.daisukefoddlock10.data.model.OrderLogisticsStatus

@Composable
fun LogisticsInfoBanner(
    order: OrderLogistics,
    modifier: Modifier = Modifier
) {
    val isInTransit  = order.status == OrderLogisticsStatus.IN_TRANSIT
    val isDelivered  = order.status == OrderLogisticsStatus.DELIVERED
    val isPreparing  = order.status == OrderLogisticsStatus.PREPARING

    val containerColor by animateColorAsState(
        targetValue = when {
            isDelivered -> Color(0xFF1565C0)   // Biru gelap — selesai
            isInTransit -> Color(0xFF2E7D32)   // Hijau — sedang diantar
            else        -> Color(0xFFE8F5E9)   // Hijau muda — sedang disiapkan
        },
        label = "status_color"
    )
    val contentColor = when {
        isDelivered || isInTransit -> Color.White
        else                       -> Color(0xFF2E7D32)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when {
                            isDelivered -> Icons.Default.CheckCircle
                            isInTransit -> Icons.AutoMirrored.Filled.DirectionsRun
                            else        -> Icons.Default.Restaurant
                        },
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = when {
                            isDelivered -> "Pesanan Selesai! 🎉"
                            isInTransit -> order.origin_name
                            else        -> "Sedang Disiapkan..."
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = when {
                        isDelivered -> "Makananmu sudah tiba. Selamat menikmati!"
                        isInTransit -> "Tujuan: ${order.destination_name}"
                        else        -> "Pesananmu sedang diproses merchant"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = contentColor.copy(alpha = 0.8f)
                )
            }

            if (!isDelivered) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${order.total_eta} Min",
                        style = MaterialTheme.typography.titleLarge,
                        color = contentColor,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = if (isInTransit) "Sedang Diantar" else "Estimasi Kedatangan",
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
