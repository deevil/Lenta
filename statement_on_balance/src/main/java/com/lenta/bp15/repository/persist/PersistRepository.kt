package com.lenta.bp15.repository.persist

import com.google.gson.Gson
import com.lenta.bp15.model.pojo.Task
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class PersistRepository @Inject constructor(
        private val hyperHive: HyperHive,
        private val gson: Gson
) : IPersistRepository {

    private val keyForSave = "SOB_TASK_DATA"

    override fun saveTaskData(task: Task) {
        hyperHive.stateAPI.saveParamToDB(keyForSave, gson.toJson(task))
    }

    override fun getSavedTaskData(): Task? {
        return hyperHive.stateAPI.getParamFromDB(keyForSave)?.let { json ->
            gson.fromJson(json, Task::class.java)
        }
    }

    override fun clearSavedData() {
        hyperHive.stateAPI.saveParamToDB(keyForSave, null)
    }

}

interface IPersistRepository {

    fun saveTaskData(task: Task)
    fun getSavedTaskData(): Task?
    fun clearSavedData()

}