package com.lenta.bp14.features.work_list.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.data.GoodsListTab
import com.lenta.bp14.models.getTaskName
import com.lenta.bp14.models.work_list.Good
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.analyseCode
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListWlViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var task: WorkListTask


    val processedSelectionsHelper = SelectionItemsHelper()
    val searchSelectionsHelper = SelectionItemsHelper()

    val selectedPage = MutableLiveData(0)
    val correctedSelectedPage = selectedPage.map { getCorrectedPagePosition(it) }

    val taskName = MutableLiveData("")

    val numberField: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToNumberField: MutableLiveData<Boolean> = MutableLiveData()

    val processingGoods = MutableLiveData<List<Good>>()
    val searchGoods = MutableLiveData<List<Good>>()

    val processedGoods: MutableLiveData<List<ProcessedListUi>> by lazy {
        task.processed.map { list: MutableList<Good>? ->
            list?.mapIndexed { index, good ->
                ProcessedListUi(
                        position = (index + 1).toString(),
                        name = good.getFormattedMaterialWithName(),
                        quantity = "11111"
                )
            }
        }
    }

    private val selectedItemOnCurrentTab: MutableLiveData<Boolean> = selectedPage
            .combineLatest(processedSelectionsHelper.selectedPositions)
            .map {
                val tab = it?.first?.toInt()
                val processedSelected = it?.second?.isNotEmpty() == true
                tab == GoodsListTab.PROCESSED.position && processedSelected || tab == GoodsListTab.SEARCH.position
            }

    val deleteButtonEnabled = selectedItemOnCurrentTab.map { it }
    val saveButtonEnabled = processingGoods.map { it?.isNotEmpty() ?: false }

    val thirdButtonVisibility = selectedPage.map { it != GoodsListTab.PROCESSING.position }

    init {
        viewModelScope.launch {
            requestFocusToNumberField.value = true
            taskName.value = "${task.getTaskType().taskType} // ${task.getTaskName()}"
        }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickSave() {

    }

    fun onClickDelete() {

    }

    override fun onOkInSoftKeyboard(): Boolean {
        checkEnteredNumber(numberField.value ?: "")
        return true
    }

    private fun checkEnteredNumber(number: String) {
        analyseCode(
                code = number,
                funcForEan = { eanCode ->
                    addGoodByEan(eanCode)
                },
                funcForMatNr = { matNr ->
                    addGoodByEan(matNr)
                },
                funcForPriceQrCode = { qrCode ->
                    //addGoodByEan(matNr)
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }

    private fun addGoodByEan(ean: String) {
        Logg.d { "Entered EAN: $ean" }
        viewModelScope.launch {
            if (task.addGoodByEan(ean)) {
                navigator.openGoodInfoWlScreen()
            }
        }
    }

    private fun addGoodByMaterial(material: String) {
        Logg.d { "Entered MATERIAL: $material" }
        viewModelScope.launch {
            if (task.addGoodByEan(material)) {
                navigator.openGoodInfoWlScreen()
            }
        }
    }

    private fun addGoodByMatcode(matcode: String) {
        Logg.d { "Entered MATCODE: $matcode" }
        viewModelScope.launch {

        }
    }

    fun onClickFilter() {
        navigator.openSearchFilterWlScreen()
    }

    fun onClickItemPosition(position: Int) {
        navigator.openGoodInfoWlScreen()
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

    }

}

data class ProcessedListUi(
        val position: String,
        val name: String,
        val quantity: String
)
