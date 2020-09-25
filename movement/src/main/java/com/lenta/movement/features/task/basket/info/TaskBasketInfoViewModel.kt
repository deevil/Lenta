package com.lenta.movement.features.task.basket.info

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.models.Basket
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.IFormatter
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.asyncLiveData
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.unsafeLazy
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject
import kotlin.properties.Delegates

class TaskBasketInfoViewModel : CoreViewModel() {

    var basketIndex by Delegates.notNull<Int>()

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var taskBasketsRepository: ITaskBasketsRepository

    @Inject
    lateinit var formatter: IFormatter

    @Inject
    lateinit var propertiesProvider: IBasketPropertiesExtractor

    private val basket: Basket?
        get() = taskBasketsRepository.getBasketByIndex(basketIndex)

    val size by unsafeLazy { MutableLiveData("${basket?.size} $PCS_ABBREVIATION") }
    val gisControl by unsafeLazy {
        val gis = basket?.let(formatter::basketGisControl)
        MutableLiveData(gis)
    }

    val gisControlVisible by unsafeLazy { gisControl.map { it != null } }

    val propertyItems by unsafeLazy {
        asyncLiveData<List<BasketProperty>> {
            emit(propertiesProvider.extractProperties(basket))
        }
    }

    val title by unsafeLazy {
        asyncLiveData<String> {
            basket?.let {
                val task = taskManager.getTask()
                val taskSettings = taskManager.getTaskSettings()
                val innerTitle = formatter.getBasketTitle(
                        basket = it,
                        task = task,
                        taskSettings = taskSettings
                )
                emit(innerTitle)
            }.orIfNull {
                Logg.e { "NO SUCH BASKET IN REPO" }
            }
        }
    }

    companion object {
        private const val PCS_ABBREVIATION = "шт."
    }
}