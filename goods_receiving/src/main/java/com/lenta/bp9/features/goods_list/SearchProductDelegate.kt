package com.lenta.bp9.features.goods_list

import android.content.Context
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.platform.requestCodeTypeBarCode
import com.lenta.bp9.platform.requestCodeTypeSap
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.models.core.GisControl
import com.lenta.shared.models.core.ProductType
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
        private val taskManager: IReceivingTaskManager,
        private val sessionInfo: ISessionInfo,
        private val context: Context
) {

    private var scanInfoResult: ScanInfoResult? = null

    private var searchFromScan: Boolean = false

    private lateinit var viewModelScope: () -> CoroutineScope

    private var scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null

    private var codeWith12Digits: String? = null

    fun copy(): SearchProductDelegate {
        val searchProductDelegate = SearchProductDelegate(
                hyperHive,
                screenNavigator,
                scanInfoRequest,
                taskManager,
                sessionInfo,
                context
        )
        searchProductDelegate.init(viewModelScope, scanResultHandler)
        return searchProductDelegate
    }

    fun init(viewModelScope: () -> CoroutineScope, scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null) {
        this.viewModelScope = viewModelScope
        this.scanResultHandler = scanResultHandler
    }

    fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {
        searchFromScan = fromScan
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

    private fun handleSearchSuccess(scanInfoResult: ScanInfoResult) {
        this.scanInfoResult = scanInfoResult
        searchProduct()
    }

    private fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure, "97")
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

    private fun searchProduct() {
        Logg.d { "searchProduct ${scanInfoResult?.productInfo?.materialNumber}" }
        scanInfoResult?.let { infoResult ->
            val taskProductInfo = taskManager.getReceivingTask()!!.taskRepository.getProducts().findProduct(infoResult.productInfo.materialNumber)
            if (taskProductInfo == null) {
                screenNavigator.openAlertGoodsNotInOrderScreen()
                return
            }
            scanResultHandler?.let { handle ->
                if (handle(infoResult)) {
                    return
                }
            }
            openProductScreen(taskProductInfo)
        }
    }

    fun openProductScreen(taskProductInfo: TaskProductInfo) {
        when (taskProductInfo.type) {
            ProductType.General -> screenNavigator.openGoodsInfoScreen(taskProductInfo)
            ProductType.ExciseAlcohol -> {
                if (taskProductInfo.isSet) {
                    screenNavigator.openNotImplementedScreenAlert("Информация о наборе")
                    //screenNavigator.openSetsInfoScreen(taskProductInfo)
                    return
                } else
                screenNavigator.openExciseAlcoInfoScreen(taskProductInfo)
            }
            else -> screenNavigator.openGoodsInfoScreen(taskProductInfo)
        }
    }
}