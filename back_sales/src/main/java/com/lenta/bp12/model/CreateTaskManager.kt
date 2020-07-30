package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.model.pojo.create_task.GoodCreate
import com.lenta.bp12.model.pojo.create_task.TaskCreate
import com.lenta.bp12.platform.extention.isAlcohol
import com.lenta.bp12.platform.extention.isCommon
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoResult
import com.lenta.bp12.request.SendTaskDataParams
import com.lenta.bp12.request.pojo.*
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.toSapBooleanString
import com.lenta.shared.utilities.getStringFromDate
import javax.inject.Inject

class CreateTaskManager @Inject constructor(
        private val database: IDatabaseRepository,
        private val generalTaskManager: IGeneralTaskManager
) : ICreateTaskManager {

    override var searchNumber = ""

    override var searchFromList = false

    override var isWasAddedProvider = false

    override val currentTask = MutableLiveData<TaskCreate>()

    override val currentGood = MutableLiveData<GoodCreate>()

    override val currentBasket = MutableLiveData<Basket>()


    override fun updateCurrentTask(task: TaskCreate?) {
        currentTask.value = task
    }

    override fun updateCurrentGood(good: GoodCreate?) {
        currentGood.value = good
    }

    override fun updateCurrentBasket(basket: Basket?) {
        currentBasket.value = basket
    }

    override fun clearCurrentGood() {
        currentGood.value = null
    }

    override fun saveGoodInTask(good: GoodCreate) {
        currentTask.value?.let { task ->
            task.goods.find { it.material == good.material }?.let { good ->
                task.goods.remove(good)
            }

            task.goods.add(0, good)
            updateCurrentTask(task)
        }
    }

    override fun findGoodByEan(ean: String): GoodCreate? {
        return currentTask.value?.let { task ->
            task.goods.find { good ->
                good.eans.contains(ean)
            }?.also { found ->
                found.ean = ean
                updateCurrentTask(task)
            }
        }
    }

    override fun findGoodByMaterial(material: String): GoodCreate? {
        return currentTask.value?.goods?.find { it.material == material }
    }

    override suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean {
        return database.isGoodCanBeAdded(goodInfo, currentTask.value!!.type.code)
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

    override fun removeGoodByMaterials(materialList: List<String>) {
        currentTask.value?.let { task ->
            task.removeGoodByMaterials(materialList)
            updateCurrentTask(task)
        }
    }

    override fun removeGoodByBasketAndMaterials(basket: Basket, materials: MutableList<String>) {
        currentTask.value?.let { task ->
            task.removeGoodByBasketAndMaterials(basket, materials)
            updateCurrentTask(task)
        }
    }

    override fun removeBaskets(basketList: MutableList<Basket>) {
        currentTask.value?.let { task ->
            task.removeBaskets(basketList)
            updateCurrentTask(task)
        }
    }

    override fun addProviderInCurrentGood(providerInfo: ProviderInfo) {
        currentGood.value?.let { good ->
            good.providers.add(0, providerInfo)
            isWasAddedProvider = true

            updateCurrentGood(good)
        }
    }

    override fun prepareSendTaskDataParams(deviceIp: String, tkNumber: String, userNumber: String) {
        currentTask.value?.let { task ->
            val positions = mutableListOf<PositionInfo>()
            val marks = mutableListOf<MarkInfo>()
            val parts = mutableListOf<PartInfo>()
            val baskets = mutableListOf<BasketInfo>()
            val basketPositions = mutableListOf<BasketPositionInfo>()

            task.baskets.forEach { basket ->
                val basketNumber = "${task.baskets.indexOf(basket) + 1}"

                baskets.add(
                        BasketInfo(
                                basketNumber = basketNumber,
                                isCommon = basket.control.isCommon().toSapBooleanString(),
                                isAlcohol = basket.control.isAlcohol().toSapBooleanString(),
                                providerCode = basket.provider.code,
                                goodType = basket.goodType,
                                section = basket.section
                        )
                )

                task.getGoodListByBasket(basket).forEach { good ->
                    basketPositions.add(
                            BasketPositionInfo(
                                    material = good.material,
                                    basketNumber = basketNumber,
                                    quantity = good.getQuantityByProvider(basket.provider.code).dropZeros()
                            )
                    )
                }
            }

            task.goods.forEach { good ->
                good.positions.forEach { position ->
                    val quantity = if (position.quantity > 0.0) position.quantity else good.getTotalQuantity()

                    positions.add(
                            PositionInfo(
                                    material = good.material,
                                    providerCode = position.provider.code,
                                    providerName = position.provider.name,
                                    factQuantity = quantity.dropZeros(),
                                    isCounted = true.toSapBooleanString(),
                                    innerQuantity = good.innerQuantity.dropZeros(),
                                    unitsCode = good.commonUnits.code
                            )
                    )
                }

                good.marks.map { mark ->
                    marks.add(
                            MarkInfo(
                                    material = good.material,
                                    number = mark.number,
                                    boxNumber = mark.boxNumber,
                                    isBadMark = mark.isBadMark.toSapBooleanString(),
                                    providerCode = mark.providerCode,
                                    basketNumber = task.getBasketNumber(good, mark.providerCode)
                            )
                    )
                }

                good.parts.map { part ->
                    parts.add(
                            PartInfo(
                                    material = good.material,
                                    producerCode = part.producerCode,
                                    productionDate = getStringFromDate(part.date, Constants.DATE_FORMAT_yyyyMMdd),
                                    quantity = part.quantity.dropZeros(),
                                    partNumber = part.number,
                                    providerCode = part.providerCode,
                                    basketNumber = task.getBasketNumber(good, part.providerCode)
                            )
                    )
                }
            }

            generalTaskManager.setSendTaskDataParams(
                    SendTaskDataParams(
                            deviceIp = deviceIp,
                            userNumber = userNumber,
                            taskName = task.name,
                            taskType = task.type.code,
                            tkNumber = tkNumber,
                            storage = task.storage,
                            reasonCode = task.reason.code,
                            isNotFinish = false.toSapBooleanString(),
                            positions = positions,
                            marks = marks,
                            parts = parts,
                            baskets = baskets,
                            basketPositions = basketPositions
                    )
            )
        }
    }

    override fun clearSearchFromListParams() {
        searchFromList = false
        searchNumber = ""
    }

}


interface ICreateTaskManager {

    var searchNumber: String
    var searchFromList: Boolean
    var isWasAddedProvider: Boolean

    val currentTask: MutableLiveData<TaskCreate>
    val currentGood: MutableLiveData<GoodCreate>
    val currentBasket: MutableLiveData<Basket>

    fun updateCurrentTask(task: TaskCreate?)
    fun updateCurrentGood(good: GoodCreate?)
    fun updateCurrentBasket(basket: Basket?)

    fun findGoodByEan(ean: String): GoodCreate?
    fun findGoodByMaterial(material: String): GoodCreate?
    suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean
    fun addBasket(basket: Basket)
    fun getBasketPosition(basket: Basket?): Int
    fun removeGoodByMaterials(materialList: List<String>)
    fun removeGoodByBasketAndMaterials(basket: Basket, materials: MutableList<String>)
    fun removeBaskets(basketList: MutableList<Basket>)
    fun addProviderInCurrentGood(providerInfo: ProviderInfo)
    fun prepareSendTaskDataParams(deviceIp: String, tkNumber: String, userNumber: String)
    fun saveGoodInTask(good: GoodCreate)
    fun clearSearchFromListParams()
    fun clearCurrentGood()

}