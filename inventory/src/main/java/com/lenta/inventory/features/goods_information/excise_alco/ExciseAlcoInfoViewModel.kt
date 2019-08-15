package com.lenta.inventory.features.goods_information.excise_alco

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.goods_list.SearchProductDelegate
import com.lenta.inventory.models.InfoStatus
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.ProcessExciseAlcoProductService
import com.lenta.inventory.models.task.TaskExciseStamp
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.AlcoCodeNetRequest
import com.lenta.inventory.requests.network.ExciseGoodsParams
import com.lenta.inventory.requests.network.ExciseGoodsRestInfo
import com.lenta.inventory.requests.network.ObtainingDataExciseGoodsNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class ExciseAlcoInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var obtainingDataExciseGoodsNetRequest: ObtainingDataExciseGoodsNetRequest

    @Inject
    lateinit var processServiceManager: IInventoryTaskManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var processExciseAlcoProductService: ProcessExciseAlcoProductService

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    private val scannedStampCode: MutableLiveData<String> = MutableLiveData()

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()

    val storePlaceNumber: MutableLiveData<String> = MutableLiveData()

    val isStorePlaceNumber: MutableLiveData<Boolean> = storePlaceNumber.map { it != "00" }

    val msgWrongProducType: MutableLiveData<String> = MutableLiveData()

    val spinList: MutableLiveData<List<String>> = MutableLiveData()

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(GoodsInfoCountType.QUANTITY.number)

    val suffix: MutableLiveData<String> = MutableLiveData()

    val textErrorUnknownStatus: MutableLiveData<String> = MutableLiveData()

    val titleProgressScreen: MutableLiveData<String> = MutableLiveData()

    private val manufacturers: MutableLiveData<List<Manufacturer>> = MutableLiveData()

    private val manufacturerCode: MutableLiveData<String> = MutableLiveData("")

    private val bottlingDate: MutableLiveData<String> = MutableLiveData("")

    val count: MutableLiveData<String> = MutableLiveData("")

    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    private val totalCount: MutableLiveData<Double> = countValue.map {
        processExciseAlcoProductService.getFactCount() ?: productInfo.value!!.factCount
    }

    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${productInfo.value!!.uom.name}" }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.combineLatest(totalCount).map {
        it!!.first != 0.0 && it.second > 0.0
    }

    val enabledRollbackButton: MutableLiveData<Boolean> = countValue.map { it ?: 0.0 > 0.0 }

    val enabledMissingButton: MutableLiveData<Boolean> = totalCount.map { it ?: 0.0 == 0.0 }

    init {
        viewModelScope.launch {
            suffix.value = productInfo.value?.uom?.name
            storePlaceNumber.value = productInfo.value!!.placeCode
            searchProductDelegate.init(viewModelScope = this@ExciseAlcoInfoViewModel::viewModelScope,
                    scanResultHandler = this@ExciseAlcoInfoViewModel::handleProductSearchResult,
                    storePlace = storePlaceNumber.value ?: "00")
            if (processExciseAlcoProductService.newProcessExciseAlcoProductService(productInfo.value!!) == null) {
                screenNavigator.goBack()
                screenNavigator.openAlertInfoScreen(message = msgWrongProducType.value!!)
            }
        }
    }

    private fun handleProductSearchResult(scanInfoResult: ScanInfoResult?): Boolean {
        enabledBtn()
        screenNavigator.goBack()
        return false
    }

    fun onResume() {
        count.value = processExciseAlcoProductService.getLastCountExciseStamp().countLastExciseStamp.let {
            if (it == 0) "" else it.toString()
        }
        selectedPosition.value = processExciseAlcoProductService.getLastCountExciseStamp().countType
    }

    fun onClickRollback() {
        val countLastExciseStamp = processExciseAlcoProductService.rollback()
        count.value = if (countLastExciseStamp.countLastExciseStamp == 0) "" else countLastExciseStamp.countLastExciseStamp.toString()
        selectedPosition.value = countLastExciseStamp.countType
    }

    fun onClickDetails() {
        screenNavigator.openGoodsDetailsStorageScreen(productInfo.value!!)
    }

    fun onClickMissing() {
        processExciseAlcoProductService.markMissing()
        screenNavigator.goBack()
    }

    fun onClickApply() {
        processExciseAlcoProductService.apply()
        screenNavigator.goBack()
    }

    fun onScanResult(data: String) {
        scannedStampCode.value = data
        when (data.length) {
            in 26..50 -> processBox(data)
            68 -> processPdf68(data)
            150 -> processPdf150(data)
            else -> searchProductDelegate.searchCode(code = data, fromScan = true)
        }
    }

    private fun processBox(boxNumber: String) {
        if (processExciseAlcoProductService.isTaskAlreadyHasExciseStampBox(boxNumber)) {
            screenNavigator.openAlertDoubleScanStamp()
            return
        }
        viewModelScope.launch {
            screenNavigator.showProgress(titleProgressScreen.value!!)
            obtainingDataExciseGoodsNetRequest(
                    ExciseGoodsParams(
                            werks = sessionInfo.market.orEmpty(),
                            materialNumber = productInfo.value!!.materialNumber,
                            materialNumberComp = "",
                            stampCode = "",
                            boxNumber = boxNumber,
                            manufacturerCode = "",
                            bottlingDate = "",
                            mode = "2",
                            codeEBP = "INV",
                            factCount = ""

                    )).either(::handleFailure, ::processBoxHandleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun processBoxHandleSuccess(exciseGoodsRestInfo: ExciseGoodsRestInfo) {
        if (exciseGoodsRestInfo.retCode != "0") {
            screenNavigator.openAlertInfoScreen(exciseGoodsRestInfo.errorTxt)
            return
        }

        when (exciseGoodsRestInfo.status) {
            InfoStatus.BoxFound.status -> {
                val boxStamps = exciseGoodsRestInfo.stampsBox.map { stampsBox ->
                    TaskExciseStamp(
                            materialNumber = productInfo.value!!.materialNumber,
                            code = stampsBox.exciseStampCode,
                            placeCode = productInfo.value!!.placeCode,
                            boxNumber = stampsBox.boxNumber
                    )
                }
                processExciseAlcoProductService.addCurrentExciseStamps(boxStamps)
                count.value = boxStamps.size.toString()
                selectedPosition.value = GoodsInfoCountType.VINTAGE.number
            }
            InfoStatus.BoxWithProblem.status -> screenNavigator.openAlertInfoScreen(exciseGoodsRestInfo.statusTxt)
            else -> screenNavigator.openAlertInfoScreen(textErrorUnknownStatus.value!!)
        }
    }

    private fun processPdf68(stampCode: String) {
        if (processExciseAlcoProductService.isTaskAlreadyHasExciseStamp(stampCode)) {
            screenNavigator.openAlertDoubleScanStamp()
        } else {
            checkExciseStampByCode(stampCode)
        }
    }

    private fun processPdf150(stampCode: String) {
        if (processExciseAlcoProductService.isTaskAlreadyHasExciseStamp(stampCode)) {
            screenNavigator.openAlertDoubleScanStamp()
            return
        }
        viewModelScope.launch {
            screenNavigator.showProgress(titleProgressScreen.value!!)
            obtainingDataExciseGoodsNetRequest(
                    ExciseGoodsParams(
                            werks = sessionInfo.market.orEmpty(),
                            materialNumber = productInfo.value!!.materialNumber,
                            materialNumberComp = "",
                            stampCode = stampCode,
                            boxNumber = "",
                            manufacturerCode = "",
                            bottlingDate = "",
                            mode = "1",
                            codeEBP = "INV",
                            factCount = ""

                    )).either(::handleFailure, ::processPdf150HandleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun processPdf150HandleSuccess(exciseGoodsRestInfo: ExciseGoodsRestInfo) {
        if (exciseGoodsRestInfo.retCode != "0") {
            screenNavigator.openAlertInfoScreen(exciseGoodsRestInfo.errorTxt)
            return
        }

        when (exciseGoodsRestInfo.status) {
            InfoStatus.StampFound.status, InfoStatus.StampOverload.status -> {
                processExciseAlcoProductService.addCurrentExciseStamp(
                        TaskExciseStamp(
                                materialNumber = productInfo.value!!.materialNumber,
                                code = scannedStampCode.value!!,
                                placeCode = productInfo.value!!.placeCode
                        )
                )
                count.value = "1"
                selectedPosition.value = GoodsInfoCountType.VINTAGE.number
                if (exciseGoodsRestInfo.status == InfoStatus.StampOverload.status) {
                    screenNavigator.openAlertStampOverload(message = exciseGoodsRestInfo.statusTxt) {}
                }
            }
            InfoStatus.StampOfOtherProduct.status -> {
                screenNavigator.openAlertInfoScreen(exciseGoodsRestInfo.statusTxt)
            }
            InfoStatus.StampNotFound.status -> {
                manufacturers.value = exciseGoodsRestInfo.manufacturers
                screenNavigator.openPartySignsScreen("${productInfo.value!!.getMaterialLastSix()} ${productInfo.value!!.description}", exciseGoodsRestInfo.manufacturers.map { it.name }, 150)
            }
            else -> screenNavigator.openAlertInfoScreen(textErrorUnknownStatus.value!!)
        }
    }

    fun onPartySignsResult(_manufacturerCode: String, _bottlingDate: String) {
        manufacturerCode.value = manufacturers.value!![_manufacturerCode.toInt()].code
        bottlingDate.value = _bottlingDate.substring(6, 10) + _bottlingDate.substring(3, 5) + _bottlingDate.substring(0, 2)
        viewModelScope.launch {
            screenNavigator.showProgress(titleProgressScreen.value!!)
            obtainingDataExciseGoodsNetRequest(
                    ExciseGoodsParams(
                            werks = sessionInfo.market.orEmpty(),
                            materialNumber = productInfo.value!!.materialNumber,
                            materialNumberComp = "",
                            stampCode = scannedStampCode.value!!,
                            boxNumber = "",
                            manufacturerCode = manufacturerCode.value!!,
                            bottlingDate = bottlingDate.value!!,
                            mode = "3",
                            codeEBP = "INV",
                            factCount = ""

                    )).either(::handleFailure, ::partySignsHandleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun partySignsHandleSuccess(exciseGoodsRestInfo: ExciseGoodsRestInfo) {
        if (exciseGoodsRestInfo.retCode != "0") {
            screenNavigator.openAlertInfoScreen(exciseGoodsRestInfo.errorTxt)
            return
        }

        when (exciseGoodsRestInfo.status) {
            InfoStatus.BatchFound.status, InfoStatus.BatchNotFound.status -> {
                processExciseAlcoProductService.addCurrentExciseStamp(
                        TaskExciseStamp(
                                materialNumber = productInfo.value!!.materialNumber,
                                code = scannedStampCode.value!!,
                                placeCode = productInfo.value!!.placeCode,
                                manufacturerCode = manufacturerCode.value!!,
                                bottlingDate = bottlingDate.value!!
                        )
                )
                count.value = "1"
                selectedPosition.value = GoodsInfoCountType.VINTAGE.number
                if (exciseGoodsRestInfo.status == InfoStatus.BatchNotFound.status) {
                    screenNavigator.openAlertStampOverload(exciseGoodsRestInfo.statusTxt) {}
                }
            }
            else -> screenNavigator.openAlertInfoScreen(textErrorUnknownStatus.value!!)
        }
    }

    private fun checkExciseStampByCode(stampCode: String) {
        viewModelScope.launch {
            screenNavigator.showProgress(titleProgressScreen.value!!)
            obtainingDataExciseGoodsNetRequest(
                    ExciseGoodsParams(
                            werks = sessionInfo.market.orEmpty(),
                            materialNumber = productInfo.value!!.materialNumber,
                            materialNumberComp = "",
                            stampCode = stampCode,
                            boxNumber = "",
                            manufacturerCode = "",
                            bottlingDate = "",
                            mode = "1",
                            codeEBP = "INV",
                            factCount = ""
                    )).either(::handleFailure, ::checkExciseStampByCodeHandleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun checkExciseStampByCodeHandleSuccess(exciseGoodsRestInfo: ExciseGoodsRestInfo) {
        if (exciseGoodsRestInfo.retCode != "0") {
            screenNavigator.openAlertInfoScreen(exciseGoodsRestInfo.errorTxt)
            return
        }

        when (exciseGoodsRestInfo.status) {
            InfoStatus.StampFound.status, InfoStatus.StampOverload.status -> {
                processExciseAlcoProductService.add(1,
                        TaskExciseStamp(
                                materialNumber = productInfo.value!!.materialNumber,
                                code = scannedStampCode.value!!,
                                placeCode = productInfo.value!!.placeCode
                        )
                )
                count.value = "1"
                selectedPosition.value = GoodsInfoCountType.PARTLY.number
                if (exciseGoodsRestInfo.status == InfoStatus.StampOverload.status) {
                    screenNavigator.openAlertStampOverload(message = exciseGoodsRestInfo.statusTxt) {}
                }
            }
            InfoStatus.StampOfOtherProduct.status -> {
                screenNavigator.openAlertInfoScreen(exciseGoodsRestInfo.statusTxt)
            }
            InfoStatus.StampNotFound.status -> {
                manufacturers.value = exciseGoodsRestInfo.manufacturers
                screenNavigator.openPartySignsScreen("${productInfo.value!!.getMaterialLastSix()} ${productInfo.value!!.description}", exciseGoodsRestInfo.manufacturers.map { it.name }, 68)
            }
            else -> screenNavigator.openAlertInfoScreen(textErrorUnknownStatus.value!!)
        }
    }

    fun onPartySignsStamp68Result(_manufacturerCode: String, _bottlingDate: String) {
        manufacturerCode.value = manufacturers.value!![_manufacturerCode.toInt()].code
        bottlingDate.value = _bottlingDate.substring(6, 10) + _bottlingDate.substring(3, 5) + _bottlingDate.substring(0, 2)
        viewModelScope.launch {
            screenNavigator.showProgress(titleProgressScreen.value!!)
            obtainingDataExciseGoodsNetRequest(
                    ExciseGoodsParams(
                            werks = sessionInfo.market.orEmpty(),
                            materialNumber = productInfo.value!!.materialNumber,
                            materialNumberComp = "",
                            stampCode = scannedStampCode.value!!,
                            boxNumber = "",
                            manufacturerCode = manufacturerCode.value!!,
                            bottlingDate = bottlingDate.value!!,
                            mode = "3",
                            codeEBP = "INV",
                            factCount = ""

                    )).either(::handleFailure, ::partySignsStamp68HandleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun partySignsStamp68HandleSuccess(exciseGoodsRestInfo: ExciseGoodsRestInfo) {
        if (exciseGoodsRestInfo.retCode != "0") {
            screenNavigator.openAlertInfoScreen(exciseGoodsRestInfo.errorTxt)
            return
        }

        when (exciseGoodsRestInfo.status) {
            InfoStatus.BatchFound.status, InfoStatus.BatchNotFound.status -> {
                processExciseAlcoProductService.add(1,
                        TaskExciseStamp(
                                materialNumber = productInfo.value!!.materialNumber,
                                code = scannedStampCode.value!!,
                                placeCode = productInfo.value!!.placeCode,
                                manufacturerCode = manufacturerCode.value!!,
                                bottlingDate = bottlingDate.value!!
                        )
                )
                count.value = "1"
                selectedPosition.value = GoodsInfoCountType.PARTLY.number
                if (exciseGoodsRestInfo.status == InfoStatus.BatchNotFound.status) {
                    screenNavigator.openAlertStampOverload(exciseGoodsRestInfo.statusTxt) {}
                }
            }
            else -> screenNavigator.openAlertInfoScreen(textErrorUnknownStatus.value!!)
        }
    }

    private fun enabledBtn() {
        if (enabledApplyButton.value!!) {
            processExciseAlcoProductService.apply()
        } else {
            if (enabledMissingButton.value!!) {
                processExciseAlcoProductService.markMissing()
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    override fun onClickPosition(position: Int) {
        return
    }

    fun onBackPressed() {
        if (enabledRollbackButton.value!!) {
            processExciseAlcoProductService.rollback()
            processExciseAlcoProductService.apply()
        }
        processExciseAlcoProductService.discard()
    }
}
