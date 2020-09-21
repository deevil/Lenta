package com.lenta.bp12.features.open_task.good_list

import com.lenta.bp12.model.pojo.Good

data class ItemGoodProcessedUi(
        val position: String,
        val name: String,
        val quantity: String,
        val material: String,
        val providerCode: String,
        val good: Good
)