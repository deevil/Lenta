package com.lenta.bp12.features.basket.basket_properties

import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class BasketPropertiesViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ICreateTaskManager

    @Inject
    lateinit var resource: IResourceManager


    private val task by lazy {
        manager.currentTask
    }

    val basket by lazy {
        manager.currentBasket
    }

    val title by lazy {
        basket.map { basket ->
            val position = manager.getBasketPosition(basket)
            val description = basket?.getDescription(task.value?.type?.isDivBySection ?: false)
            resource.basket("$position: $description")
        }
    }

    val properties by lazy {
        basket.map { basket ->
            BasketPropertiesUi(
                    section = basket?.section.orEmpty(),
                    goodType = basket?.goodType.orEmpty(),
                    gisControl = basket?.control?.description.orEmpty(),
                    provider = "${basket?.provider?.code} ${basket?.provider?.name}"
            )
        }
    }

}

data class BasketPropertiesUi(
        val section: String,
        val goodType: String,
        val gisControl: String,
        val provider: String
)
