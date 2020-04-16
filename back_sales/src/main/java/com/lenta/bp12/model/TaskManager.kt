package com.lenta.bp12.model

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.pojo.*
import com.lenta.bp12.platform.extention.getBlockType
import com.lenta.bp12.platform.extention.getControlType
import com.lenta.bp12.platform.extention.getGoodKind
import com.lenta.bp12.repository.IDatabaseRepository
import com.lenta.bp12.request.GoodInfoResult
import com.lenta.bp12.request.TaskContentResult
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.bp12.request.pojo.TaskInfo
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.utilities.extentions.isSapTrue
import javax.inject.Inject

class TaskManager @Inject constructor(
        private val database: IDatabaseRepository
) : ITaskManager {

    override var mode: Mode = Mode.CREATE_TASK

    override var searchNumber = ""

    override var openGoodFromList = false

    override var openPositionFromList = false

    override val tasks = MutableLiveData<List<Task>>(emptyList())

    override val currentTask = MutableLiveData<Task>()

    override val currentGood = MutableLiveData<Good>()

    override val currentBasket = MutableLiveData<Basket>()

    override val currentPosition = MutableLiveData<Position>()


    override fun updateTasks(taskList: List<Task>?) {
        tasks.value = taskList ?: emptyList()
    }

    override fun updateCurrentTask(task: Task?) {
        currentTask.value = task
    }

    override fun updateCurrentGood(good: Good?) {
        currentGood.value = good
    }

    override fun updateCurrentBasket(basket: Basket?) {
        currentBasket.value = basket
    }

    override fun updateCurrentPosition(position: Position?) {
        currentPosition.value = position
    }

    override suspend fun putInCurrentGood(goodInfo: GoodInfoResult) {
        currentGood.value = Good(
                ean = goodInfo.eanInfo.ean,
                material = goodInfo.materialInfo.material,
                name = goodInfo.materialInfo.name,
                innerQuantity = goodInfo.materialInfo.innerQuantity.toDoubleOrNull() ?: 0.0,
                units = database.getUnitsByCode(goodInfo.materialInfo.unitsCode),
                orderUnits = database.getUnitsByCode(goodInfo.materialInfo.orderUnitCode),
                kind = goodInfo.getGoodKind(),
                type = goodInfo.materialInfo.goodType,
                control = goodInfo.getControlType(),
                providers = goodInfo.providers.toMutableList(),
                producers = goodInfo.producers.toMutableList(),
                matrix = getMatrixType(goodInfo.materialInfo.matrix),
                section = goodInfo.materialInfo.section
        )
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

    override suspend fun addTasks(tasksInfo: List<TaskInfo>) {
        val taskList = tasksInfo.map { taskInfo ->
            Task(
                    number = taskInfo.number,
                    name = taskInfo.name,
                    type = TaskType(
                            type = taskInfo.type,
                            description = taskInfo.name,
                            section = taskInfo.section,
                            purchaseGroup = taskInfo.purchaseGroup,
                            goodGroup = taskInfo.goodGroup
                    ),
                    storage = taskInfo.storage,

                    isStrict = taskInfo.isStrict.isSapTrue(),
                    blockType = taskInfo.blockType.getBlockType(),
                    blockUser = taskInfo.blockUser,
                    blockIp = taskInfo.blockIp,
                    isProcessed = !taskInfo.isNotFinish.isSapTrue(),
                    control = taskInfo.control.getControlType(),
                    comment = taskInfo.comment,
                    provider = ProviderInfo(
                            code = taskInfo.providerCode,
                            name = taskInfo.providerName
                    ),
                    quantity = taskInfo.quantity.toIntOrNull() ?: 0,
                    reason = database.getReturnReasonList(taskInfo.type).first { it.code == taskInfo.reasonCode }
            )
        }

        updateTasks(taskList)
    }

    override suspend fun addGoodsInCurrentTask(taskContentResult: TaskContentResult) {
        currentTask.value?.let { task ->
            taskContentResult.positions.map { positionInfo ->
                val position = Position(
                        quantity = positionInfo.quantity.toDoubleOrNull() ?: 0.0,
                        provider = database.getProviderInfo(positionInfo.providerCode),
                        isCounted = positionInfo.isCounted.isSapTrue(),
                        isDelete = positionInfo.isDeleted.isSapTrue()
                )

                val good = task.goods.find { it.isSameMaterial(positionInfo.material) }
                        ?: database.getGoodByMaterial(positionInfo.material)

                good?.positions?.add(0, position)

                task.updateGood(good)
            }

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

    override fun prepareGoodAndPosition(material: String, providerCode: String) {
        findGoodByMaterial(material)?.let { good ->
            //openPositionFromList = true
            updateCurrentGood(good)

            good.positions.find { it.provider?.code == providerCode }?.let { position ->
                updateCurrentPosition(position)
            }
        }
    }

}


interface ITaskManager {

    var mode: Mode

    var searchNumber: String
    var openGoodFromList: Boolean
    var openPositionFromList: Boolean

    val tasks: MutableLiveData<List<Task>>

    val currentTask: MutableLiveData<Task>
    val currentGood: MutableLiveData<Good>
    val currentBasket: MutableLiveData<Basket>
    val currentPosition: MutableLiveData<Position>

    fun updateTasks(taskList: List<Task>?)
    fun updateCurrentTask(task: Task?)
    fun updateCurrentGood(good: Good?)
    fun updateCurrentBasket(basket: Basket?)
    fun updateCurrentPosition(position: Position?)

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
    suspend fun addTasks(tasksInfo: List<TaskInfo>)
    suspend fun addGoodsInCurrentTask(taskContentResult: TaskContentResult)
    fun prepareGoodAndPosition(material: String, providerCode: String)

}