package com.lenta.bp10.features.goods_list

import com.lenta.bp10.fmp.resources.dao_ext.isChkOwnpr
import com.lenta.bp10.fmp.resources.tasks_settings.ZmpUtz29V001Rfc
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.platform.requestCodeAddProduct
import com.lenta.bp10.platform.requestCodeTypeBarCode
import com.lenta.bp10.platform.requestCodeTypeSap
import com.lenta.bp10.requests.network.PermissionToWriteoffNetRequest
import com.lenta.bp10.requests.network.PermissionToWriteoffPrams
import com.lenta.bp10.requests.network.PermissionToWriteoffRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
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
        private val processServiceManager: IWriteOffTaskManager,
        private val sessionInfo: ISessionInfo,
        private var permissionToWriteoffNetRequest: PermissionToWriteoffNetRequest
) {

    private val zmpUtz29V001: ZmpUtz29V001Rfc by lazy {
        ZmpUtz29V001Rfc(hyperHive)
    }

    private var scanInfoResult: ScanInfoResult? = null

    private lateinit var viewModelScope: () -> CoroutineScope

    private var scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null

    private var codeWith12Digits: String? = null

    fun init(viewModelScope: () -> CoroutineScope, scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null) {
        this.viewModelScope = viewModelScope
        this.scanResultHandler = scanResultHandler
    }

    fun searchCode(code: String, fromScan: Boolean, isBarCode :Boolean? = null) {

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
                            tkNumber = processServiceManager.getWriteOffTask()!!.taskDescription.tkNumber,
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
            requestCodeAddProduct -> {
                openGoodInfoScreen()
                true
            }
            requestCodeTypeSap -> {
                searchCode("000000$codeWith12Digits", fromScan = false, isBarCode = false)
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


    private fun openGoodInfoScreen() {
        scanInfoResult?.let { infoResult ->
            scanResultHandler?.let { handle ->
                if (handle(infoResult)) {
                    return
                }
            }
            when (infoResult.productInfo.type) {
                ProductType.General -> screenNavigator.openGoodInfoScreen(infoResult.productInfo, infoResult.quantity)
                ProductType.ExciseAlcohol -> {
                    if (scanInfoResult!!.productInfo.isSet) {
                        screenNavigator.openSetsInfoScreen(infoResult.productInfo)
                        return
                    } else
                    //TODO (Борисенко) реализовать логику для алкоголя и убрать хардкод
                        openNotSupportedMessageScreen()
                }
                else -> openNotSupportedMessageScreen()
            }
        }
    }

    private fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSearchSuccess(scanInfoResult: ScanInfoResult) {
        Logg.d { "scanInfoResult: $scanInfoResult" }
        this.scanInfoResult = scanInfoResult
        viewModelScope().launch {
            if (zmpUtz29V001.isChkOwnpr(processServiceManager.getWriteOffTask()?.taskDescription!!.taskType.code)) {
                screenNavigator.showProgress(permissionToWriteoffNetRequest)
                permissionToWriteoffNetRequest(
                        PermissionToWriteoffPrams(
                                matnr = scanInfoResult.productInfo.materialNumber,
                                werks = sessionInfo.market!!))
                        .either(::handleFailure, ::handlePermissionsSuccess)
                screenNavigator.hideProgress()
            } else {
                searchProduct()
            }
        }
    }

    private fun handlePermissionsSuccess(permissionToWriteoff: PermissionToWriteoffRestInfo) {
        if (permissionToWriteoff.ownr.isEmpty()) {
            screenNavigator.openAlertNotAllowWriteOffToWorkScreen()
        } else searchProduct()
    }

    private fun searchProduct() {

        scanInfoResult?.let {
            var goodsForTask = false

            processServiceManager.getWriteOffTask()?.taskDescription!!.materialTypes.firstOrNull { taskMatType ->
                taskMatType == it.productInfo.materialType
            }?.let {
                goodsForTask = true
            }


            if (!goodsForTask) {
                screenNavigator.openAlertGoodsNotForTaskScreen()
                return
            }

            goodsForTask = false

            if (it.productInfo.type == ProductType.ExciseAlcohol || it.productInfo.type == ProductType.NonExciseAlcohol) {
                processServiceManager.getWriteOffTask()?.taskDescription!!.gisControls.forEach { gis ->
                    if (gis == "A") goodsForTask = true
                }
                if (!goodsForTask) {
                    screenNavigator.openAlertGoodsNotForTaskScreen()
                    return
                }
            }

            it.productInfo.matrixType.let { matrixType ->
                if (!matrixType.isNormal()) {
                    screenNavigator.openMatrixAlertScreen(matrixType = matrixType, codeConfirmation = requestCodeAddProduct)
                    return
                }
            }
        }

        openGoodInfoScreen()

    }


    private fun openNotSupportedMessageScreen() {
        screenNavigator.openAlertScreen("Поддержка данного типа товара в процессе разработки")
    }


}