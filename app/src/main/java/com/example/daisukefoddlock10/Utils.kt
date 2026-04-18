package com.example.daisukefoddlock10

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatRupiah(amount: Int): String {
    return "%,d".format(amount).replace(',', '.')
}

fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}
