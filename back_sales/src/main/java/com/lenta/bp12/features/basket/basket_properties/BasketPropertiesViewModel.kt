package com.lenta.bp12.features.basket.basket_properties

import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.model.pojo.extentions.getDescription
import com.lenta.bp12.model.pojo.extentions.getPosition
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.mapSkipNulls
import com.lenta.shared.utilities.extentions.unsafeLazy
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
        basket.mapSkipNulls { basket ->
            val position = basket.getPosition()
            val description = basket.getDescription(
                    isDivBySection = task.value?.type?.isDivBySection ?: false,
                    isWholeSale = manager.isWholesaleTaskType
            )
            resource.basket("$position: $description")
        }
    }

    val properties by lazy {
        basket.mapSkipNulls { basket ->
            BasketPropertiesUi(
                    section = basket.section.orEmpty(),
                    goodType = basket.goodType.orEmpty(),
                    gisControl = basket.control?.description.orEmpty(),
                    provider = "${basket.provider?.code} ${basket?.provider?.name}",
                    markTypeGroup = basket.markTypeGroup?.name.orEmpty(),
                    purchaseGroup = basket.purchaseGroup.orEmpty()
            )
        }
    }

    val isSectionVisible by unsafeLazy {
        basket.mapSkipNulls {
            it.section.isNullOrEmpty().not()
        }
    }

    val isGoodTypeVisible by unsafeLazy {
        basket.mapSkipNulls {
            it.goodType.isNullOrEmpty().not()
        }
    }

    val isProviderVisible by unsafeLazy {
        basket.mapSkipNulls {
            it.provider?.code.isNullOrEmpty().not()
        }
    }

    val isMarkTypeGroupVisible by unsafeLazy {
        basket.mapSkipNulls {
            it.markTypeGroup != null
        }
    }

    val isPurchaseGroupVisible by unsafeLazy {
        basket.mapSkipNulls{
            it.purchaseGroup.isNullOrEmpty().not()
        }
    }
}
