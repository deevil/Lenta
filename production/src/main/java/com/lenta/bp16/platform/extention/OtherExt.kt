package com.lenta.bp16.platform.extention

import com.lenta.bp16.model.TaskStatus
import com.lenta.bp16.model.TaskType
import com.lenta.bp16.request.pojo.TaskInfo
import com.lenta.shared.utilities.extentions.isSapTrue


fun TaskInfo.getTaskStatus(): TaskStatus {
    return when (this.blockType) {
        1 -> TaskStatus.SELF_LOCK
        2 -> TaskStatus.LOCK
        else -> {
            when {
                this.isPlay.isSapTrue() -> TaskStatus.STARTED
                this.isPack.isSapTrue() -> TaskStatus.PACKING
                else -> TaskStatus.COMMON
            }
        }
    }
}

fun TaskType.getTaskType(): Int {
    return when (this) {
        TaskType.PROCESSING_UNIT -> 1
        TaskType.EXTERNAL_SUPPLY -> 2
    }
}