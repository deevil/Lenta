package com.lenta.bp14.features.not_exposed.good_info

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.data.pojo.Stock
import com.lenta.bp14.models.not_exposed_products.INotExposedProductsTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class GoodInfoNeViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var task: INotExposedProductsTask

    val firstStorageStock = MutableLiveData("125??")

    val selectedPage = MutableLiveData(0)

    val stocks = MutableLiveData<List<Stock>>()

    val quantityField = MutableLiveData<String>("0")

    private val frameTypeSelected = MutableLiveData(false)

    val cancelButtonEnabled = frameTypeSelected.map { true }

    val framedButtonEnabled = frameTypeSelected.combineLatest(quantityField).map {
        true
    }
    val notFramedButtonEnabled = frameTypeSelected.combineLatest(quantityField).map {
        true
    }

    init {
        /*viewModelScope.launch {
            good.value = taskManager.currentGood
            stocks.value = good.value?.stocks

            frameType.value = ""
        }*/
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickCancel() {

    }

    fun onClickFramed() {
        task.setCheckInfo(quantity = null, isEmptyPlaceMarked = true)
        navigator.goBack()
    }

    fun onClickNotFramed() {
        task.setCheckInfo(quantity = null, isEmptyPlaceMarked = false)
        navigator.goBack()
    }

    fun onClickApply() {
        quantityField.value?.toDoubleOrNull()?.let {
            task.setCheckInfo(quantity = it, isEmptyPlaceMarked = null)
        }
        navigator.goBack()
    }

    fun getTitle(): String? {
        task.scanInfoResult?.productInfo?.let {
            return "${it.getMaterialLastSix()} ${it.description}"
        }
        return null
    }

}
