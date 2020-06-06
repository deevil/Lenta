package com.lenta.movement.features.task.basket.info

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.models.Basket
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.IFormatter
import com.lenta.shared.platform.viewmodel.CoreViewModel
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

    val supplierVisible by lazy { MutableLiveData(basket.supplier != null) }

    val size by lazy { MutableLiveData("${basket.size} шт.") }
    val gisControl by lazy { MutableLiveData("Обычный товар") }
    val supplier by lazy { MutableLiveData(basket.supplier?.name.orEmpty()) }

    fun getTitle(): String {
        return formatter.getBasketDescription(basket, taskManager.getTask(), taskManager.getTaskSettings())
    }

}