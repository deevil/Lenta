package com.lenta.bp15.features.task_list

import com.lenta.shared.utilities.BlockType

data class ItemTaskUi(
        val position: String,
        val number: String,
        val name: String,
        val description: String,
        val isFinished: Boolean,
        val lockType: BlockType,
        val quantity: String
)