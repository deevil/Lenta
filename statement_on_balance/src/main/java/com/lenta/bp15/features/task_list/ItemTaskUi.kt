package com.lenta.bp15.features.task_list

import com.lenta.shared.utilities.BlockType

data class ItemTaskUi(
        val position: String,
        val number: String,
        val firstLine: String,
        val secondLine: String,
        val isNotFinished: Boolean,
        val lockType: BlockType,
        val goodsQuantity: String
)