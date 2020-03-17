package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.CreateTask
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoResult
import com.lenta.shared.utilities.extentions.isSapTrue
import javax.inject.Inject

class CreateTaskManager @Inject constructor(
        private val database: IDatabaseRepository
) : ICreateTaskManager {

    private val task = MutableLiveData<CreateTask>()

    private val currentGood = MutableLiveData<Good>()

    override fun getTask(): MutableLiveData<CreateTask> {
        return task
    }

    override fun getCurrentGood(): MutableLiveData<Good> {
        return currentGood
    }

    override fun updateTask(createTask: CreateTask) {
        task.value = createTask
    }

    override suspend fun addGood(goodInfo: GoodInfoResult) {
        task.value?.let { changedTask ->
            changedTask.goods.add(0, Good(
                    ean = goodInfo.eanInfo.ean,
                    material = goodInfo.materialInfo.material,
                    name = goodInfo.materialInfo.name,
                    innerQuantity = goodInfo.materialInfo.innerQuantity.toDoubleOrNull() ?: 0.0,
                    units = database.getUnitsByCode(goodInfo.materialInfo.unitCode),
                    type = if (goodInfo.materialInfo.isExcise.isSapTrue()) GoodType.EXCISE else if (goodInfo.materialInfo.isAlcohol.isSapTrue()) GoodType.ALCOHOL else GoodType.COMMON,
                    isAlcohol = goodInfo.materialInfo.isAlcohol.isSapTrue(),
                    isExcise = goodInfo.materialInfo.isExcise.isSapTrue(),
                    providers = goodInfo.providers

            ))

            currentGood.value = changedTask.goods[0]
            task.value = changedTask
        }
    }

    override fun isGoodWasAdded(ean: String?, material: String?): Boolean {
        require((ean != null) || (material != null)) {
            "One param must bu not null - ean: $ean, material: $material"
        }

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
    fun getCurrentGood(): MutableLiveData<Good>
    fun updateTask(createTask: CreateTask)
    suspend fun addGood(goodInfo: GoodInfoResult)
    fun isGoodWasAdded(ean: String? = null, material: String? = null): Boolean

}