package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.TaskCreate
import com.lenta.bp12.repository.IDatabaseRepository
import javax.inject.Inject

class OpenTaskManager @Inject constructor(
        private val database: IDatabaseRepository
) : IOpenTaskManager {

    val tasks = MutableLiveData<List<TaskCreate>>(emptyList())

    val currentTask = MutableLiveData<TaskCreate>()

    val currentGood = MutableLiveData<Good>()

    override fun createTask() {

    }

}


interface IOpenTaskManager {

    fun createTask()

}