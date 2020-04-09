package com.lenta.bp12.features.create_task.basket_properties

import com.lenta.bp12.model.ITaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class BasketPropertiesViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var manager: ITaskManager


    val basket by lazy {
        manager.currentBasket
    }

    val title by lazy {
        basket.map { basket ->
            "Корзина ${manager.getBasketPosition(basket)}: ${basket?.getDescription()}"
        }
    }

    val properties by lazy {
        basket.map { basket ->
            BasketPropertiesUi(
                    section = basket?.section ?: "",
                    type = basket?.type ?: "",
                    gisControl = basket?.control?.description ?: "",
                    provider = "${basket?.provider?.code} ${basket?.provider?.name}"
            )
        }
    }

}

data class BasketPropertiesUi(
        val section: String,
        val type: String,
        val gisControl: String,
        val provider: String
)
