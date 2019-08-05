package com.lenta.inventory.features.goods_information.sets.components

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.goods_information.sets.SetComponentInfo
import com.lenta.inventory.models.task.ProcessSetsService
import com.lenta.inventory.models.task.TaskExciseStamp
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
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

    val iconRes: MutableLiveData<Int> = MutableLiveData(0)
    val textColor: MutableLiveData<Int> = MutableLiveData(0)
    val componentInfo: MutableLiveData<SetComponentInfo> = MutableLiveData()
    val storePlaceNumber: MutableLiveData<String> = MutableLiveData()
    val isStorePlaceNumber: MutableLiveData<Boolean> = storePlaceNumber.map { it != "00" }
    val limitExceeded: MutableLiveData<String> = MutableLiveData()
    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = MutableLiveData(0.0)
    private val totalCount: MutableLiveData<Double> = countValue.map {
        processSetsService.getCountExciseStampsForComponent(componentInfo.value!!).toDouble()
    }
    val totalCountWithUom: MutableLiveData<String> = totalCount.map {
        "${it.toStringFormatted()} из ${((componentInfo.value!!.count).toDouble()).toStringFormatted()}"
    }

    val suffix: MutableLiveData<String> = MutableLiveData()
    val spinList: MutableLiveData<List<String>> = MutableLiveData()
    val titleProgressScreen: MutableLiveData<String> = MutableLiveData()
    private val scannedStampCode: MutableLiveData<String> = MutableLiveData()
    val stampAnotherProduct: MutableLiveData<String> = MutableLiveData()
    val alcocodeNotFound: MutableLiveData<String> = MutableLiveData()
    val componentNotFound: MutableLiveData<String> = MutableLiveData()
    val topTitle: MutableLiveData<String> = MutableLiveData()
    val isStamp: MutableLiveData<Boolean> = MutableLiveData()

    val enabledRollbackButton: MutableLiveData<Boolean> = countValue.map {
        (it ?: 0.0) > 0.0
    }

    val enabledApplyButton: MutableLiveData<Boolean> = totalCount.map {
        (it ?: 0.0) >= (componentInfo.value!!.count).toDouble()
    }

    init {
        viewModelScope.launch {
            suffix.value = componentInfo.value?.uom?.name
            storePlaceNumber.value = componentInfo.value!!.placeCode
            topTitle.value = "${componentInfo.value!!.number.substring(componentInfo.value!!.number.length - 6)} ${componentInfo.value!!.name}"
            if (isStamp.value!!) {
                count.value = "1"
                countValue.value = 1.0
            }
        }
    }

    fun onClickRollback() {
        countValue.value = processSetsService.rollback().toDouble()
    }

    //todo
    fun onClickApply() {
        if (componentInfo.value!!.number.substring(componentInfo.value!!.number.length - 6) == "382322") {
            processSetsService.addCurrentComponentExciseStamp(
                    TaskExciseStamp(
                            materialNumber = componentInfo.value!!.number,
                            code = "951869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5060",
                            placeCode = componentInfo.value!!.placeCode,
                            setMaterialNumber = componentInfo.value!!.setNumber
                    )
            )
            count.value = "1"
            countValue.value = countValue.value!!.plus(1.0)
        } else {
            processSetsService.addCurrentComponentExciseStamp(
                    TaskExciseStamp(
                            materialNumber = componentInfo.value!!.number,
                            code = "751869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5060",
                            placeCode = componentInfo.value!!.placeCode,
                            setMaterialNumber = componentInfo.value!!.setNumber
                    )
            )
            processSetsService.addCurrentComponentExciseStamp(
                    TaskExciseStamp(
                            materialNumber = componentInfo.value!!.number,
                            code = "851869302882400418200O46BLILJ3I8DPM0DV1NT3PAFZR9M13NK6CD9LOQKOE0VX143NMT2JZSTII5RQG0A5N5YVB8BG90UPAC5BTDNALGLOH9YH8SP1QV6RJ9YTFIIQAG2RFX73JKZLAM3C5060",
                            placeCode = componentInfo.value!!.placeCode,
                            setMaterialNumber = componentInfo.value!!.setNumber
                    )
            )
            count.value = "2"
            countValue.value = countValue.value!!.plus(2.0)
        }


        //processSetsService.applyComponent()
        screenNavigator.goBack()
    }

    fun onBackPressed() {
        processSetsService.clearExciseStampsForComponent(componentInfo.value!!)
    }

    fun onScanResult(data: String) {
        scannedStampCode.value = data
        when (data.length) {
            68 -> processPdf68(data)
            150 -> processPdf150(data)
            else -> processItemByBarcode(data)
        }
    }

    private fun processPdf150(stampCode: String) {
        if (totalCount.value!! >= (componentInfo.value!!.count).toDouble() ) {
            count.value = "0"
            screenNavigator.openAlertScreen(limitExceeded.value!!, iconRes = iconRes.value!!, textColor = textColor.value, pageNumber = "98")
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
                            materialNumber = componentInfo.value!!.setNumber,
                            materialNumberComp = componentInfo.value!!.number,
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
            count.value = "0"
            screenNavigator.openAlertScreen(exciseGoodsRestInfo.errorTxt, iconRes = iconRes.value!!, textColor = textColor.value, pageNumber = "98")
            return
        }

        if (exciseGoodsRestInfo.materialNumber == componentInfo.value!!.number) {
            processSetsService.addCurrentComponentExciseStamp(
                    TaskExciseStamp(
                            materialNumber = componentInfo.value!!.number,
                            code = scannedStampCode.value!!,
                            placeCode = componentInfo.value!!.placeCode,
                            setMaterialNumber = componentInfo.value!!.setNumber
                    )
            )
            count.value = "1"
            countValue.value = countValue.value!!.plus(1.0)
        } else {
            count.value = "0"
            screenNavigator.openAlertScreen(stampAnotherProduct.value!!, iconRes = iconRes.value!!, textColor = textColor.value, pageNumber = "98")
        }
    }

    private fun processPdf68(stampCode: String) {
        if (totalCount.value!! >= (componentInfo.value!!.count).toDouble()) {
            count.value = "0"
            screenNavigator.openAlertScreen(limitExceeded.value!!, iconRes = iconRes.value!!, textColor = textColor.value, pageNumber = "98")
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

    private fun alcoCodeHandleSuccess(alcoCodeRestInfo: List<AlcoCodeRestInfo>) {
        alcoCodeRestInfo[0].data.filter { data ->
            data[1] == componentInfo.value!!.number &&
                    (data[2] == BigInteger(scannedStampCode.value!!.substring(7, 19), 36).toString().padStart(19, '0') ||
                            data[2] == BigInteger(scannedStampCode.value!!.substring(7, 19), 36).toString().padStart(20, '0'))
        }.isNotEmpty().let {
            if (it) {
                processSetsService.addCurrentComponentExciseStamp(
                        TaskExciseStamp(
                                materialNumber = componentInfo.value!!.number,
                                code = scannedStampCode.value!!,
                                placeCode = componentInfo.value!!.placeCode,
                                setMaterialNumber = componentInfo.value!!.setNumber
                        )
                )
                count.value = "1"
                countValue.value = countValue.value!!.plus(1.0)
            } else {
                count.value = "0"
                screenNavigator.openAlertScreen(alcocodeNotFound.value!!, iconRes = iconRes.value!!, textColor = textColor.value, pageNumber = "98")
            }
        }
    }

    private fun processItemByBarcode(eanCode: String) {
        processSetsService.getComponentsForSet().filter { component ->
            component.number == eanCode
        }.map {
            processSetsService.clearExciseStampsForComponent(componentInfo.value!!)
            componentInfo.value = it
            count.value = "0"
            countValue.value = 0.0
            topTitle.value = "${componentInfo.value!!.number.substring(componentInfo.value!!.number.length - 6)} ${componentInfo.value!!.name}"
            return
        }

        screenNavigator.openAlertScreen(componentNotFound.value!!, iconRes = iconRes.value!!, textColor = textColor.value, pageNumber = "98")
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    override fun onClickPosition(position: Int) {
        return
    }
}
