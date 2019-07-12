package com.lenta.inventory.models.task

import com.lenta.inventory.models.RecountType
import com.lenta.inventory.requests.network.TasksItem
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.GisControl
import javax.inject.Inject

class TaskDescription (val taskNumber: String,
                       val taskName: String,
                       val taskType: String,
                       val stock: String,
                       val isRecount: Boolean,
                       val isStrict: Boolean,
                       val blockType: String,
                       val lockUser: String,
                       val lockIP: String, //????
                       val productsInTask: Int, //NUM_POS - rest91
                       val isStarted: Boolean, //!notFinish - rest91
                       val dateFrom: String,
                       val dateTo: String,
                       val taskDeadLine: String, //Время на обработку задания (строка) REST-96
                       val recountType: RecountType, //mode - rest91
                       val gis: GisControl) {

    @Inject
    lateinit var sessionInfo: ISessionInfo

    fun getTaskTypeAndNumber() : String {
        return "$taskType-$taskNumber"
    }

    fun isAlco() : Boolean {
        return gis == GisControl.Alcohol
    }

    fun isBlockedByMe() : Boolean {
        return blockType == "1" && lockUser == sessionInfo.userName
    }

    fun isBlocked() : Boolean {
        return blockType == "1" && lockUser != sessionInfo.userName
    }

    fun isMultiUser() : Boolean {
        return blockType == "2"
    }

    companion object {
        fun from(taskInfo: TasksItem, recountType: RecountType, deadline: String) : TaskDescription {
            return TaskDescription(taskNumber = taskInfo.taskNumber,
                    taskName = taskInfo.taskName,
                    taskType = taskInfo.taskType,
                    stock = taskInfo.stock,
                    isRecount = taskInfo.isRecount.isNotEmpty(),
                    isStrict = taskInfo.isStrict.isNotEmpty(),
                    blockType = taskInfo.blockType,
                    lockUser = taskInfo.lockUser,
                    lockIP = taskInfo.lockIP,
                    productsInTask = taskInfo.countProductsInTask.toInt(),
                    isStarted = taskInfo.notFinish.isNotEmpty(),
                    dateFrom = taskInfo.dateFrom,
                    dateTo = taskInfo.dateTo,
                    taskDeadLine = deadline,
                    recountType = recountType,
                    gis = GisControl.GeneralProduct) //TODO: taskInfo.gisControl string to GisControl value
        }
    }
}