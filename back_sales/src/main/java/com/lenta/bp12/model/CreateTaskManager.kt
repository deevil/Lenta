package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.CreateTask
import com.lenta.bp12.repository.IDatabaseRepository
import javax.inject.Inject

class CreateTaskManager @Inject constructor(
        private val database: IDatabaseRepository
) : ICreateTaskManager {

    private val task = MutableLiveData<CreateTask>()

    override fun updateTask(createTask: CreateTask) {
        task.value = createTask
    }

}


interface ICreateTaskManager {

    fun updateTask(createTask: CreateTask)

}