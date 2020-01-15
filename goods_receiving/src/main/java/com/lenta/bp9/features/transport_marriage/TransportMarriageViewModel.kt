package com.lenta.bp9.features.transport_marriage

import android.content.Context
import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskCargoUnitInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.DeclareTransportDefectNetRequest
import com.lenta.bp9.requests.network.DeclareTransportDefectParams
import com.lenta.bp9.requests.network.DeclareTransportDefectRestInfo
import com.lenta.bp9.requests.network.DirectSupplierStartRecountParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TransportMarriageViewModel : CoreViewModel(), PageSelectionListener,
        OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var declareTransportDefectNetRequest: DeclareTransportDefectNetRequest

    val selectedPage = MutableLiveData(0)
    val listCargoUnits: MutableLiveData<List<ListCargoUnitsItem>> = MutableLiveData()
    val listAct: MutableLiveData<List<ListAct>> = MutableLiveData()
    val actSelectionsHelper = SelectionItemsHelper()
    val cargoUnitNumber: MutableLiveData<String> = MutableLiveData("")
    val requestFocusToCargoUnit: MutableLiveData<Boolean> = MutableLiveData()

    val deleteButtonEnabled: MutableLiveData<Boolean> = actSelectionsHelper.selectedPositions.map {
        it?.isNotEmpty() ?: false
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    init {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            taskManager.getReceivingTask()?.let { task ->
                val params = DeclareTransportDefectParams(
                        taskNumber = task.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personnelNumber = sessionInfo.personnelNumber ?: ""
                )
                declareTransportDefectNetRequest(params).either(::handleFailure, ::handleSuccess)
            }
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(result: DeclareTransportDefectRestInfo) {
        taskManager.getReceivingTask()?.taskRepository?.getCargoUnits()?.updateCargoUnits(result.cargoUnits.map { TaskCargoUnitInfo.from(it) })
        updateData()
    }

    private fun updateData() {
        listCargoUnits.postValue(
                taskManager.getReceivingTask()?.taskRepository?.getCargoUnits()?.getCargoUnits()?.mapIndexed { index, taskCargoUnitInfo ->
                    ListCargoUnitsItem(
                            number = index + 1,
                            cargoUnitNumber = taskCargoUnitInfo.cargoUnitNumber,
                            quantityPositions = taskCargoUnitInfo.quantityPositions.toString(),
                            even = index % 2 == 0
                    )
                }?.reversed()
        )
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

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

}

data class ListCargoUnitsItem(
        val number: Int,
        val cargoUnitNumber: String,
        val quantityPositions: String,
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
