package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.CreateTask
import com.lenta.bp12.repository.IDatabaseRepository
import javax.inject.Inject

class OpenTaskManager @Inject constructor(
        private val database: IDatabaseRepository
) : IOpenTaskManager {

    val tasks = MutableLiveData<List<CreateTask>>(emptyList())

    val currentTask = MutableLiveData<CreateTask>()

    val currentGood = MutableLiveData<Good>()

    override fun createTask() {

    }

}


interface IOpenTaskManager {

    fun createTask()

}