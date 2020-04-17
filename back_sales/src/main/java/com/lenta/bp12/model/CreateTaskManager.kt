package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.model.pojo.create_task.Good
import com.lenta.bp12.model.pojo.create_task.Task
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoResult
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.constants.Constants
import javax.inject.Inject

class CreateTaskManager @Inject constructor(
        private val database: IDatabaseRepository
) : ICreateTaskManager {


    override var searchNumber = ""

    override var openGoodFromList = false

    override val currentTask = MutableLiveData<Task>()

    override val currentGood = MutableLiveData<Good>()

    override val currentBasket = MutableLiveData<Basket>()


    override fun updateCurrentTask(task: Task?) {
        currentTask.value = task
    }

    override fun updateCurrentGood(good: Good?) {
        currentGood.value = good
    }

    override fun updateCurrentBasket(basket: Basket?) {
        currentBasket.value = basket
    }

    override suspend fun putInCurrentGood(goodInfo: GoodInfoResult) {
        val newGood = Good(
                ean = goodInfo.eanInfo.ean,
                material = goodInfo.materialInfo.material,
                name = goodInfo.materialInfo.name,
                units = database.getUnitsByCode(goodInfo.materialInfo.unitsCode),
                kind = goodInfo.getGoodKind(),
                type = goodInfo.materialInfo.goodType,
                control = goodInfo.getControlType(),
                section = goodInfo.materialInfo.section,
                matrix = getMatrixType(goodInfo.materialInfo.matrix),
                isFullData = true,
                innerQuantity = goodInfo.materialInfo.innerQuantity.toDoubleOrNull() ?: 0.0,
                orderUnits = database.getUnitsByCode(goodInfo.materialInfo.orderUnitCode),
                providers = goodInfo.providers.toMutableList(),
                producers = goodInfo.producers.toMutableList()
        )

        findGoodByMaterial(newGood.material)?.let { good ->
            newGood.positions = good.positions
        }

        currentGood.value = newGood
    }

    override fun addCurrentGoodInTask() {
        currentTask.value?.let { task ->
            task.goods.find { it.material == currentGood.value!!.material }?.let { good ->
                task.goods.remove(good)
            }

            task.goods.add(0, currentGood.value!!)
            updateCurrentTask(task)
        }
    }

    override fun findGoodByEan(ean: String): Good? {
        return currentTask.value?.goods?.find { it.ean == ean }
    }

    override fun findGoodByMaterial(material: String): Good? {
        val formattedMaterial = if (material.length == Constants.SAP_6) "000000000000$material" else material
        return currentTask.value?.goods?.find { it.material == formattedMaterial }
    }

    override suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean {
        return database.isGoodCanBeAdded(goodInfo, currentTask.value!!.type!!.type)
    }

    override fun addBasket(basket: Basket) {
        currentTask.value?.let { task ->
            task.baskets.add(basket)
            updateCurrentTask(task)
        }

        updateCurrentBasket(basket)
    }

    override fun getBasketPosition(basket: Basket?): Int {
        val position = currentTask.value?.baskets?.indexOf(basket)

        return if (position != null) position + 1 else 0
    }

    override fun deleteGoodByMaterials(materialList: List<String>) {
        currentTask.value?.let { task ->
            task.goods.let { goods ->
                materialList.forEach { material ->
                    goods.remove(goods.find { it.material == material })
                }
            }

            task.deleteEmptyBaskets()
            updateCurrentTask(task)
        }
    }

    override fun deleteBaskets(basketList: MutableList<Basket>) {
        currentTask.value?.let { task ->
            task.baskets.let { baskets ->
                basketList.forEach { basket ->
                    task.deleteGoodFromBasket(basket)
                    baskets.remove(basket)
                }
            }

            updateCurrentTask(task)
        }
    }

    override fun finishCurrentTask() {
        currentTask.value?.let { task ->
            task.isProcessed = true

            updateCurrentTask(task)
        }
    }

    override fun addProviderInCurrentGood(providerInfo: ProviderInfo) {
        currentGood.value?.let { good ->
            good.providers.add(0, providerInfo)

            updateCurrentGood(good)
        }
    }

}


interface ICreateTaskManager {

    var searchNumber: String
    var openGoodFromList: Boolean

    val currentTask: MutableLiveData<Task>
    val currentGood: MutableLiveData<Good>
    val currentBasket: MutableLiveData<Basket>

    fun updateCurrentTask(task: Task?)
    fun updateCurrentGood(good: Good?)
    fun updateCurrentBasket(basket: Basket?)

    suspend fun putInCurrentGood(goodInfo: GoodInfoResult)
    fun addCurrentGoodInTask()
    fun findGoodByEan(ean: String): Good?
    fun findGoodByMaterial(material: String): Good?
    suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean
    fun addBasket(basket: Basket)
    fun getBasketPosition(basket: Basket?): Int
    fun deleteGoodByMaterials(materialList: List<String>)
    fun deleteBaskets(basketList: MutableList<Basket>)
    fun finishCurrentTask()
    fun addProviderInCurrentGood(providerInfo: ProviderInfo)

}