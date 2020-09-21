package com.lenta.bp15.features.task_list

import com.lenta.shared.utilities.BlockType

data class ItemTaskUi(
        val position: String,
        val name: String,
        val description: String,
        val isFinished: Boolean,
        val blockType: BlockType,
        val quantity: String
)