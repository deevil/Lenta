package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.model.pojo.create_task.Good
import com.lenta.bp12.model.pojo.create_task.Task
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoResult
import com.lenta.bp12.request.SendTaskDataParams
import com.lenta.bp12.request.pojo.MarkInfo
import com.lenta.bp12.request.pojo.PartInfo
import com.lenta.bp12.request.pojo.PositionInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.toSapBooleanString
import javax.inject.Inject

class CreateTaskManager @Inject constructor(
        private val database: IDatabaseRepository,
        private val generalTaskManager: IGeneralTaskManager
) : ICreateTaskManager {


    override var searchNumber = ""

    override var openGoodFromList = false

    override var isWasAddedProvider = false

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

    override fun saveGoodInTask(good: Good) {
        currentTask.value?.let { task ->
            task.goods.find { it.material == good.material }?.let { good ->
                task.goods.remove(good)
            }

            task.goods.add(0, good)
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
        return database.isGoodCanBeAdded(goodInfo, currentTask.value!!.properties.type)
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

    override fun removeBaskets(basketList: MutableList<Basket>) {
        currentTask.value?.let { task ->
            task.removeBaskets(basketList)
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
            isWasAddedProvider = true

            updateCurrentGood(good)
        }
    }

    override fun prepareSendTaskDataParams(deviceIp: String, tkNumber: String, userNumber: String) {
        currentTask.value?.let { task ->
            val positions = mutableListOf<PositionInfo>()
            val marks = mutableListOf<MarkInfo>()
            val parts = mutableListOf<PartInfo>()

            task.goods.forEach { good ->
                good.positions.forEach { position ->
                    positions.add(
                            PositionInfo(
                                    material = good.material,
                                    providerCode = position.provider?.code ?: "",
                                    providerName = position.provider?.name ?: "",
                                    quantity = position.quantity.dropZeros(),
                                    isCounted = true.toSapBooleanString(),
                                    isDeleted = false.toSapBooleanString(),
                                    unitsCode = good.units.code
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
                                    producerCode = mark.producerCode
                            )
                    )
                }

                good.parts.map { part ->
                    parts.add(
                            PartInfo(
                                    material = good.material,
                                    producerCode = part.producerCode,
                                    productionDate = part.date,
                                    unitsCode = part.units.code,
                                    quantity = part.quantity.dropZeros(),
                                    partNumber = part.number,
                                    providerCode = part.providerCode
                            )
                    )
                }
            }

            generalTaskManager.setSendTaskDataParams(
                    SendTaskDataParams(
                            deviceIp = deviceIp,
                            taskNumber = task.number,
                            userNumber = userNumber,
                            taskName = task.name,
                            taskType = task.properties.type,
                            tkNumber = tkNumber,
                            storage = task.storage,
                            reasonCode = task.reason.code,
                            isNotFinish = (!task.isProcessed).toSapBooleanString(),
                            positions = positions,
                            marks = marks,
                            parts = parts
                    )
            )
        }
    }

}


interface ICreateTaskManager {

    var searchNumber: String
    var openGoodFromList: Boolean
    var isWasAddedProvider: Boolean

    val currentTask: MutableLiveData<Task>
    val currentGood: MutableLiveData<Good>
    val currentBasket: MutableLiveData<Basket>

    fun updateCurrentTask(task: Task?)
    fun updateCurrentGood(good: Good?)
    fun updateCurrentBasket(basket: Basket?)

    fun findGoodByEan(ean: String): Good?
    fun findGoodByMaterial(material: String): Good?
    suspend fun isGoodCanBeAdded(goodInfo: GoodInfoResult): Boolean
    fun addBasket(basket: Basket)
    fun getBasketPosition(basket: Basket?): Int
    fun removeGoodByMaterials(materialList: List<String>)
    fun removeBaskets(basketList: MutableList<Basket>)
    fun finishCurrentTask()
    fun addProviderInCurrentGood(providerInfo: ProviderInfo)
    fun prepareSendTaskDataParams(deviceIp: String, tkNumber: String, userNumber: String)
    fun saveGoodInTask(good: Good)

}