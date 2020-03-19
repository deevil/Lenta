package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.CreateTask
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoResult
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.utilities.extentions.isSapTrue
import javax.inject.Inject

class CreateTaskManager @Inject constructor(
        private val database: IDatabaseRepository
) : ICreateTaskManager {

    override var searchNumber = ""

    override val task = MutableLiveData<CreateTask>()

    override val currentGood = MutableLiveData<Good>()


    override fun updateTask(createTask: CreateTask) {
        task.value = createTask
    }

    override suspend fun putInCurrentGood(goodInfo: GoodInfoResult) {
        currentGood.value = Good(
                ean = goodInfo.eanInfo.ean,
                material = goodInfo.materialInfo.material,
                name = goodInfo.materialInfo.name,
                innerQuantity = goodInfo.materialInfo.innerQuantity.toDoubleOrNull() ?: 0.0,
                units = database.getUnitsByCode(goodInfo.materialInfo.unitCode),
                orderUnits = database.getUnitsByCode(goodInfo.materialInfo.orderUnitCode),
                type = if (goodInfo.materialInfo.isExcise.isSapTrue()) GoodType.EXCISE else if (goodInfo.materialInfo.isAlcohol.isSapTrue()) GoodType.ALCOHOL else GoodType.COMMON,
                isAlcohol = goodInfo.materialInfo.isAlcohol.isSapTrue(),
                isExcise = goodInfo.materialInfo.isExcise.isSapTrue(),
                providers = goodInfo.providers,
                producers = goodInfo.producers,
                matrix = getMatrixType(goodInfo.materialInfo.matrix),
                section = goodInfo.materialInfo.section
        )
    }

    override fun addCurrentGood() {
        task.value?.let { changedTask ->
            changedTask.goods.add(0, currentGood.value!!)
            task.value = changedTask
        }
    }

    override fun findGoodByEan(ean: String): Good? {
        return task.value?.goods?.find { it.ean == ean }
    }

    override fun findGoodByMaterial(material: String): Good? {
        return task.value?.goods?.find { it.material == material }
    }

    override suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean {
        return database.isGoodCanBeAdded(goodInfo, task.value!!.type.type)
    }
}


interface ICreateTaskManager {

    var searchNumber: String

    val task: MutableLiveData<CreateTask>
    val currentGood: MutableLiveData<Good>

    fun updateTask(createTask: CreateTask)
    suspend fun putInCurrentGood(goodInfo: GoodInfoResult)
    fun addCurrentGood()
    fun findGoodByEan(ean: String): Good?
    fun findGoodByMaterial(material: String): Good?
    suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean

}