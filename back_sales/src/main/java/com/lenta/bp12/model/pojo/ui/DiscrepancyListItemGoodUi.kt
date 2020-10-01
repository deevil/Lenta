package com.lenta.bp12.model.pojo.ui

import com.lenta.bp12.model.pojo.Good

data class DiscrepancyListItemGoodUi(
        val position: String,
        val name: String,
        val material: String,
        val providerCode: String,
        val quantity: String,
        val good: Good
)