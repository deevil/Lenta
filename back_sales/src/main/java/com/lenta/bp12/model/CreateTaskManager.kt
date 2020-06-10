package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.create_task.Basket
import com.lenta.bp12.model.pojo.create_task.Good
import com.lenta.bp12.model.pojo.create_task.Task
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodType
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoResult
import com.lenta.bp12.request.SendTaskDataParams
import com.lenta.bp12.request.pojo.MarkInfo
import com.lenta.bp12.request.pojo.PartInfo
import com.lenta.bp12.request.pojo.PositionInfo
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.models.core.getMatrixType
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
                type = goodInfo.getGoodType(),
                matype = goodInfo.materialInfo.matype,
                control = goodInfo.getControlType(),
                section = goodInfo.materialInfo.section,
                matrix = getMatrixType(goodInfo.materialInfo.matrix),
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

    override fun addOrUpdateGood(good: Good) {
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
        return database.isGoodCanBeAdded(goodInfo, currentTask.value!!.properties!!.type)
    }

    override fun addBasket(basket: Basket) {
        currentTask.value?.let { task ->
            task.baskets.add(basket)
            updateCurrentTask(task)
        }

        //updateCurrentBasket(basket)
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
                                    markNumber = mark.markNumber,
                                    boxNumber = mark.boxNumber,
                                    isBadMark = mark.isBadMark.toSapBooleanString(),
                                    providerCode = mark.providerCode
                            )
                    )
                }

                good.parts.map { part ->
                    parts.add(
                            PartInfo(
                                    material = good.material,
                                    producer = part.producer,
                                    productionDate = part.productionDate,
                                    unitsCode = part.units.code,
                                    quantity = part.quantity.dropZeros(),
                                    partNumber = part.partNumber,
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
                            taskType = task.properties!!.type,
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
    fun prepareSendTaskDataParams(deviceIp: String, tkNumber: String, userNumber: String)
    fun addOrUpdateGood(good: Good)

}