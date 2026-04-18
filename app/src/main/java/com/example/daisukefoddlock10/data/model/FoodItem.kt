package com.example.daisukefoddlock10.data.model

import com.example.daisukefoddlock10.R

data class FoodItem(
    val id: Int,
    val name: String,
    val description: String,
    val basePrice: Int,
    val imageRes: Int
)

val foodMenuList = listOf(
    FoodItem(1, "Lasagna", "Pasta berlapis daging cincang & saus béchamel khas Italia", 45000, R.drawable.lasagna),
    FoodItem(2, "Mochi", "Kue beras Jepang kenyal dengan isian kacang merah manis", 18000, R.drawable.mochi),
    FoodItem(3, "Onigiri", "Nasi kepal Jepang dengan isian tuna mayo & balutan nori", 15000, R.drawable.onigiri),
    FoodItem(4, "Pangsit", "Pangsit goreng/rebus isi udang & daging ayam pilihan", 20000, R.drawable.pangsit),
    FoodItem(5, "Ramen", "Mie kuah kaldu tonkotsu gurih dengan topping chashu & telur", 38000, R.drawable.ramen),
    FoodItem(6, "Samyang", "Mie pedas Korea level extra hot dengan saus gochujang", 25000, R.drawable.samyang),
    FoodItem(7, "Sandwich", "Roti gandum lapis daging panggang, keju, & sayuran segar", 28000, R.drawable.sandwitch),
    FoodItem(8, "Kebab", "Daging sapi panggang berbumbu rempah dalam roti pita hangat", 30000, R.drawable.kebab),
    FoodItem(9, "Katsu", "Ayam/sapi goreng tepung crispy dengan saus katsu Jepang", 35000, R.drawable.katsu),
    FoodItem(10, "Dorayaki", "Kue pancake Jepang dengan isian selai kacang & madu", 16000, R.drawable.dorayaki)
)
