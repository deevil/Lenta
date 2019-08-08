package com.lenta.inventory.features.goods_list

import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.models.task.TaskProductInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.platform.requestCodeTypeBarCode
import com.lenta.inventory.platform.requestCodeTypeSap
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.GisControl
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.isNormal
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequest
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequestParams
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SearchProductDelegate @Inject constructor(
        private val hyperHive: HyperHive,
        private val screenNavigator: IScreenNavigator,
        private val scanInfoRequest: ScanInfoRequest,
        private val taskManager: IInventoryTaskManager,
        private val sessionInfo: ISessionInfo
) {

    private var scanInfoResult: ScanInfoResult? = null

    private lateinit var viewModelScope: () -> CoroutineScope

    private var scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null

    private var storePlaceCode: String = "00"

    private var codeWith12Digits: String? = null

    fun copy(): SearchProductDelegate {
        val searchProductDelegate = SearchProductDelegate(
                hyperHive,
                screenNavigator,
                scanInfoRequest,
                taskManager,
                sessionInfo
        )
        searchProductDelegate.init(viewModelScope, scanResultHandler, storePlaceCode)
        return searchProductDelegate
    }

    fun init(viewModelScope: () -> CoroutineScope, scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null, storePlace: String = "00") {
        Logg.d { "viewModelScope hash: ${viewModelScope.hashCode()}" }
        this.viewModelScope = viewModelScope
        this.scanResultHandler = scanResultHandler
        this.storePlaceCode = storePlace
    }

    fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {

        Logg.d { "hashCode: ${hashCode()}" }

        if (isBarCode == null && code.length == 12) {
            codeWith12Digits = code
            screenNavigator.openSelectTypeCodeScreen(requestCodeTypeSap, requestCodeTypeBarCode)
            return
        }

        viewModelScope().launch {
            screenNavigator.showProgress(scanInfoRequest)
            scanInfoRequest(
                    ScanInfoRequestParams(
                            number = code,
                            tkNumber = sessionInfo.market ?: "",
                            fromScan = fromScan,
                            isBarCode = isBarCode
                    )
            )
                    .either(::handleFailure, ::handleSearchSuccess)
            screenNavigator.hideProgress()

        }
    }

    fun handleResultCode(code: Int?): Boolean {
        return when (code) {
            requestCodeTypeSap -> {
                searchCode("000000000000${codeWith12Digits?.takeLast(6)}", fromScan = false, isBarCode = false)
                codeWith12Digits = null
                true
            }
            requestCodeTypeBarCode -> {
                searchCode(code = codeWith12Digits ?: "", fromScan = false, isBarCode = true)
                codeWith12Digits = null
                true
            }
            else -> false
        }

    }

    private fun handleSearchResultOrOpenProductScreen() {
        scanInfoResult?.let { infoResult ->

            var taskProductInfo = taskManager.getInventoryTask()!!.taskRepository.getProducts().findProduct(infoResult.productInfo.materialNumber, storePlaceCode)
            if (taskProductInfo == null) {
                taskProductInfo = TaskProductInfo(materialNumber = infoResult.productInfo.materialNumber,
                                                  description = infoResult.productInfo.description,
                                                  uom = infoResult.productInfo.uom,
                                                  type = infoResult.productInfo.type,
                                                  isSet = infoResult.productInfo.isSet,
                                                  sectionId = infoResult.productInfo.sectionId,
                                                  matrixType = infoResult.productInfo.matrixType,
                                                  materialType = infoResult.productInfo.materialType,
                                                  placeCode = storePlaceCode,
                                                  factCount = 0.0,
                                                  isPositionCalc = false,
                                                  isExcOld = false

                )

                taskManager.getInventoryTask()!!.taskRepository.getProducts().addProduct(taskProductInfo)
            }

            scanResultHandler?.let { handle ->
                if (handle(infoResult)) {
                    return
                }
            }
            with(infoResult) {
                openProductScreen(taskProductInfo,
                        if (productInfo.type == ProductType.ExciseAlcohol && !productInfo.isSet) 0.0 else quantity)
            }

        }
    }

    private fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure, "97")
    }

    private fun handleSearchSuccess(scanInfoResult: ScanInfoResult) {
        Logg.d { "scanInfoResult: $scanInfoResult" }
        this.scanInfoResult = scanInfoResult
        searchProduct()
    }

    private fun searchProduct() {

        scanInfoResult?.let {
            val productExists = taskManager.getInventoryTask()!!.taskRepository.getProducts().getProducts().findLast { taskProductInfo ->
                taskProductInfo.materialNumber == it.productInfo.materialNumber
            } != null

            val isStrict = taskManager.getInventoryTask()!!.taskDescription.isStrict

            if (isStrict && !productExists) {
                screenNavigator.openAlertGoodsNotForTaskScreen()
                return
            } else {
                val alcoCheckOK: Boolean
                if (it.productInfo.type == ProductType.ExciseAlcohol || it.productInfo.type == ProductType.NonExciseAlcohol) {
                    alcoCheckOK = taskManager.getInventoryTask()!!.taskDescription.gis == GisControl.Alcohol
                } else {
                    alcoCheckOK = taskManager.getInventoryTask()!!.taskDescription.gis == GisControl.GeneralProduct
                }
                if (!alcoCheckOK) {
                    screenNavigator.openAlertWrongGoodsType()
                } else {
                    handleSearchResultOrOpenProductScreen()
                }
            }
        }
    }

    fun openTaskProductScreen(taskProductInfo: TaskProductInfo) {
        when (taskProductInfo.type) {
            ProductType.General -> screenNavigator.openGoodsInfoScreen(taskProductInfo)
            ProductType.ExciseAlcohol -> {
                if (taskProductInfo.isSet) {
                    screenNavigator.openSetsInfoScreen(taskProductInfo)
                    return
                } else
                    Logg.d { "taskProductInfo: $taskProductInfo" }
                screenNavigator.openExciseAlcoInfoScreen(taskProductInfo)
            }
            else -> screenNavigator.openGoodsInfoScreen(taskProductInfo)
        }
    }

    private fun openProductScreen(taskProductInfo: TaskProductInfo, quantity: Double) {
        openTaskProductScreen(taskProductInfo)
    }

}