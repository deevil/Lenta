package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.CreateTask
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoResult
import javax.inject.Inject

class CreateTaskManager @Inject constructor(
        private val database: IDatabaseRepository
) : ICreateTaskManager {

    private val task = MutableLiveData<CreateTask>()

    private val currentGood = MutableLiveData<Good>()

    override fun getTask(): MutableLiveData<CreateTask> {
        return task
    }

    override fun updateTask(createTask: CreateTask) {
        task.value = createTask
    }

    override fun addGood(goodInfo: GoodInfoResult) {
        task.value?.let { changedTask ->
            changedTask.goods.add(0, Good(
                    ean = "000",
                    material = "111",
                    name = "222"
            ))

            currentGood.value = changedTask.goods[0]
            task.value = changedTask
        }
    }

    override fun isGoodWasAdded(ean: String?, material: String?): Boolean {
        task.value?.goods?.find { good ->
            if (ean != null) good.ean == ean else good.material == material
        }?.let {
            currentGood.value = it
            return true
        }

        return false
    }

}


interface ICreateTaskManager {

    fun getTask(): MutableLiveData<CreateTask>
    fun updateTask(createTask: CreateTask)
    fun addGood(goodInfo: GoodInfoResult)
    fun isGoodWasAdded(ean: String? = null, material: String? = null): Boolean

}