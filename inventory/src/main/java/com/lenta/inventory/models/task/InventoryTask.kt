package com.lenta.inventory.models.task

import com.lenta.inventory.models.repositories.ITaskRepository

class InventoryTask(val taskDescription: TaskDescription, val taskRepository: ITaskRepository) {

    //TODO возвращает модель сохранения задания
    fun getTaskSaveModel(): TaskSaveModel {
        return TaskSaveModel(taskDescription!!)
    }

    //вызывается при возврате на 20 экран и при нажатии на кнопку ОБНОВИТЬ, вызываем 96 рест
    fun updateStorePlaces() : InventoryTask {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun clearStorePlace() : InventoryTask {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun processStorePlace(storePlaceNumber: String) : StorePlaceProcessing{
        return StorePlaceProcessing(this,  storePlaceNumber)
    }






}