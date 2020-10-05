package com.lenta.bp14.features.base

import androidx.lifecycle.LiveData
import com.lenta.bp14.models.ui.ZPartUi
import com.lenta.bp14.models.work_list.Stock
import com.lenta.bp14.models.work_list.ZPart
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.platform.resource.IResourceFormatter
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.orIfNull
import javax.inject.Inject

abstract class BaseGoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var resourceFormatter: IResourceFormatter

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    abstract val zParts: LiveData<List<ZPartUi>>

    protected fun Stock.getZPartQuantity(goodUnitsName: String): String = if (hasZPart) {
        "${zPartsQuantity.dropZeros()} $goodUnitsName"
    } else {
        ""
    }

    protected fun List<ZPart>?.mapToZPartUiList(goodUnitsName: String): List<ZPartUi> {
        return this?.mapIndexed { index, zPart ->
            val quantity = "${zPart.quantity.dropZeros()} $goodUnitsName"
            ZPartUi(
                    index = "${index + 1}",
                    stock = zPart.stock,
                    info = resourceFormatter.getFormattedZPartInfo(zPart),
                    largeInfo = resourceFormatter.getLargeFormattedZPartInfo(zPart),
                    quantity = quantity
            )
        }.orEmpty()
    }

    fun showZPartInfo(index: Int) {
        zParts.value?.getOrNull(index)?.let { zPart ->
            screenNavigator.openZPartInfoFragment(zPart)
        }.orIfNull {
            Logg.w { "ZPart value is null!" }
            screenNavigator.showAlertWithStockItemNotFound()
        }
    }
}