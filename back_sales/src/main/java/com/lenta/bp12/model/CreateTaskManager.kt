package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.TaskCreate
import com.lenta.bp12.model.pojo.Good
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoResult
import com.lenta.shared.models.core.getMatrixType
import javax.inject.Inject

class CreateTaskManager @Inject constructor(
        private val database: IDatabaseRepository
) : ICreateTaskManager {

    override var searchNumber = ""

    override var searchFromList = false

    override val task = MutableLiveData<TaskCreate>()

    override val currentGood = MutableLiveData<Good>()

    override val currentBasket = MutableLiveData<Basket>()


    override fun updateTask(taskCreate: TaskCreate) {
        task.value = taskCreate
    }

    override suspend fun putInCurrentGood(goodInfo: GoodInfoResult) {
        currentGood.value = Good(
                ean = goodInfo.eanInfo.ean,
                material = goodInfo.materialInfo.material,
                name = goodInfo.materialInfo.name,
                innerQuantity = goodInfo.materialInfo.innerQuantity.toDoubleOrNull() ?: 0.0,
                units = database.getUnitsByCode(goodInfo.materialInfo.unitCode),
                orderUnits = database.getUnitsByCode(goodInfo.materialInfo.orderUnitCode),
                kind = goodInfo.getGoodKind(),
                type = goodInfo.materialInfo.goodType,
                control = goodInfo.getControlType(),
                providers = goodInfo.providers,
                producers = goodInfo.producers,
                matrix = getMatrixType(goodInfo.materialInfo.matrix),
                section = goodInfo.materialInfo.section
        )
    }

    override fun addGoodInTask() {
        task.value?.let { changedTask ->
            changedTask.goods.find { it.material == currentGood.value!!.material }?.let { good ->
                changedTask.goods.remove(good)
            }

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

    override fun addBasket(basket: Basket) {
        task.value?.let { changedTask ->
            changedTask.baskets.add(basket)
            task.value = changedTask
        }

        currentBasket.value = basket
    }

    override fun getBasketPosition(basket: Basket?): Int {
        val position = task.value?.baskets?.indexOf(basket)

        return if (position != null) position + 1 else 0
    }

    override fun deleteGoodByMaterials(materials: List<String>) {
        task.value?.let { changedTask ->
            changedTask.goods.let { goods ->
                materials.forEach { material ->
                    goods.remove(goods.find { it.material == material })
                }
            }

            task.value = changedTask
        }
    }
}


interface ICreateTaskManager {

    var searchNumber: String
    var searchFromList: Boolean

    val task: MutableLiveData<TaskCreate>
    val currentGood: MutableLiveData<Good>
    val currentBasket: MutableLiveData<Basket>

    fun updateTask(taskCreate: TaskCreate)
    suspend fun putInCurrentGood(goodInfo: GoodInfoResult)
    fun addGoodInTask()
    fun findGoodByEan(ean: String): Good?
    fun findGoodByMaterial(material: String): Good?
    suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean
    fun addBasket(basket: Basket)
    fun getBasketPosition(basket: Basket?): Int
    fun deleteGoodByMaterials(materials: List<String>)

}