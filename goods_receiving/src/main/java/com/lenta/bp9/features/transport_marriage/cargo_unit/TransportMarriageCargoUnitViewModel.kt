package com.lenta.bp9.features.transport_marriage.cargo_unit

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.transport_marriage.ActItem
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskTransportMarriageInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.platform.requestCodeTypeBarCode
import com.lenta.bp9.platform.requestCodeTypeSap
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.ZmpUtzGrz26V001NetRequest
import com.lenta.bp9.requests.network.ZmpUtzGrz26V001Params
import com.lenta.bp9.requests.network.ZmpUtzGrz26V001Result
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequest
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequestParams
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.mobrun.plugin.api.HyperHive
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
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var scanInfoRequest: ScanInfoRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo

    val actSelectionsHelper = SelectionItemsHelper()
    val cargoUnitNumber: MutableLiveData<String> = MutableLiveData("")
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()
    val listAct:MutableLiveData<List<ActItem>> = MutableLiveData()
    private val isScan: MutableLiveData<Boolean> = MutableLiveData(false)
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
        launchUITryCatch {
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

    private fun searchProduct(materialNumber: String) {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            val foundTransportMarriageInfo = taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.findTransportMarriage(cargoUnitNumber.value ?: "", materialNumber)
            if (!foundTransportMarriageInfo.isNullOrEmpty()) {
                foundTransportMarriageInfo.findLast {
                    it.processingUnitNumber == cargoUnitNumber.value
                }?.let {
                    screenNavigator.openTransportMarriageGoodsInfoScreen(transportMarriageInfo = it)
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
        launchUITryCatch {
            repoInMemoryHolder.manufacturers.value = result.manufacturers
            result.processingUnits.map {processingUnitInfo ->
                val batchNumber = result.taskBatches.findLast {batchesInfo ->
                    batchesInfo.materialNumber == processingUnitInfo.materialNumber && batchesInfo.processingUnitNumber == processingUnitInfo.processingUnitNumber
                }?.batchNumber
                val foundTransportMarriageInfo = TaskTransportMarriageInfo.from(hyperHive, processingUnitInfo, cargoUnitNumber.value ?: "", batchNumber ?: "")
                taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.addTransportMarriage(foundTransportMarriageInfo)
            }
            taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.findTransportMarriage(cargoUnitNumber = cargoUnitNumber.value ?: "", materialNumber = result.processingUnits[0].materialNumber)?.
                    map {
                        screenNavigator.openTransportMarriageGoodsInfoScreen(transportMarriageInfo = it)
                        return@map
                    }
        }
    }

    fun onClickItemPosition(position: Int) {
        isScan.value = false
        listAct.value?.get(position)?.transportMarriage?.let {
            searchProduct(materialNumber = it.materialNumber)
        }
    }

    fun onScanResult(data: String) {
        launchUITryCatch {
            isScan.value = true
            screenNavigator.showProgressLoadingData(::handleFailure)

            scanInfoRequest(
                    ScanInfoRequestParams(
                            number = data,
                            tkNumber = sessionInfo.market ?: "",
                            fromScan = true,
                            isBarCode = true
                    )
            ).also {
                screenNavigator.hideProgress()
            }.either(::handleFailure) { scanInfoResult ->
                searchProduct(materialNumber = scanInfoResult.productInfo.materialNumber)
            }
        }

    }

    override fun onOkInSoftKeyboard(): Boolean {
        isScan.value = true
        when (eanCode.value?.length) {
            6 -> {
                eanCode.value?.let {
                    searchProduct(materialNumber = "000000000000$it")
                }
            }
            12 -> {
                screenNavigator.openSelectTypeCodeScreen(requestCodeTypeSap, requestCodeTypeBarCode)
            }
            else -> {
                eanCode.value?.let {
                    onScanResult(it)
                }
            }
        }
        return true
    }

    fun onResult(code: Int?) {
        when (code) {
            requestCodeTypeSap -> {
                eanCode.value?.let {
                    searchProduct(materialNumber = "000000000000${it.takeLast(6)}")
                }
            }
            requestCodeTypeBarCode -> {
                eanCode.value?.let {
                    onScanResult(it)
                }
            }
        }
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
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
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
        launchUITryCatch {
            repoInMemoryHolder.manufacturers.value = result.manufacturers
            result.processingUnits.map {processingUnitInfo ->
                val batchNumber = result.taskBatches.findLast {batchesInfo ->
                    batchesInfo.materialNumber == processingUnitInfo.materialNumber && batchesInfo.processingUnitNumber == processingUnitInfo.processingUnitNumber
                }?.batchNumber
                taskManager.getReceivingTask()?.
                        taskRepository?.
                        getTransportMarriage()?.
                        addTransportMarriage(TaskTransportMarriageInfo.from(hyperHive, processingUnitInfo, cargoUnitNumber.value ?: "", batchNumber ?: ""))
            }
            //если нажали Целиком, то MENGE для каждого товара сразу записываем все кол-во из поля quantityInvestments
            val transportMarriageInfoCurrent = taskManager.getReceivingTask()?.
                    taskRepository?.
                    getTransportMarriage()?.
                    getTransportMarriage()?.map {
                it.copy(quantity = it.quantityInvestments)
            }
            transportMarriageInfoCurrent?.let {
                taskManager.getReceivingTask()?.
                        taskRepository?.
                        getTransportMarriage()?.
                        updateTransportMarriage(it)
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
