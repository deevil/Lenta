package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.Task
import com.lenta.bp12.repository.IDatabaseRepository
import javax.inject.Inject

class TaskManager @Inject constructor(
        private val database: IDatabaseRepository
) : ITaskManager {

    val tasks = MutableLiveData<List<Task>>(emptyList())

    override fun createTask() {

    }

}


interface ITaskManager {

    //val tasks: MutableLiveData<List<Task>>
    //val currentTask: MutableLiveData<Task>
    //val currentGood: MutableLiveData<Good>

    fun createTask()

}