package com.lenta.bp12.managers.interfaces

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.model.pojo.Mark
import com.lenta.bp12.model.pojo.Part
import com.lenta.bp12.model.pojo.create_task.TaskCreate
import com.lenta.bp12.request.pojo.ProviderInfo
/**
 * implementation:
 * @see com.lenta.bp12.managers.CreateTaskManager
 * */
interface ICreateTaskManager : ITaskManager {

    val currentTask: MutableLiveData<TaskCreate>
    var isWasAddedProvider: Boolean

    suspend fun addGoodToBasket(good: Good, part: Part? = null, provider: ProviderInfo, count: Double)
    suspend fun addGoodToBasketWithMark(good: Good, mark: Mark, provider: ProviderInfo)
    suspend fun addGoodToBasketWithMarks(good: Good, marks: List<Mark>, provider: ProviderInfo)
    suspend fun getOrCreateSuitableBasket(task: TaskCreate, good: Good, provider: ProviderInfo): Basket?

    fun updateCurrentTask(task: TaskCreate?)

    fun removeGoodByMaterials(materialList: List<String>)
    fun addProviderInCurrentGood(providerInfo: ProviderInfo)
}