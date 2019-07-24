package com.lenta.inventory.features.goods_information.sets.components

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.goods_information.excise_alco.GoodsInfoCountType
import com.lenta.inventory.features.goods_information.sets.SetComponentInfo
import com.lenta.inventory.models.task.ProcessSetsService
import com.lenta.inventory.models.task.TaskExciseStamp
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import java.math.BigInteger
import javax.inject.Inject

class SetComponentsViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var processSetsService: ProcessSetsService

    @Inject
    lateinit var obtainingDataExciseGoodsNetRequest: ObtainingDataExciseGoodsNetRequest

    @Inject
    lateinit var alcoCodeNetRequest: AlcoCodeNetRequest

    val componentInfo: MutableLiveData<SetComponentInfo> = MutableLiveData()
    val limitExceeded: MutableLiveData<String> = MutableLiveData()
    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val targetTotalCount: MutableLiveData<Double> = MutableLiveData()
    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = MutableLiveData(0.0)
    private val totalCount: MutableLiveData<Double> = countValue.map { (it ?: 0.0) + processSetsService.getCountExciseStampsForComponent(componentInfo.value!!)}
    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} из ${((componentInfo.value!!.count).toDouble() * targetTotalCount.value!!).toStringFormatted()}" }
    val suffix: MutableLiveData<String> = MutableLiveData()
    val spinList: MutableLiveData<List<String>> = MutableLiveData()
    val titleProgressScreen: MutableLiveData<String> = MutableLiveData()
    private val scannedStampCode: MutableLiveData<String> = MutableLiveData()
    val stampAnotherProduct: MutableLiveData<String> = MutableLiveData()
    val alcocodeNotFound: MutableLiveData<String> = MutableLiveData()

    val enabledButton: MutableLiveData<Boolean> = countValue.map {
        it!! > 0.0
    }

    init {
        viewModelScope.launch {
            suffix.value = componentInfo.value?.uom?.name
        }
    }

    fun onClickRollback() {
        screenNavigator.openAlertScreen(processSetsService.rollback().toString(), pageNumber = "98")
        //countValue.value = countValue.value!!.minus(processSetsService.rollback())
    }

    fun onClickApply() {
        //todo onClickApply
        onScanResult("22N0000154KNI691XDC380V71231001511013ZZ012345678901234567890123456ZZ")
    }

    override fun onClickPosition(position: Int) {
        return
    }

    fun onBackPressed() {
        //todo onBackPressed
        return
        //processExciseAlcoProductService.discard()
    }

    fun onScanResult(data: String) {
        //todo onScanResult
        scannedStampCode.value = data
        when (data.length) {
            68 -> processPdf68(data)
            150 -> processPdf150(data)
            else -> processPdf150(data)
        }
    }

    private fun processPdf150(stampCode: String){
        if ( totalCount.value!! >= ((componentInfo.value!!.count).toDouble() * targetTotalCount.value!!) ) {
            count.value = "0"
            screenNavigator.openAlertScreen(limitExceeded.value!!, pageNumber = "98")
            return
        }

        if (processSetsService.isTaskAlreadyHasExciseStamp(stampCode)) {
            count.value = "0"
            screenNavigator.openAlertDoubleScanStamp()
            return
        }

        viewModelScope.launch {
            screenNavigator.showProgress(titleProgressScreen.value!!)
            obtainingDataExciseGoodsNetRequest(
                    ExciseGoodsParams(
                            werks = sessionInfo.market.orEmpty(),
                            materialNumber = componentInfo.value!!.number,
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
            count.value = "0"
            screenNavigator.openAlertScreen(exciseGoodsRestInfo.errorTxt, pageNumber = "98")
            return
        }

        if (exciseGoodsRestInfo.materialNumber == componentInfo.value!!.number) {
            processSetsService.addCurrentComponentExciseStamps(
                                                    TaskExciseStamp(
                                                            materialNumber = componentInfo.value!!.number,
                                                            code = scannedStampCode.value!!,
                                                            placeCode = componentInfo.value!!.placeCode,
                                                            setMaterialNumber = componentInfo.value!!.setNumber
                                                    )
            )
            count.value = "1"
            countValue.value = countValue.value!!.plus(1.0)
        }
        else {
            count.value = "0"
            screenNavigator.openAlertScreen(stampAnotherProduct.value!!, pageNumber = "98")
        }
    }

    private fun processPdf68(stampCode: String){
        if ( totalCount.value!! >= ((componentInfo.value!!.count).toDouble() * targetTotalCount.value!!) ) {
            count.value = "0"
            screenNavigator.openAlertScreen(limitExceeded.value!!, pageNumber = "98")
            return
        }

        if (processSetsService.isTaskAlreadyHasExciseStamp(stampCode)) {
            count.value = "0"
            screenNavigator.openAlertDoubleScanStamp()
            return
        }

        viewModelScope.launch {
            screenNavigator.showProgress(titleProgressScreen.value!!)
            alcoCodeNetRequest(null).either(::handleFailure, ::alcoCodeHandleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun alcoCodeHandleSuccess(alcoCodeRestInfo: List<AlcoCodeRestInfo>){
        alcoCodeRestInfo[0].data.filter { data ->
            data[1] == componentInfo.value!!.number &&
                    (data[2] == BigInteger(scannedStampCode.value!!.substring(7,19), 36).toString().padStart(19,'0') ||
                            data[2] == BigInteger(scannedStampCode.value!!.substring(7,19), 36).toString().padStart(20,'0'))
        }.isNotEmpty().let {
            if (it) {
                processSetsService.addCurrentComponentExciseStamps(
                        TaskExciseStamp(
                                materialNumber = componentInfo.value!!.number,
                                code = scannedStampCode.value!!,
                                placeCode = componentInfo.value!!.placeCode,
                                setMaterialNumber = componentInfo.value!!.setNumber
                        )
                )
                count.value = "1"
                countValue.value = countValue.value!!.plus(1.0)
            }
            else{
                count.value = "0"
                screenNavigator.openAlertScreen(alcocodeNotFound.value!!, pageNumber = "98")
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }
}
