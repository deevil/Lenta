package com.lenta.bp9.features.transport_marriage

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import javax.inject.Inject

class TransportMarriageViewModel : CoreViewModel(), PageSelectionListener,
        OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val selectedPage = MutableLiveData(0)
    val listCargoUnits: MutableLiveData<List<ListCargoUnitsItem>> = MutableLiveData()
    val listAct: MutableLiveData<List<ListAct>> = MutableLiveData()
    val actSelectionsHelper = SelectionItemsHelper()
    val cargoUnitNumber: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToCargoUnit: MutableLiveData<Boolean> = MutableLiveData()

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onClickCancellation() {

    }

    fun onClickProcess() {

    }

    fun onClickItemPosition(position: Int) {
        /**val matnr: String? = if (selectedPage.value == 0) {
            listCounted.value?.get(position)?.productInfo?.materialNumber
        } else {
            listWithoutBarcode.value?.get(position)?.productInfo?.materialNumber
        }
        searchProductDelegate.searchCode(code = matnr ?: "", fromScan = false)*/
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchCargoUnit(cargoUnitNumber.value ?: "")
        return true
    }

    fun onScanResult(data: String) {
        searchCargoUnit(data)
    }

    private fun searchCargoUnit(data: String) {
        /**viewModelScope.launch {
            searchCargoUnitNumber.value = data
            val findCargoUnit = processCargoUnitsService.findCargoUnit(data)
            if (findCargoUnit == null) {
                screenNavigator.showProgressLoadingData()
                val params = GettingDataNewCargoUnitParameters(
                        taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
                        cargoUnitNumber = data
                )
                gettingDataNewCargoUnit(params).either(::handleFailure, ::handleSuccessNewCargoUnit)
                screenNavigator.hideProgress()
            } else {
                screenNavigator.openCargoUnitCardScreen(findCargoUnit)
            }
        }*/
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToCargoUnit.value = true
        cargoUnitNumber.value = cargoUnitNumber.value ?: "" + digit
    }

}

data class ListCargoUnitsItem(
        val number: Int,
        val name: String,
        val quantityWithUom: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}

data class ListAct(
        val number: Int,
        val name: String,
        val quantityWithUom: String,
        val even: Boolean
) : Evenable {
    override fun isEven() = even

}
