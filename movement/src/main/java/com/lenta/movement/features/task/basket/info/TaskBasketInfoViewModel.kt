package com.lenta.movement.features.task.basket.info

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.models.Basket
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject
import kotlin.properties.Delegates

class TaskBasketInfoViewModel: CoreViewModel() {

    var basketIndex by Delegates.notNull<Int>()

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var taskBasketsRepository: ITaskBasketsRepository

    @Inject
    lateinit var formatter: IFormatter

    private val basket: Basket
        get() = taskBasketsRepository.getBasketByIndex(basketIndex)

    val size by unsafeLazy { MutableLiveData("${basket.size} $PCS_ABBREVIATION") }
    val gisControl by unsafeLazy { MutableLiveData(formatter.basketGisControl(basket)) }
    val supplier by unsafeLazy { MutableLiveData(basket.supplier?.name.orEmpty()) }

    val gisControlVisible by unsafeLazy { gisControl.map { it.isNullOrEmpty().not() } }
    val supplierVisible by unsafeLazy { MutableLiveData(basket.supplier != null) }

    fun getTitle(): String {
        return "${formatter.getBasketName(basket)}: ${formatter.getBasketDescription(
            basket,
            taskManager.getTask(),
            taskManager.getTaskSettings()
        )}"
    }

    companion object {
        private const val PCS_ABBREVIATION = "шт."
    }
}