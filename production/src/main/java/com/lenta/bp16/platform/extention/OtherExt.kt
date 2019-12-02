package com.lenta.bp16.platform.extention

import com.lenta.bp16.model.TaskType
import com.lenta.bp16.request.pojo.Task
import com.lenta.shared.utilities.extentions.isSapTrue


fun Task.getTaskType(): TaskType {
    return when (this.blockType) {
        1 -> TaskType.SELF_LOCK
        2 -> TaskType.LOCK
        else -> {
            when {
                this.isPlay.isSapTrue() -> TaskType.STARTED
                this.isPack.isSapTrue() -> TaskType.PACKING
                else -> TaskType.COMMON
            }
        }
    }
}