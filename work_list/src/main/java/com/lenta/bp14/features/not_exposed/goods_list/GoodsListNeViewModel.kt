package com.lenta.bp14.features.not_exposed.goods_list

import androidx.lifecycle.LiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.filter.FilterFieldType
import com.lenta.bp14.models.filter.FilterParameter
import com.lenta.bp14.models.data.GoodsListTab
import com.lenta.bp14.models.data.pojo.Good
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.models.not_exposed_products.INotExposedProductsTask
import com.lenta.bp14.models.not_exposed_products.repo.INotExposedProductInfo
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.ProductInfoNetRequest
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequest
import com.lenta.shared.requests.combined.scan_info.analyseCode
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListNeViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var task: INotExposedProductsTask

    @Inject
    lateinit var scanInfoRequest: ScanInfoRequest

    @Inject
    lateinit var productInfoNetRequest: ProductInfoNetRequest

    val onOkFilterListener = object : OnOkInSoftKeyboardListener {
        override fun onOkInSoftKeyboard(): Boolean {
            applyFilter()
            return true
        }
    }

    val processedSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val correctedSelectedPage = selectedPage.map { getCorrectedPagePosition(it) }

    val taskName by lazy {
        "${task.getTaskType().taskType} // ${task.getTaskName()}"
    }

    val numberField: MutableLiveData<String> = MutableLiveData("")

    val filterField: MutableLiveData<String> = MutableLiveData("")

    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val processingGoods = MutableLiveData<List<Good>>()

    private val toUiFunc = { products: List<INotExposedProductInfo>? ->
        products?.reversed()?.mapIndexed { index, productInfo ->
            NotExposedProductUi(
                    position = products.size - index,
                    matNr = productInfo.matNr,
                    name = "${productInfo.matNr.takeLast(6)} ${productInfo.name}",
                    quantity = "${productInfo.quantity.toStringFormatted()} ${productInfo.uom?.name
                            ?: ""}",
                    isEmptyPlaceMarked = productInfo.isEmptyPlaceMarked
            )
        }
    }

    val processedGoods: LiveData<List<NotExposedProductUi>> by lazy {
        task.getProducts().map(toUiFunc)
    }
    val searchGoods: LiveData<List<NotExposedProductUi>> by lazy {
        task.getFilteredProducts().map(toUiFunc)
    }

    private val selectedItemOnCurrentTab: MutableLiveData<Boolean> = correctedSelectedPage
            .combineLatest(processedSelectionsHelper.selectedPositions)
            .map {
                val tab = it?.first?.toInt()
                val processedSelected = it?.second?.isNotEmpty() == true
                tab == GoodsListTab.PROCESSED.position && processedSelected || tab == GoodsListTab.SEARCH.position
            }

    val saveButtonEnabled by lazy { processedGoods.map { it?.isNotEmpty() ?: false } }

    val thirdButtonEnabled by lazy {
        selectedItemOnCurrentTab.combineLatest(saveButtonEnabled).map {
            when (correctedSelectedPage.value) {
                0, 1 -> selectedItemOnCurrentTab.value
                2 -> saveButtonEnabled.value
                else -> null
            }
        }
    }


    val thirdButtonVisibility = correctedSelectedPage.map { it != GoodsListTab.PROCESSING.position }

    init {
        viewModelScope.launch {
            requestFocusToNumberField.value = true

        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkCode(numberField.value)
        return true
    }

    private fun checkCode(code: String?) {
        analyseCode(
                code = code ?: "",
                funcForEan = { eanCode ->
                    searchCode(eanCode = eanCode)
                },
                funcForMatNr = { matNr ->
                    searchCode(matNr = matNr)
                },
                funcForPriceQrCode = { qrCode ->
                    navigator.showGoodNotFound()
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }


    private fun searchCode(eanCode: String? = null, matNr: String? = null) {
        require((!eanCode.isNullOrBlank() xor !matNr.isNullOrBlank()))
        viewModelScope.launch {
            navigator.showProgressLoadingData()
            task.getProductInfoAndSetProcessed(ean = eanCode, matNr = matNr).either(
                    {
                        navigator.openAlertScreen(failure = it)
                    }
            ) {
                navigator.openGoodInfoNeScreen()
            }

            navigator.hideProgress()
        }


    }



    fun onClickSave() {

    }

    private fun onClickDelete() {
        processedGoods.value!!.filterIndexed { index, _ ->
            processedSelectionsHelper.isSelected(position = index)
        }.map { it.matNr }.toSet().apply {
            task.removeCheckResultsByMatNumbers(this)
        }
        processedSelectionsHelper.clearPositions()
    }

    fun onClickThirdButton() {
        when (correctedSelectedPage.value) {
            1 -> onClickDelete()
            2 -> navigator.openSearchFilterWlScreen()
        }

    }

    fun onClickItemPosition(position: Int) {
        val correctedPage = correctedSelectedPage.value
        when (correctedPage) {
            1 -> {
                (processedGoods.value)?.getOrNull(position)?.let {
                    checkCode(it.matNr)
                }

            }
            2 -> {
                (searchGoods.value)?.getOrNull(position)?.let {
                    checkCode(it.matNr)
                }

            }
        }
    }

    fun getPagesCount(): Int {
        return if (task.isFreeMode()) 2 else 3
    }

    fun getCorrectedPagePosition(position: Int?): Int {
        return if (getPagesCount() == 3) position ?: 0 else (position ?: 0) + 1
    }

    fun onDigitPressed(digit: Int) {
        numberField.postValue(numberField.value ?: "" + digit)
        requestFocusToNumberField.value = true
    }

    fun applyFilter() {
        task.onFilterChanged(FilterParameter(FilterFieldType.NUMBER, filterField.value
                ?: ""))
    }

}


data class NotExposedProductUi(
        val position: Int,
        val matNr: String,
        val name: String,
        val quantity: String?,
        val isEmptyPlaceMarked: Boolean?
)
