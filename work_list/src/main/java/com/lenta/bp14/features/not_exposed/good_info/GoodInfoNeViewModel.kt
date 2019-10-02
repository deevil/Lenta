package com.lenta.bp14.features.not_exposed.good_info

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.features.work_list.good_info.ItemStockUi
import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.data.getGoodType
import com.lenta.bp14.models.not_exposed_products.INotExposedProductsTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.isSapTrue
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodInfoNeViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var task: INotExposedProductsTask

    private val goodInfo by lazy {
        task.getProcessedProductInfoResult()!!
    }

    val productParamsUi: MutableLiveData<ProductParamsUi> by lazy {
        MutableLiveData<ProductParamsUi>(
                goodInfo.let {
                    ProductParamsUi(
                            matrixType = getMatrixType(it.productInfo.matrixType),
                            sectionId = it.productInfo.sectionNumber,
                            type = it.productInfo.getGoodType(),
                            isNew = it.productInfo.isNew.isSapTrue(),
                            isHealthyFood = it.productInfo.isHealthyFood.isSapTrue()
                    )
                }
        )
    }


    val selectedPage = MutableLiveData(0)

    val stocks: MutableLiveData<List<ItemStockUi>> by lazy {
        MutableLiveData<List<ItemStockUi>>(
                goodInfo.let { goodInfo ->
                    goodInfo.stocks.mapIndexed { index, stock ->
                        ItemStockUi(
                                number = "${index + 1}",
                                storage = stock.lgort,
                                quantity = "${stock.stock.toStringFormatted()} ${goodInfo.uom?.name
                                        ?: ""}"
                        )

                    }
                }
        )
    }


    val originalProcessedProductInfo by lazy {
        task.getProcessedCheckInfo()
    }

    val marketStorage by lazy {
        "${(goodInfo.stocks.sumByDouble { it.stock }).toStringFormatted()} ${goodInfo.uom?.name
                ?: ""}"
    }

    val quantityField by lazy {
        MutableLiveData<String>().also {
            viewModelScope.launch {
                it.value = originalProcessedProductInfo?.quantity?.toStringFormatted() ?: "0"
            }
        }
    }

    val applyButtonEnabled: MutableLiveData<Boolean> by lazy {
        quantityField.map { it?.toDoubleOrNull() ?: 0.0 != 0.0 }
    }

    val isEmptyPlaceMarked by lazy {
        MutableLiveData<Boolean>(originalProcessedProductInfo?.isEmptyPlaceMarked)
    }

    val isInputNumberEnabled by lazy { isEmptyPlaceMarked.map { it == null } }

    val cancelButtonEnabled by lazy { isEmptyPlaceMarked.map { it != null } }

    val framedButtonEnabled by lazy {
        isEmptyPlaceMarked.map { it != true }
    }
    val notFramedButtonEnabled by lazy {
        isEmptyPlaceMarked.map { it != false }
    }


    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickCancel() {
        isEmptyPlaceMarked.value = null
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
        goodInfo.productInfo.let {
            return "${it.matNr.takeLast(6)} ${it.name}"
        }
    }

    fun onBackPressed(): Boolean {
        if (isHaveChangedData()) {
            navigator.openConfirmationNotSaveChanges {
                navigator.goBack()
            }
            return false
        }
        return true
    }

    private fun isHaveChangedData(): Boolean {
        return quantityField.value?.toDoubleOrNull() ?: 0.0 != originalProcessedProductInfo?.quantity ?: 0.0
    }

}

data class ProductParamsUi(
        val matrixType: MatrixType,
        val sectionId: String,
        val type: GoodType,
        val isNew: Boolean,
        val isHealthyFood: Boolean
)

