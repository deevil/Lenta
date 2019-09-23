package com.lenta.bp14.features.not_exposed.goods_list

import androidx.lifecycle.LiveData
import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.data.GoodsListTab
import com.lenta.bp14.models.data.pojo.Good
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.models.not_exposed_products.INotExposedProductsTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequest
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequestParams
import com.lenta.shared.requests.combined.scan_info.analyseCode
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListNeViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var task: INotExposedProductsTask

    @Inject
    lateinit var scanInfoRequest: ScanInfoRequest


    val processedSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val correctedSelectedPage = selectedPage.map { getCorrectedPagePosition(it) }

    val taskName by lazy {
        "${task.getTaskType().taskType} // ${task.getTaskName()}"
    }

    val numberField: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val processingGoods = MutableLiveData<List<Good>>()
    val processedGoods: LiveData<List<NotExposedProductUi>> by lazy {
        task.getProducts().map { products ->
            products?.mapIndexed { index, productInfo ->
                NotExposedProductUi(
                        position = products.size - index,
                        matNr = productInfo.matNr,
                        name = productInfo.name,
                        quantity = "${productInfo.quantity} ${productInfo.uom}",
                        isEmptyPlaceMarked = productInfo.isEmptyPlaceMarked
                )
            }
        }
    }
    val searchGoods = MutableLiveData<List<Good>>()

    private val selectedItemOnCurrentTab: MutableLiveData<Boolean> = correctedSelectedPage
            .combineLatest(processedSelectionsHelper.selectedPositions)
            .map {
                val tab = it?.first?.toInt()
                val processedSelected = it?.second?.isNotEmpty() == true
                tab == GoodsListTab.PROCESSED.position && processedSelected || tab == GoodsListTab.SEARCH.position
            }

    val thirdButtonEnabled = selectedItemOnCurrentTab.map { it }
    val saveButtonEnabled = processingGoods.map { it?.isNotEmpty() ?: false }

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
                    searchCode(number = eanCode, fromScan = true, isBarcode = true)
                },
                funcForMatNr = { matNr ->
                    searchCode(number = matNr, fromScan = false, isBarcode = false)
                },
                funcForPriceQrCode = { qrCode ->
                    navigator.showGoodNotFound()
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }

    private fun searchCode(number: String, fromScan: Boolean, isBarcode: Boolean?) {
        viewModelScope.launch {
            navigator.showProgress(scanInfoRequest)
            scanInfoRequest(
                    ScanInfoRequestParams(
                            number = number,
                            tkNumber = task.getDescription().tkNumber,
                            fromScan = fromScan,
                            isBarCode = isBarcode
                    )
            ).either(
                    fnL = {
                        navigator.openAlertScreen(it)
                    }
            ) {
                task.scanInfoResult = it
                navigator.openGoodInfoNeScreen()
            }
            navigator.hideProgress()
        }
    }


    fun onClickSave() {

    }

    private fun onClickDelete() {

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

}


data class NotExposedProductUi(
        val position: Int,
        val matNr: String,
        val name: String,
        val quantity: String?,
        val isEmptyPlaceMarked: Boolean?
)
