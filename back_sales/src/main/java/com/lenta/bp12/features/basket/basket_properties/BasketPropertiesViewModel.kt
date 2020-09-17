package com.lenta.bp12.features.basket.basket_properties

import com.lenta.bp12.managers.interfaces.ICreateTaskManager
import com.lenta.bp12.model.pojo.Basket
import com.lenta.bp12.model.pojo.extentions.getDescription
import com.lenta.bp12.model.pojo.extentions.getPosition
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
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
        basket.map { basket ->
            val position = basket.getPosition()
            val description = basket?.getDescription(
                    isDivBySection = task.value?.type?.isDivBySection ?: false,
                    isWholeSale = manager.isWholesaleTaskType
            )
            resource.basket("$position: $description")
        }
    }

    val properties by lazy {
        basket.map { basket ->
            BasketPropertiesUi(
                    section = basket?.section.orEmpty(),
                    goodType = basket?.goodType.orEmpty(),
                    gisControl = basket?.control?.description.orEmpty(),
                    provider = "${basket?.provider?.code} ${basket?.provider?.name}",
                    markTypeGroup = buildMarkTypeString(basket),
                    purchaseGroup = basket?.purchaseGroup.orEmpty()
            )
        }
    }

    private fun buildMarkTypeString(basket: Basket?) = buildString {
        val group = basket?.markTypeGroup?.name.orEmpty()
        append(group)
        val mrc = basket?.maxRetailPrice
        if (mrc.isNullOrEmpty().not()) {
            append("\n")
            append("МРЦ-")
            append(mrc)
            append(" руб")
        }
    }

    val isSectionVisible by unsafeLazy {
        basket.map {
            it?.section.isNullOrEmpty().not()
        }
    }

    val isGoodTypeVisible by unsafeLazy {
        basket.map {
            it?.goodType.isNullOrEmpty().not()
        }
    }

    val isProviderVisible by unsafeLazy {
        basket.map {
            it?.provider?.code.isNullOrEmpty().not()
        }
    }

    val isMarkTypeGroupVisible by unsafeLazy {
        basket.mapSkipNulls {
            it.markTypeGroup != null
        }
    }

    val isPurchaseGroupVisible by unsafeLazy {
        basket.map{
            it?.purchaseGroup.isNullOrEmpty().not()
        }
    }

}

data class BasketPropertiesUi(
        val section: String,
        val goodType: String,
        val gisControl: String,
        val provider: String,
        val markTypeGroup: String,
        val purchaseGroup: String
)
