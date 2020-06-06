package com.lenta.movement.features.task.basket.info

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.models.Basket
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.IFormatter
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class TaskBasketInfoViewModel: CoreViewModel() {

    var basketIndex: Int = -1

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var taskBasketsRepository: ITaskBasketsRepository

    @Inject
    lateinit var formatter: IFormatter

    private val basket: Basket
        get() = taskBasketsRepository.getBasketByIndex(basketIndex)

    val size by lazy { MutableLiveData("${basket.size} шт.") }
    val gisControl by lazy { MutableLiveData(formatter.basketGisControl(basket)) }
    val supplier by lazy { MutableLiveData(basket.supplier?.name.orEmpty()) }

    val gisControlVisible by lazy { gisControl.map { it.isNullOrEmpty().not() } }
    val supplierVisible by lazy { MutableLiveData(basket.supplier != null) }

    fun getTitle(): String {
        return "${formatter.getBasketName(basket)}: ${formatter.getBasketDescription(
            basket,
            taskManager.getTask(),
            taskManager.getTaskSettings()
        )}"
    }

}