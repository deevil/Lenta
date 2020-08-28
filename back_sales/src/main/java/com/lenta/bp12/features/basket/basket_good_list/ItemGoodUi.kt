package com.lenta.bp12.features.basket.basket_good_list

import com.lenta.bp12.model.pojo.Good

data class ItemGoodUi(
        val position: String,
        val name: String,
        val quantity: String,
        val material: String,
        val good: Good
)