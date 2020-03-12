package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Task
import com.lenta.bp12.repository.IDatabaseRepository
import javax.inject.Inject

class CreateTaskManager @Inject constructor(
        private val database: IDatabaseRepository
) : ICreateTaskManager {

    val currentTask = MutableLiveData<Task>()

    val currentGood = MutableLiveData<Good>()

    override fun createTask() {

    }

}


interface ICreateTaskManager {

    fun createTask()

}