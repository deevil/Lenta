package com.lenta.bp16.model.pojo

import com.lenta.bp16.model.TaskType
import com.lenta.bp16.request.pojo.GoodInfo
import com.lenta.bp16.request.pojo.PackInfo
import com.lenta.bp16.request.pojo.ProcessingUnit
import com.lenta.bp16.request.pojo.RawInfo

data class Task(
        var isProcessed: Boolean = false,
        val type: TaskType,
        val processingUnit: ProcessingUnit,
        var goods: List<GoodInfo>? = null,
        var raws: List<RawInfo>? = null,
        var packs: List<PackInfo>? = null
) {
}