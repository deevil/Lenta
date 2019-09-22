package com.lenta.bp14.features.price_check.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.check_price.ICheckPriceResult
import com.lenta.bp14.models.check_price.ICheckPriceTask
import com.lenta.bp14.models.data.GoodsListTab
import com.lenta.bp14.models.data.pojo.Good
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.analyseCode
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListPcViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var task: ICheckPriceTask


    val processedSelectionsHelper = SelectionItemsHelper()
    val searchSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)

    val correctedSelectedPage = selectedPage.map { getCorrectedPagePosition(it) }

    val taskName = MutableLiveData("")

    val numberField = MutableLiveData<String>("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val processingGoods = MutableLiveData<List<Good>>(listOf())

    private val funcUiAdapter = { list: List<ICheckPriceResult>? ->
        list?.reversed()?.mapIndexed { index, iCheckPriceResult ->
            CheckPriceResultUi(
                    matNr = iCheckPriceResult.matNr!!,
                    position = list.size - index,
                    name = "${iCheckPriceResult.matNr?.takeLast(6)} ${iCheckPriceResult.name}",
                    isPriceValid = iCheckPriceResult.isAllValid(),
                    isPrinted = iCheckPriceResult.isPrinted
            )
        }
    }

    val processedGoods by lazy {
        task.getCheckResults().map(funcUiAdapter)
    }
    val searchGoods by lazy {
        task.getCheckResultsForPrint().map(funcUiAdapter)
    }

    private val selectedItemOnCurrentTab: MutableLiveData<Boolean> = correctedSelectedPage
            .combineLatest(processedSelectionsHelper.selectedPositions)
            .combineLatest(searchSelectionsHelper.selectedPositions)
            .map {
                val tab = it?.first?.first?.toInt()
                val processedSelected = it?.first?.second?.isNotEmpty() == true
                val searchSelected = it?.second?.isNotEmpty() == true
                tab == GoodsListTab.PROCESSED.position && processedSelected || tab == GoodsListTab.SEARCH.position && searchSelected
            }

    val deleteButtonEnabled = selectedItemOnCurrentTab.map { it }
    val printButtonEnabled = selectedItemOnCurrentTab.map { it }
    val videoButtonEnabled by lazy {
        MutableLiveData(task.isFreeMode())
    }

    val saveButtonEnabled by lazy {
        processedGoods.map { it?.isNotEmpty() ?: false }
    }

    val deleteButtonVisibility = correctedSelectedPage.map { it != GoodsListTab.PROCESSING.position }
    val printButtonVisibility = correctedSelectedPage.map { it != GoodsListTab.PROCESSING.position }


    init {
        viewModelScope.launch {
            requestFocusToNumberField.value = true
            taskName.value = "${task.getTaskType().taskType} // ${task.getTaskName()}"

            /*processedGoods.value = List(10) {
                CheckPriceResultUi(
                        matNr = "4546465",
                        position = it + 1,
                        name = "4546465 nameeeee",
                        isPriceValid = true,
                        isPrinted = false
                )
            }

            searchGoods.value = List(10) {
                CheckPriceResultUi(
                        matNr = "4546465",
                        position = it + 1,
                        name = "4546465 nameeeee",
                        isPriceValid = true,
                        isPrinted = false
                )
            }*/

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
                    searchCode(qrCode = qrCode)
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }

    private fun searchCode(eanCode: String? = null, matNr: String? = null, qrCode: String? = null) {
        viewModelScope.launch {
            require((eanCode != null) xor (matNr != null) xor (qrCode != null)) {
                "only one param allowed. eanCode: $eanCode, matNr: $matNr, qrCode: $qrCode "
            }
            navigator.showProgressLoadingData()

            when {
                !eanCode.isNullOrBlank() -> task.getActualPriceByEan(eanCode)
                !matNr.isNullOrBlank() -> task.getActualPriceByMatNr(matNr)
                !qrCode.isNullOrBlank() -> task.checkPriceByQrCode(qrCode)
                else -> throw IllegalArgumentException()
            }.either(
                    fnL = {
                        navigator.openAlertScreen(it)
                    }
            ) {
                task.processingMatNumber = it.matNumber
                if (qrCode.isNullOrBlank()) {
                    navigator.openGoodInfoPcScreen()
                }
            }
            navigator.hideProgress()

        }
    }


    fun onClickSave() {

    }

    fun onClickDelete() {
        when (correctedSelectedPage.value) {
            1 -> processedSelectionsHelper
            2 -> searchSelectionsHelper
            else -> null
        }?.let { selectionHelper ->
            selectionHelper.selectedPositions.value?.apply {
                task.removeCheckResultsByMatNumbers(
                        if (selectionHelper === processedSelectionsHelper) {
                            processedGoods
                        } else {
                            searchGoods
                        }.value?.filterIndexed { index, _ ->
                            this.contains(index)
                        }?.map { it.matNr }?.toSet()
                                ?: emptySet()
                )
            }
            selectionHelper.clearPositions()
        }

    }

    fun onClickPrint() {

    }

    fun onClickVideo() {
        navigator.openScanPriceScreen()
    }

    fun onClickItemPosition(position: Int) {
        getMatNrByPosition(position)?.let { selectedMatNr ->
            task.processingMatNumber = selectedMatNr
            navigator.openGoodInfoPcScreen()
        }
    }

    private fun getMatNrByPosition(position: Int): String? {
        return when (correctedSelectedPage.value) {
            0 -> processingGoods.value?.map { it.material }
            1 -> processedGoods.value?.map { it.matNr }
            2 -> searchGoods.value?.map { it.matNr }
            else -> null
        }.let {
            it?.getOrNull(position)
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

    fun onScanResult(data: String) {
        checkCode(data)
    }
}


data class CheckPriceResultUi(
        val matNr: String,
        val position: Int,
        val name: String,
        val isPriceValid: Boolean?,
        val isPrinted: Boolean
)


