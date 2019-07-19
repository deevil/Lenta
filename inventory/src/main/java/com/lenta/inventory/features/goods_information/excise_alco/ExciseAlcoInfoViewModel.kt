package com.lenta.inventory.features.goods_information.excise_alco

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.models.InfoStatus
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.ProcessExciseAlcoProductService
import com.lenta.inventory.models.task.TaskExciseStamp
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.Manufacturer
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
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
    lateinit var alcoCodeNetRequest: AlcoCodeNetRequest

    @Inject
    lateinit var obtainingDataExciseGoodsNetRequest: ObtainingDataExciseGoodsNetRequest

    @Inject
    lateinit var processServiceManager: IInventoryTaskManager

    @Inject
    lateinit var sessionInfo: ISessionInfo

    private val processExciseAlcoProductService: ProcessExciseAlcoProductService by lazy {
        processServiceManager.getInventoryTask()!!.processExciseAlcoProduct(productInfo.value!!)!!
    }

    val scannedStampCode: MutableLiveData<String> = MutableLiveData()

    val isEtCountValue: MutableLiveData<Boolean> = MutableLiveData(false)

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()

    val storePlaceNumber: MutableLiveData<String> = MutableLiveData()

    val isStorePlaceNumber: MutableLiveData<Boolean> = storePlaceNumber.map { it != "00" }

    val spinList: MutableLiveData<List<String>> = MutableLiveData()

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(GoodsInfoCountType.QUANTITY.number)

    val suffix: MutableLiveData<String> = MutableLiveData()

    val textErrorUnknownStatus: MutableLiveData<String> = MutableLiveData()

    val titleProgressScreen: MutableLiveData<String> = MutableLiveData()

    private val manufacturers: MutableLiveData<List<Manufacturer>> = MutableLiveData()

    private val manufacturerCode: MutableLiveData<String> = MutableLiveData("")

    private val bottlingDate: MutableLiveData<String> = MutableLiveData("")

    val count: MutableLiveData<String> = MutableLiveData("")

    private val countValue: MutableLiveData<Double> = count.map{ it?.toDoubleOrNull() ?: 0.0 }

    private val totalCount: MutableLiveData<Double> by lazy {
        countValue.map { processExciseAlcoProductService.getFactCount() }
    }

    val totalCountWithUom: MutableLiveData<String> by lazy {
        totalCount.map { "${it.toStringFormatted()} ${productInfo.value!!.uom.name}" }
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.combineLatest(totalCount).map {
        it!!.first != 0.0 && it.second >= 0.0
    }

    val enabledRollbackButton: MutableLiveData<Boolean> = countValue.map { it ?: 0.0 > 0.0 && isEtCountValue.value == false}

    val enabledMissingButton: MutableLiveData<Boolean> = totalCount.map { it ?: 0.0 <= 0.0 }

    val isUpdateDate: MutableLiveData<Boolean> = MutableLiveData(false)

    init {
        viewModelScope.launch {
            suffix.value = productInfo.value?.uom?.name
            storePlaceNumber.value = productInfo.value!!.placeCode
        }
    }

    fun onResume(){
        if (isUpdateDate.value!!) {
            processExciseAlcoProductService.updateCurrentData()
            processExciseAlcoProductService.getLastCountExciseStamp().countLastExciseStamp.let {
                if (it == 0) "" else it
            }
            selectedPosition.value = processExciseAlcoProductService.getLastCountExciseStamp().countType
            isUpdateDate.value = false
        }
    }

    fun onClickRollback() {
        val countLastExciseStamp = processExciseAlcoProductService.rollback()
        count.value = if (countLastExciseStamp.countLastExciseStamp == 0) "" else countLastExciseStamp.countLastExciseStamp.toString()
        selectedPosition.value = countLastExciseStamp.countType
    }

    fun onClickDetails() {
        isUpdateDate.value = true
        screenNavigator.openGoodsDetailsStorageScreen(productInfo.value!!)
    }

    fun onClickMissing() {
        processExciseAlcoProductService.markMissing()
        screenNavigator.goBack()
    }

    fun onClickApply() {
        if (isEtCountValue.value!!){
            processExciseAlcoProductService.add(count.value!!.toInt(),
                    TaskExciseStamp(
                            materialNumber = productInfo.value!!.materialNumber,
                            code = scannedStampCode.value!!,
                            placeCode = productInfo.value!!.placeCode
                    )
            )
        }
        processExciseAlcoProductService.apply()
        screenNavigator.goBack()
    }

    fun onScanResult(data: String) {
        scannedStampCode.value = data
        when (data.length) {
            in 26..50 -> processBox(data)
            68 -> processPdf68(data)
            150 -> processPdf150(data)
            else -> processItemByBarcode(data)
        }
    }

    private fun processBox(boxNumber: String){
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

                    )).
                    either(::handleFailure, ::processBoxHandleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun processBoxHandleSuccess(exciseGoodsRestInfo: ExciseGoodsRestInfo){
        if (exciseGoodsRestInfo.retCode != "0") {
            screenNavigator.openAlertScreen(exciseGoodsRestInfo.errorTxt, pageNumber = "98")
            return
        }

        when (exciseGoodsRestInfo.status.toInt()) {
            InfoStatus.BoxFound.status -> {
                val boxStamps = exciseGoodsRestInfo.stampsBox.map {stampsBox ->
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
            InfoStatus.BoxWithProblem.status -> screenNavigator.openAlertScreen(exciseGoodsRestInfo.statusTxt, pageNumber = "98")
            else -> screenNavigator.openAlertScreen(textErrorUnknownStatus.value!!, pageNumber = "98")
        }
    }

    private fun processPdf68(stampCode: String){
        if (processExciseAlcoProductService.isTaskAlreadyHasExciseStamp(stampCode)) {
            screenNavigator.openAlertDoubleScanStamp()
            return
        } else{
            if (processExciseAlcoProductService.isLinkingOldStamps()) {
                checkExciseStampByCode(stampCode)
            } else checkExciseStampByAlcCcode(stampCode)
        }
    }

    private fun processPdf150(stampCode: String){
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

                    )).
                    either(::handleFailure, ::processPdf150HandleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun processPdf150HandleSuccess(exciseGoodsRestInfo: ExciseGoodsRestInfo){
        if (exciseGoodsRestInfo.retCode != "0") {
            screenNavigator.openAlertScreen(exciseGoodsRestInfo.errorTxt, pageNumber = "98")
            return
        }

        when (exciseGoodsRestInfo.status.toInt()) {
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
                if (exciseGoodsRestInfo.status.toInt() == InfoStatus.StampOverload.status) {
                    isUpdateDate.value = true
                    screenNavigator.openAlertScreen(exciseGoodsRestInfo.statusTxt, pageNumber = "98")
                }
            }
            InfoStatus.StampOfOtherProduct.status -> {
                screenNavigator.openAlertScreen(exciseGoodsRestInfo.statusTxt, pageNumber = "98")
            }
            InfoStatus.StampNotFound.status -> {
                manufacturers.value = exciseGoodsRestInfo.manufacturers
                screenNavigator.openPartySignsScreen("${productInfo.value!!.getMaterialLastSix()} ${productInfo.value!!.description}", exciseGoodsRestInfo.manufacturers.map { it.name }, 150)
            }
            else -> screenNavigator.openAlertScreen(textErrorUnknownStatus.value!!, pageNumber = "98")
        }
    }

    fun onPartySignsResult(bundle: Bundle) {
        manufacturerCode.value = manufacturers.value!![bundle.getString("manufacturerCode").toInt()].code
        bottlingDate.value = bundle.getString("bottlingDate").substring(6,10)+bundle.getString("bottlingDate").substring(3,5)+bundle.getString("bottlingDate").substring(0,2)
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

                    )).
                    either(::handleFailure, ::partySignsHandleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun partySignsHandleSuccess(exciseGoodsRestInfo: ExciseGoodsRestInfo){
        Logg.d { "partySignsHandleSuccess $exciseGoodsRestInfo" }

        if (exciseGoodsRestInfo.retCode != "0") {
            screenNavigator.openAlertScreen(exciseGoodsRestInfo.errorTxt, pageNumber = "98")
            return
        }

        when (exciseGoodsRestInfo.status.toInt()) {
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
                if (exciseGoodsRestInfo.status.toInt() == InfoStatus.BatchNotFound.status) {

                    isUpdateDate.value = true
                    screenNavigator.openAlertScreen(exciseGoodsRestInfo.statusTxt, pageNumber = "98")
                }
            }
            else -> screenNavigator.openAlertScreen(textErrorUnknownStatus.value!!, pageNumber = "98")
        }
    }

    private fun checkExciseStampByCode(stampCode: String){
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
                    )).
                    either(::handleFailure, ::checkExciseStampByCodeHandleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun checkExciseStampByCodeHandleSuccess(exciseGoodsRestInfo: ExciseGoodsRestInfo){
        if (exciseGoodsRestInfo.retCode != "0") {
            screenNavigator.openAlertScreen(exciseGoodsRestInfo.errorTxt, pageNumber = "98")
            return
        }

        when (exciseGoodsRestInfo.status.toInt()) {
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
                if (exciseGoodsRestInfo.status.toInt() == InfoStatus.StampOverload.status) {

                    isUpdateDate.value = true
                    screenNavigator.openAlertScreen(exciseGoodsRestInfo.statusTxt, pageNumber = "98")
                }
            }
            InfoStatus.StampOfOtherProduct.status -> {
                screenNavigator.openAlertScreen(exciseGoodsRestInfo.statusTxt, pageNumber = "98")
            }
            InfoStatus.StampNotFound.status -> {
                manufacturers.value = exciseGoodsRestInfo.manufacturers
                screenNavigator.openPartySignsScreen("${productInfo.value!!.getMaterialLastSix()} ${productInfo.value!!.description}", exciseGoodsRestInfo.manufacturers.map { it.name }, 68)
            }
            else -> screenNavigator.openAlertScreen(textErrorUnknownStatus.value!!, pageNumber = "98")
        }
    }

    fun onPartySignsStamp68Result(bundle: Bundle) {
        manufacturerCode.value = manufacturers.value!![bundle.getString("manufacturerCode").toInt()].code
        bottlingDate.value = bundle.getString("bottlingDate").substring(6,10)+bundle.getString("bottlingDate").substring(3,5)+bundle.getString("bottlingDate").substring(0,2)
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

                    )).
                    either(::handleFailure, ::partySignsStamp68HandleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun partySignsStamp68HandleSuccess(exciseGoodsRestInfo: ExciseGoodsRestInfo){
        Logg.d { "partySignsHandleSuccess $exciseGoodsRestInfo" }

        if (exciseGoodsRestInfo.retCode != "0") {
            screenNavigator.openAlertScreen(exciseGoodsRestInfo.errorTxt, pageNumber = "98")
            return
        }

        when (exciseGoodsRestInfo.status.toInt()) {
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
                if (exciseGoodsRestInfo.status.toInt() == InfoStatus.BatchNotFound.status) {

                    isUpdateDate.value = true
                    screenNavigator.openAlertScreen(exciseGoodsRestInfo.statusTxt, pageNumber = "98")
                }
            }
            else -> screenNavigator.openAlertScreen(textErrorUnknownStatus.value!!, pageNumber = "98")
        }
    }

    private fun checkExciseStampByAlcCcode(stampCode: String){
        if (productInfo.value!!.isExcOld){
            isEtCountValue.value = true
        }
        else {
            viewModelScope.launch {
                screenNavigator.showProgress(titleProgressScreen.value!!)
                alcoCodeNetRequest(null).either(::handleFailure, ::alcoCodeHandleSuccess)
                screenNavigator.hideProgress()
            }
        }
    }

    fun alcoCodeHandleSuccess(alcoCodeRestInfo: List<AlcoCodeRestInfo>){
        alcoCodeRestInfo.filterIndexed { index, alcoCodeRestInfo ->
            (alcoCodeRestInfo.data[index][2] == Base36ToBase10(scannedStampCode.value!!.substring(7,19)).padStart(19,'0') &&
                alcoCodeRestInfo.data[index][1].substring(alcoCodeRestInfo.data[index][1].length - 6) == productInfo.value!!.materialNumber) ||
                    (alcoCodeRestInfo.data[index][2] == Base36ToBase10(scannedStampCode.value!!.substring(7,19)).padStart(20,'0') &&
                            alcoCodeRestInfo.data[index][1].substring(alcoCodeRestInfo.data[index][1].length - 6) == productInfo.value!!.materialNumber)
        }.isNotEmpty().let {
            if (it) {
                processExciseAlcoProductService.add(1,
                                                    TaskExciseStamp(
                                                        materialNumber = productInfo.value!!.materialNumber,
                                                        code = scannedStampCode.value!!,
                                                        placeCode = productInfo.value!!.placeCode
                                                    )
                )
                count.value = "1"
                selectedPosition.value = GoodsInfoCountType.PARTLY.number
            }
            else{
                //TODO уточнить, выводить ли сообщение, что алкокод не найден
            }
        }
    }

    private fun processItemByBarcode(data: String){
        //TODO реализовать функционал со Списка товаров
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    override fun onClickPosition(position: Int) {
        return
    }

    fun Base36ToBase10(base36: String) : String{
        var base10 = 0.0
        val base36Length = base36.length - 1
        base36.forEachIndexed { index, c ->
            base10 += (Integer.valueOf(c.toString(), 36).toDouble()) * Math.pow(36.0, (base36Length-index).toDouble())
        }
        return base10.toBigDecimal().toString()
    }

    fun onBackPressed(){
        processExciseAlcoProductService.discard()
    }
}
