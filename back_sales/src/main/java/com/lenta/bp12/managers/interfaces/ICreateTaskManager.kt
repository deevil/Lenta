package com.lenta.bp12.managers.interfaces

import com.lenta.bp12.model.pojo.create_task.TaskCreate
import com.lenta.bp12.request.pojo.ProviderInfo

/**
 * implementation:
 * @see com.lenta.bp12.managers.CreateTaskManager
 * */
interface ICreateTaskManager: ITaskManager<TaskCreate> {

    var isWasAddedProvider: Boolean

    fun removeGoodByMaterials(materialList: List<String>)
    fun addProviderInCurrentGood(providerInfo: ProviderInfo)
}