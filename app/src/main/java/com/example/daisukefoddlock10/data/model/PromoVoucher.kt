package com.example.daisukefoddlock10.data.model

data class PromoVoucher(
    val id: Int,
    val title: String,
    val description: String,
    val discountAmount: Int,
    val minOrder: Int = 0
)

val promoVoucherList = listOf(
    PromoVoucher(1, "HEMAT10", "Diskon Rp10.000 untuk pembelian pertama", 10000, minOrder = 20000),
    PromoVoucher(2, "GRATIS ONGKIR", "Gratis ongkos kirim min. order Rp30.000", 5000, minOrder = 30000),
    PromoVoucher(3, "SPESIAL WEEKEND", "Diskon 15% khusus Sabtu & Minggu", 15000, minOrder = 40000),
    PromoVoucher(4, "MEMBER VIP", "Cashback Rp20.000 untuk member VIP", 20000, minOrder = 50000)
)
