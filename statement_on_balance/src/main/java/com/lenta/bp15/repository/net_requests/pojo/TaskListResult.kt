package com.lenta.bp15.repository.net_requests.pojo

import com.google.gson.annotations.SerializedName
import com.lenta.bp15.model.pojo.Block
import com.lenta.bp15.model.pojo.Task
import com.lenta.shared.utilities.BlockType
import com.lenta.shared.utilities.extentions.IResultWithRetCodes
import com.lenta.shared.utilities.extentions.isSapTrue

data class TaskListResult(
        /** Список объектов */
        @SerializedName("ET_TASK_LIST")
        val tasks: List<TaskRawInfo>?,
        /** Таблица возврата */
        @SerializedName("ET_RETCODE")
        override val retCodes: List<RetCode>?
) : IResultWithRetCodes {

    fun convertToTasks(): List<Task>? {
        return tasks?.map { taskRawInfo ->
            Task(
                    number = taskRawInfo.number,
                    name = taskRawInfo.firstLine,
                    description = taskRawInfo.secondLine,
                    quantity = taskRawInfo.quantity.toDoubleOrNull() ?: 0.0,
                    block = Block(
                            type = BlockType.from(taskRawInfo.lockType),
                            user = taskRawInfo.lockUser,
                            ip = taskRawInfo.lockIp
                    ),
                    isFinished = !taskRawInfo.isNotFinish.isSapTrue()
            )
        }
    }
}