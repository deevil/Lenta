package com.lenta.bp14.features.price_check.good_info

import com.lenta.bp14.models.check_price.ICheckPriceTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class GoodInfoPcViewModel : CoreViewModel() {
    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var task: ICheckPriceTask

    private val priceInfo by lazy {
        task.getProcessingActualPrice()
    }

    val priceInfoUi by lazy {
        ActualPriceInfoUi(
                price1 = priceInfo?.price1,
                price2 = priceInfo?.price2,
                price1Promotion = priceInfo?.price3,
                price2Sale = priceInfo?.price4
        )
    }

    fun getTitle(): String {
        return "${priceInfo?.matNumber?.takeLast(6)} ${priceInfo?.productName}"
    }

    fun onClickNoPrice() {
        setNewCheckStatusAndGoBack(null)
    }

    fun onClickNotValid() {
        setNewCheckStatusAndGoBack(false)
    }

    fun onClickValid() {
        setNewCheckStatusAndGoBack(true)

    }


    private fun setNewCheckStatusAndGoBack(isValid: Boolean?) {
        task.setCheckPriceStatus(isValid)
        navigator.goBack()
    }

}

data class ActualPriceInfoUi(
        val price1: Float?,
        val price2: Float?,
        val price1Promotion: Float?,
        val price2Sale: Float?
)
