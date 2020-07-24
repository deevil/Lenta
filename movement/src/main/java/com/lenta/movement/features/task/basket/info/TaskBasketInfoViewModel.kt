package com.lenta.movement.features.task.basket.info

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.models.Basket
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.IFormatter
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.asyncLiveData
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
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

    val title by unsafeLazy {
        asyncLiveData<String> {
            val task = taskManager.getTask()
            val taskSettings = taskManager.getTaskSettings()
            val innerTitle = formatter.getBasketTitle(
                    basket = basket,
                    task = task,
                    taskSettings = taskSettings
            )
            emit(innerTitle)
        }
    }

    companion object {
        private const val PCS_ABBREVIATION = "шт."
    }
}