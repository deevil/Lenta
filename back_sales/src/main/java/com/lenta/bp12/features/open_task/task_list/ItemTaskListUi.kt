package com.lenta.bp12.features.open_task.task_list

import com.lenta.bp12.model.BlockType

data class ItemTaskListUi(
        val position: String,
        val number: String,
        val name: String,
        val provider: String,
        val isFinished: Boolean,
        val blockType: BlockType,
        val quantity: String
)