package com.lenta.bp14.models

import com.google.gson.Gson
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class PersistTaskData @Inject constructor(
        private val hyperHive: HyperHive,
        private val gson: Gson
) : IPersistTaskData {

    private val keyForSave = "WKL_TASK_DATA"

    override fun saveTaskData(taskData: TaskData?) {
        if (taskData?.data == null) {
            hyperHive.stateAPI.saveParamToDB(keyForSave, null)
        } else {
            hyperHive.stateAPI.saveParamToDB(keyForSave, gson.toJson(taskData))
        }
    }

    override fun getSavedTaskData(): TaskData? {
        hyperHive.stateAPI.getParamFromDB(keyForSave)?.let { json ->
            return gson.fromJson(json, TaskData::class.java)
        }
        return null
    }

    override fun clearSavedData() {
        saveTaskData(null)
    }

}

interface IPersistTaskData {
    fun saveTaskData(taskData: TaskData?)
    fun getSavedTaskData(): TaskData?
    fun clearSavedData()
}

data class TaskData(
        val taskType: String?,
        val data: String?
)