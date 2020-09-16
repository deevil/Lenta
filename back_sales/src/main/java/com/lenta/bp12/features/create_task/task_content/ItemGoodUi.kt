package com.lenta.bp12.features.create_task.task_content

import com.lenta.bp12.model.MarkType
import com.lenta.bp12.model.pojo.Good

data class ItemGoodUi(
        val material: String,
        val position: String,
        val name: String,
        val quantity: String,
        val markType: MarkType,
        val good: Good
)