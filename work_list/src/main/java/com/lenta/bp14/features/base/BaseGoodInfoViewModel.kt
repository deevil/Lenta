package com.lenta.bp14.features.base

import com.lenta.bp14.models.ui.ZPartUi
import com.lenta.bp14.models.work_list.Stock
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.models.work_list.ZPart
import com.lenta.bp14.platform.resource.IResourceFormatter
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.dropZeros
import javax.inject.Inject

abstract class BaseGoodInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var resourceFormatter: IResourceFormatter

    protected fun Stock.getZPartQuantity(goodUnitsName: String): String = if (hasZPart) {
        "${zPartsQuantity.dropZeros()} $goodUnitsName"
    } else {
        ""

    }

    protected fun List<ZPart>?.mapToZPartUiList(goodUnitsName: String): List<ZPartUi> {
        return this?.mapIndexed { index, zPart ->
            val quantity = "${zPart.quantity.dropZeros()} $goodUnitsName"
            ZPartUi(
                    "${index + 1}",
                    zPart.stock,
                    resourceFormatter.getFormattedZPartInfo(zPart),
                    quantity
            )
        }.orEmpty()
    }
}