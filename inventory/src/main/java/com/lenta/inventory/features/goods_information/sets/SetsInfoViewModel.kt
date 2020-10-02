package com.lenta.inventory.features.goods_information.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.goods_details_storage.ComponentItem
import com.lenta.inventory.features.goods_list.SearchProductDelegate
import com.lenta.inventory.models.task.ProcessSetsService
import com.lenta.inventory.models.task.TaskExciseStamp
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigInteger
import javax.inject.Inject

class SetsInfoViewModel : CoreViewModel(), OnPositionClickListener {

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

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    val iconRes: MutableLiveData<Int> = MutableLiveData(0)
    val textColor: MutableLiveData<Int> = MutableLiveData(0)
    val msgWrongProducType: MutableLiveData<String> = MutableLiveData()
    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    private val componentsInfo: ArrayList<SetComponentInfo> = ArrayList()
    val titleProgressScreen: MutableLiveData<String> = MutableLiveData()
    val storePlaceNumber: MutableLiveData<String> = MutableLiveData()
    val isStorePlaceNumber: MutableLiveData<Boolean> = storePlaceNumber.map { it != "00" }
    val componentsSelectionsHelper = SelectionItemsHelper()
    val spinList: MutableLiveData<List<String>> = MutableLiveData()
    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = MutableLiveData(0.0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val stampAnotherProduct: MutableLiveData<String> = MutableLiveData()
    val alcocodeNotFound: MutableLiveData<String> = MutableLiveData()
    private val scannedStampCode: MutableLiveData<String> = MutableLiveData()
    private var countRunRest = 0
    private val arrExciseGoodsRestInfo: ArrayList<ExciseGoodsRestInfo> = ArrayList()
    val limitExceeded: MutableLiveData<String> = MutableLiveData()

    private val totalCount: MutableLiveData<Double> = countValue.map {
        (it ?: 0.0) + (processSetsService.getFactCount() ?: productInfo.value!!.factCount)
    }

    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${productInfo.value!!.uom.name}" }

    val componentsItem: MutableLiveData<List<ComponentItem>> = countValue.map {
        componentsInfo.mapIndexed { index, componentInfo ->
            val countExciseStampForComponent = processSetsService.getCountExciseStampsForComponent(componentInfo)
            ComponentItem(
                    number = index + 1,
                    name = "${componentInfo.number.substring(componentInfo.number.length - 6)} ${componentInfo.name}",
                    quantity = "$countExciseStampForComponent из ${((componentInfo.count).toDouble()).toStringFormatted()}",
                    menge = componentInfo.count,
                    even = index % 2 == 0,
                    countSets = totalCount.value ?: 0.0,
                    selectedPosition = selectedPosition.value!!,
                    setMaterialNumber = componentInfo.setNumber
            )
        }
    }

    val enabledMissingButton: MutableLiveData<Boolean> = totalCount.map {
        it ?: 0.0 == 0.0
    }

    val visibilityMissingButton: MutableLiveData<Boolean> = selectedPage.map {
        it == 0
    }

    val enabledApplyButton: MutableLiveData<Boolean> = totalCount.map {
        ((it ?: 0.0) > 0.0 && processSetsService.getCountExciseStampsForComponents() == 0)
    }

    val enabledCleanButton: MutableLiveData<Boolean> = selectedPage
            .combineLatest(componentsSelectionsHelper.selectedPositions)
            .map {
                val selectedTabPos = selectedPage.value ?: 0
                val selectedComponentsPositions = componentsSelectionsHelper.selectedPositions.value
                if (selectedTabPos == 0) true else !selectedComponentsPositions.isNullOrEmpty()
            }

    val visibilityCleanButton: MutableLiveData<Boolean> = selectedPage.map {
        it == 1
    }


    init {
        launchUITryCatch {
            screenNavigator.showProgress(titleProgressScreen.value!!)
            suffix.value = productInfo.value?.uom?.name
            storePlaceNumber.value = productInfo.value!!.placeCode
            searchProductDelegate.init(viewModelScope = this@SetsInfoViewModel::viewModelScope,
                    scanResultHandler = this@SetsInfoViewModel::handleProductSearchResult,
                    storePlace = storePlaceNumber.value ?: "00")
            withContext(Dispatchers.IO) {
                if (processSetsService.newProcessSetsService(productInfo.value!!) == null) {
                    screenNavigator.goBack()
                    screenNavigator.openAlertScreen(
                            message = msgWrongProducType.value!!,
                            iconRes = iconRes.value!!,
                            textColor = textColor.value!!,
                            pageNumber = "98")
                }
            }
            componentsInfo.addAll(processSetsService.getComponentsForSet())
            screenNavigator.hideProgress()

        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleProductSearchResult(scanInfoResult: ScanInfoResult?): Boolean {
        enabledBtn()
        screenNavigator.goBack()
        return false
    }

    private fun updateComponents() {
        countValue.value = countValue.value
        componentsSelectionsHelper.clearPositions()
    }

    fun onResume() {
        var totalCountComponents = 0.0
        var totalCountExciseStampForComponents = 0
        componentsInfo.map { componentInfo ->
            totalCountComponents += componentInfo.count.toDouble()
            totalCountExciseStampForComponents += processSetsService.getCountExciseStampsForComponent(componentInfo)
        }
        if (totalCountComponents == totalCountExciseStampForComponents.toDouble() && totalCountExciseStampForComponents != 0) {
            processSetsService.applyComponentsExciseStamps()
            count.value = "1"
            countValue.value = countValue.value!!.plus(1.0)
            selectedPage.value = 0
        }

        updateComponents()
    }

    fun onClickMissing() {
        processSetsService.markMissing()
        screenNavigator.goBack()
    }

    fun onClickApply() {
        processSetsService.apply(totalCount.value!!)
        screenNavigator.goBack()
    }

    fun onClickClean() {
        componentsSelectionsHelper.selectedPositions.value?.map { position ->
            processSetsService.clearExciseStampsForComponent(componentsInfo[position])
        }
        updateComponents()
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
        if (processSetsService.isTaskAlreadyHasExciseStamp(stampCode)) {
            screenNavigator.openAlertDoubleScanStamp()
            return
        }

        countRunRest = 0
        arrExciseGoodsRestInfo.clear()

        launchUITryCatch {
            screenNavigator.showProgress(titleProgressScreen.value!!)
            componentsInfo.map { componentInfo ->
                obtainingDataExciseGoodsNetRequest(
                        ExciseGoodsParams(
                                werks = sessionInfo.market.orEmpty(),
                                materialNumber = componentInfo.setNumber,
                                materialNumberComp = componentInfo.number,
                                stampCode = stampCode,
                                boxNumber = "",
                                manufacturerCode = "",
                                bottlingDate = "",
                                mode = "1",
                                codeEBP = "INV",
                                factCount = ""

                        )).either(::handleFailure, ::processPdf150HandleSuccess)
            }
            screenNavigator.hideProgress()
        }
    }

    private fun processPdf150HandleSuccess(exciseGoodsRestInfo: ExciseGoodsRestInfo) {
        if (exciseGoodsRestInfo.retCode != "0") {
            screenNavigator.openAlertScreen(exciseGoodsRestInfo.errorTxt, iconRes = iconRes.value!!, textColor = textColor.value, pageNumber = "98")
            return
        }

        if (countRunRest <= componentsInfo.size) {
            countRunRest += 1
            arrExciseGoodsRestInfo.add(exciseGoodsRestInfo)
        }
        if (countRunRest == componentsInfo.size) {
            componentsInfo.forEachIndexed { index, setComponentInfo ->
                if (arrExciseGoodsRestInfo[index].materialNumber == setComponentInfo.number) {
                    val countExciseStampForComponent = processSetsService.getCountExciseStampsForComponent(setComponentInfo)
                    if (countExciseStampForComponent >= (setComponentInfo.count).toDouble()) {
                        screenNavigator.openAlertScreen(limitExceeded.value!!, iconRes = iconRes.value!!, textColor = textColor.value, pageNumber = "98")
                    } else {
                        processSetsService.addCurrentComponentExciseStamp(
                                TaskExciseStamp(
                                        materialNumber = setComponentInfo.number,
                                        code = scannedStampCode.value!!,
                                        placeCode = setComponentInfo.placeCode,
                                        setMaterialNumber = setComponentInfo.setNumber
                                )
                        )
                        screenNavigator.openSetComponentsScreen(
                                componentInfo = setComponentInfo,
                                isStamp = true)
                    }
                    return
                }
            }
            screenNavigator.openAlertScreen(stampAnotherProduct.value!!, iconRes = iconRes.value!!, textColor = textColor.value, pageNumber = "98")
        }
    }

    private fun processPdf68(stampCode: String) {
        if (processSetsService.isTaskAlreadyHasExciseStamp(stampCode)) {
            screenNavigator.openAlertDoubleScanStamp()
            return
        }

        launchUITryCatch {
            screenNavigator.showProgress(titleProgressScreen.value!!)
            alcoCodeNetRequest(null).either(::handleFailure, ::alcoCodeHandleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun alcoCodeHandleSuccess(alcoCodeRestInfo: List<AlcoCodeRestInfo>) {
        componentsInfo.map { componentInfo ->
            alcoCodeRestInfo[0].data.filter { data ->
                data[1] == componentInfo.number &&
                        (data[2] == BigInteger(scannedStampCode.value!!.substring(7, 19), 36).toString().padStart(19, '0') ||
                                data[2] == BigInteger(scannedStampCode.value!!.substring(7, 19), 36).toString().padStart(20, '0'))
            }.isNotEmpty().let {
                if (it) {
                    val countExciseStampForComponent = processSetsService.getCountExciseStampsForComponent(componentInfo)
                    if (countExciseStampForComponent >= (componentInfo.count).toDouble()) {
                        screenNavigator.openAlertScreen("${limitExceeded.value!!} (${componentInfo.number.substring(componentInfo.number.length - 6)})", iconRes = iconRes.value!!, textColor = textColor.value, pageNumber = "98")
                    } else {
                        processSetsService.addCurrentComponentExciseStamp(
                                TaskExciseStamp(
                                        materialNumber = componentInfo.number,
                                        code = scannedStampCode.value!!,
                                        placeCode = componentInfo.placeCode,
                                        setMaterialNumber = componentInfo.setNumber
                                )
                        )
                        screenNavigator.openSetComponentsScreen(componentInfo = componentInfo, isStamp = true)
                    }
                    return
                }
            }
        }

        screenNavigator.openAlertScreen(alcocodeNotFound.value!!, iconRes = iconRes.value!!, textColor = textColor.value, pageNumber = "98")
    }

    private fun processItemByBarcode(searchCode: String) {
        componentsInfo.filter {
            it.number == searchCode || processSetsService.isHaveEanForComponent(it.number, searchCode)
        }.map { componentInfo ->
            val countExciseStampForComponent = processSetsService.getCountExciseStampsForComponent(componentInfo)
            if (countExciseStampForComponent >= (componentInfo.count).toDouble()) {
                screenNavigator.openAlertScreen("${limitExceeded.value!!} ($searchCode)", iconRes = iconRes.value!!, textColor = textColor.value, pageNumber = "98")
            } else {
                screenNavigator.openSetComponentsScreen(componentInfo = componentInfo, isStamp = false)
            }
            return
        }

        searchProductDelegate.searchCode(code = searchCode, fromScan = true, isBarCode = true)
    }

    private fun enabledBtn() {
        if (enabledApplyButton.value!!) {
            processSetsService.apply(totalCount.value!!)
        } else {
            if (enabledMissingButton.value!!) {
                processSetsService.markMissing()
            }
        }
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
        updateComponents()
    }

    fun onBackPressed() {
        processSetsService.discard()
    }

    fun onClickComponentTitle(position: Int) {
        processItemByBarcode(componentsInfo[position].number)
    }

    override fun onClickPosition(position: Int) {
        return
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }
}
