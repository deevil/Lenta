package com.lenta.bp14.features.price_check.price_scanner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.ml.CheckStatus
import com.lenta.bp14.models.check_price.CheckPriceResult
import com.lenta.bp14.models.check_price.ICheckPriceTask
import com.lenta.bp14.models.check_price.toCheckStatus
import com.lenta.bp14.models.getTaskName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class PriceScannerViewModel : CoreViewModel() {

    @Inject
    lateinit var task: ICheckPriceTask

    @Inject
    lateinit var sessionInfo: ISessionInfo

    private val checkPriceResult: MutableLiveData<CheckPriceResult?> = MutableLiveData()

    val resultUi: LiveData<CheckPriceResultUi> = checkPriceResult.map { it.toUi() }


    fun getTitle(): String {
        return "${task.getTaskType().taskType} // ${task.getTaskName()}"
    }

    fun checkStatus(rawCode: String): CheckStatus? {
        return task.checkProductFromVideoScan(rawCode = rawCode)?.apply {
            checkPriceResult.value = this
        }?.toCheckStatus() ?: CheckStatus.ERROR.apply {
            checkPriceResult.value = null
        }
    }

}

private fun CheckPriceResult?.toUi(): CheckPriceResultUi? {
    return this?.let {
        CheckPriceResultUi(
                productTitle = this.matNr?.takeLast(6) ?: "",
                price = this.actualPriceInfo.price1.toString(),
                discountPrice = this.actualPriceInfo.getDiscountCardPrice().toString(),
                priceIsValid = this.isPriceValid(),
                discountPriceIsValid = this.isDiscountPriceValid(),
                isAdded = true
        )
    } ?: CheckPriceResultUi(
            productTitle = "Неизвестный",
            price = "",
            discountPrice = "",
            priceIsValid = null,
            discountPriceIsValid = null,
            isAdded = false
    )
}

data class CheckPriceResultUi(
        val productTitle: String,
        val price: String,
        val discountPrice: String,
        val priceIsValid: Boolean?,
        val discountPriceIsValid: Boolean?,
        val isAdded: Boolean
)
