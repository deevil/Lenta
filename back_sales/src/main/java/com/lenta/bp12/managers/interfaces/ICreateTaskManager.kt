package com.lenta.bp12.managers.interfaces

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.create_task.TaskCreate
import com.lenta.bp12.request.pojo.ProviderInfo

/**
 * implementation:
 * @see com.lenta.bp12.managers.CreateTaskManager
 * */
interface ICreateTaskManager : ITaskManager {

    val currentTask: MutableLiveData<TaskCreate>
    var isWasAddedProvider: Boolean

    suspend fun getOrCreateSuitableBasket(task: TaskCreate, good: Good, provider: ProviderInfo): Basket?

    fun updateCurrentTask(task: TaskCreate?)

    fun removeGoodByMaterials(materialList: List<String>)
    fun addProviderInCurrentGood(providerInfo: ProviderInfo)
}