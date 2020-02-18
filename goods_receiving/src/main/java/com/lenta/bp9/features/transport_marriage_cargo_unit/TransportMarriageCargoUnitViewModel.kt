package com.lenta.bp9.features.transport_marriage_cargo_unit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.features.transport_marriage.ActItem
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskTransportMarriageInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.ZmpUtzGrz26V001NetRequest
import com.lenta.bp9.requests.network.ZmpUtzGrz26V001Params
import com.lenta.bp9.requests.network.ZmpUtzGrz26V001Result
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class TransportMarriageCargoUnitViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var zmpUtzGrz26V001NetRequest: ZmpUtzGrz26V001NetRequest
    @Inject
    lateinit var hyperHive: HyperHive

    val actSelectionsHelper = SelectionItemsHelper()
    val cargoUnitNumber: MutableLiveData<String> = MutableLiveData("")
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()
    val listAct:MutableLiveData<List<ActItem>> = MutableLiveData()
    private val origTransportMarriage: MutableLiveData<List<TaskTransportMarriageInfo>?> = MutableLiveData()
    val deleteButtonEnabled: MutableLiveData<Boolean> = actSelectionsHelper.selectedPositions.map {
        it?.isNotEmpty() ?: false
    }
    val applyButtonEnabled: MutableLiveData<Boolean> = listAct.map {
        discrepancies()
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    init {
        viewModelScope.launch {
            origTransportMarriage.value = taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.getTransportMarriage()?.filter {itFilter ->
                itFilter.cargoUnitNumber == cargoUnitNumber.value
            }?.map {
                it.copy()
            }
        }
    }

    fun onResume() {
        updateData()
    }

    private fun updateData() {
        listAct.postValue(
                taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.getTransportMarriage()?.filter {
                    it.cargoUnitNumber == cargoUnitNumber.value
                }?.mapIndexed { index, transportMarriage ->
                    ActItem(
                            number = index + 1,
                            name = "${transportMarriage.getMaterialLastSix()} ${transportMarriage.materialName}",
                            processingUnitName = "ЕО-${transportMarriage.processingUnitNumber}",
                            quantityWithUom = "- ${transportMarriage.quantity.toStringFormatted()} ${transportMarriage.uom.name}",
                            transportMarriage = transportMarriage,
                            even = index % 2 == 0
                    )
                }?.reversed()
        )
        actSelectionsHelper.clearPositions()
    }

    private fun searchProduct(materialNumber: String, processingUnitNumber: String?) {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            val foundTransportMarriageInfo = taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.findTransportMarriage(cargoUnitNumber.value ?: "", materialNumber)
            if (foundTransportMarriageInfo != null) {
                foundTransportMarriageInfo.findLast {
                    it.processingUnitNumber == processingUnitNumber
                }?.let {
                    screenNavigator.openTransportMarriageGoodsInfoScreen(it)
                }
            } else {
                taskManager.getReceivingTask()?.let { task ->
                    val params = ZmpUtzGrz26V001Params(
                            taskNumber = task.taskHeader.taskNumber,
                            cargoUnitNumber = cargoUnitNumber.value ?: "",
                            materialNumber = materialNumber
                    )
                    zmpUtzGrz26V001NetRequest(params).either(::handleFailure, ::handleSuccessSearchProduct)
                }
            }
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccessSearchProduct(result: ZmpUtzGrz26V001Result) {
        viewModelScope.launch {
            result.processingUnits.map {processingUnitInfo ->
                val batchNumber = result.taskBatches.findLast {batchesInfo ->
                    batchesInfo.materialNumber == processingUnitInfo.materialNumber && batchesInfo.processingUnitNumber == processingUnitInfo.processingUnitNumber
                }?.batchNumber
                val foundTransportMarriageInfo = TaskTransportMarriageInfo.from(hyperHive, processingUnitInfo, cargoUnitNumber.value ?: "", batchNumber ?: "")
                taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.addTransportMarriage(foundTransportMarriageInfo)
            }
            taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.findTransportMarriage(cargoUnitNumber = cargoUnitNumber.value ?: "", materialNumber = result.processingUnits[0].materialNumber)?.
                    map {
                        screenNavigator.openTransportMarriageGoodsInfoScreen(it)
                        return@map
                    }
        }
    }

    fun onClickItemPosition(position: Int) {
        listAct.value?.get(position)?.transportMarriage?.let {
            searchProduct(materialNumber = it.materialNumber, processingUnitNumber = it.processingUnitNumber)
        }
    }

    fun onScanResult(data: String) {
        searchProduct(materialNumber = data, processingUnitNumber = null)
    }

    override fun onOkInSoftKeyboard(): Boolean {
        eanCode.value?.let {
            searchProduct(materialNumber = it, processingUnitNumber = null)
        }
        return true
    }

    private fun discrepancies() : Boolean {
        val newTransportMarriage = taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.getTransportMarriage()?.filter {
            it.cargoUnitNumber == cargoUnitNumber.value
        }

        return newTransportMarriage?.filter {newTrMar ->
            origTransportMarriage.value?.findLast {
                newTrMar.materialNumber == it.materialNumber && newTrMar.cargoUnitNumber == it.cargoUnitNumber
            } != null
        }?.size != origTransportMarriage.value?.size || ((newTransportMarriage?.size ?: 0) > 0 && origTransportMarriage.value?.size == 0)
    }

    fun onClickCancellation() {
        if ( discrepancies()) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.clear()
                        origTransportMarriage.value?.map {
                            taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.addTransportMarriage(it)
                        }
                        screenNavigator.goBack()
                    }
            )
        } else {
            screenNavigator.goBack()
        }
    }

    fun onClickEntirely() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            taskManager.getReceivingTask()?.let { task ->
                val params = ZmpUtzGrz26V001Params(
                        taskNumber = task.taskHeader.taskNumber,
                        cargoUnitNumber = cargoUnitNumber.value ?: "",
                        materialNumber = ""
                )
                zmpUtzGrz26V001NetRequest(params).either(::handleFailure, ::handleSuccessEntirely)
            }
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccessEntirely(result: ZmpUtzGrz26V001Result) {
        viewModelScope.launch {
            result.processingUnits.map {processingUnitInfo ->
                val batchNumber = result.taskBatches.findLast {batchesInfo ->
                    batchesInfo.materialNumber == processingUnitInfo.materialNumber && batchesInfo.processingUnitNumber == processingUnitInfo.processingUnitNumber
                }?.batchNumber
                taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.addTransportMarriage(TaskTransportMarriageInfo.from(hyperHive, processingUnitInfo, cargoUnitNumber.value ?: "", batchNumber ?: ""))
            }
            updateData()
        }
    }

    fun onClickDelete() {
        actSelectionsHelper.selectedPositions.value?.map { position ->
            listAct.value?.get(position)?.transportMarriage?.let {
                taskManager.
                        getReceivingTask()?.
                        taskRepository?.
                        getTransportMarriage()?.
                        deleteTransportMarriage(it)
            }
        }
        updateData()
    }

    fun onClickApply() {
        screenNavigator.goBack()
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToEan.value = true
        eanCode.value = eanCode.value ?: "" + digit
    }

}
