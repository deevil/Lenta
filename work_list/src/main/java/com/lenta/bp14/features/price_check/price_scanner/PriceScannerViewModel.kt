package com.lenta.bp14.features.price_check.price_scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.ml.CheckStatus
import com.lenta.bp14.models.check_price.CheckPriceTaskManager
import com.lenta.bp14.models.check_price.ICheckPriceResult
import com.lenta.bp14.models.check_price.toCheckStatus
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.models.getTaskType
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class PriceScannerViewModel : CoreViewModel() {

    @Inject
    lateinit var checkPriceTaskManager: CheckPriceTaskManager

    @Inject
    lateinit var sesionInfo: ISessionInfo

    private val checkPriceResult: MutableLiveData<ICheckPriceResult?> = MutableLiveData()

    val resultUi: LiveData<CheckPriceResultUi> = checkPriceResult.map { it.toUi() }


    fun getTitle(): String {
        return "${checkPriceTaskManager.getTaskType()} // ${checkPriceTaskManager.getTaskName()}"
    }

    fun checkStatus(rawCode: String): CheckStatus? {
        return checkPriceTaskManager.getTask()?.checkProductFromScan(rawCode = rawCode)?.apply {
            checkPriceResult.value = this
        }?.toCheckStatus()
    }

}

private fun ICheckPriceResult?.toUi(): CheckPriceResultUi? {
    return this?.let {
        CheckPriceResultUi(
                productTitle = this.matNr?.takeLast(6) ?: "",
                price = this.actualPriceInfo.price.toString(),
                discountPrice = this.actualPriceInfo.discountCardPrice.toString(),
                priceIsValid = this.isPriceValid(),
                discountPriceIsValid = this.isDiscountPriceValid(),
                isAdded = true
        )
    }
}

data class CheckPriceResultUi(
        val productTitle: String,
        val price: String,
        val discountPrice: String,
        val priceIsValid: Boolean?,
        val discountPriceIsValid: Boolean?,
        val isAdded: Boolean
)
