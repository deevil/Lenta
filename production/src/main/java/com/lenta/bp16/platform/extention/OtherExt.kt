package com.lenta.bp16.platform.extention

import com.lenta.bp16.model.IDataInfo
import com.lenta.bp16.model.TaskStatus
import com.lenta.bp16.platform.Constants
import com.lenta.bp16.request.pojo.TaskInfo
import com.lenta.shared.utilities.extentions.isSapTrue


fun TaskInfo.getTaskStatus(): TaskStatus {
    return when (this.blockType) {
        "1" -> TaskStatus.SELF_LOCK
        "2" -> TaskStatus.LOCK
        else -> {
            when {
                this.isPlay.isSapTrue() -> TaskStatus.STARTED
                else -> TaskStatus.COMMON
            }
        }
    }
}

fun getFieldWithSuffix(field: String?, suffix: String): String {
    return field?.takeIf { it.isNotEmpty() }?.run {
        buildString {
            append(field)
            append(" ")
            append(suffix)
        }
    }.orEmpty()
}

fun <T : IDataInfo, K> Iterable<T>.distinctAndAddFirstValue(selector: (T) -> K, mapper: (T) -> String): List<String> {
    val listWithoutRepeat = this.distinctBy(selector)
    val list = listWithoutRepeat.map(mapper).toMutableList()
    if (list.size > 1) {
        list.add(0, Constants.CHOOSE_ITEM)
    }
    return list
}