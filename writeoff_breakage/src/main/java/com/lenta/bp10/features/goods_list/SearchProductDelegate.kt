package com.lenta.bp10.features.goods_list

import com.lenta.bp10.features.good_information.LimitsChecker
import com.lenta.bp10.fmp.resources.dao_ext.isChkOwnpr
import com.lenta.bp10.fmp.resources.tasks_settings.ZmpUtz29V001Rfc
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.platform.requestCodeAddAddToProduction
import com.lenta.bp10.platform.requestCodeAddProductWithBadStamp
import com.lenta.bp10.platform.requestCodeTypeBarCode
import com.lenta.bp10.platform.requestCodeTypeSap
import com.lenta.bp10.requests.network.PermissionToWriteoffNetRequest
import com.lenta.bp10.requests.network.PermissionToWriteoffPrams
import com.lenta.bp10.requests.network.PermissionToWriteoffRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
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
        private val processServiceManager: IWriteOffTaskManager,
        private val sessionInfo: ISessionInfo,
        private var permissionToWriteoffNetRequest: PermissionToWriteoffNetRequest
) {

    private val zmpUtz29V001: ZmpUtz29V001Rfc by lazy {
        ZmpUtz29V001Rfc(hyperHive)
    }

    private var scanInfoResult: ScanInfoResult? = null

    private var checksEnabled: Boolean = true

    private var limitsChecker: LimitsChecker? = null

    private lateinit var viewModelScope: () -> CoroutineScope

    private var scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null

    private var codeWith12Digits: String? = null

    fun copy(): SearchProductDelegate {
        val searchProductDelegate = SearchProductDelegate(
                hyperHive,
                screenNavigator,
                scanInfoRequest,
                processServiceManager,
                sessionInfo,
                permissionToWriteoffNetRequest
        )

        searchProductDelegate.init(viewModelScope, scanResultHandler, checksEnabled, limitsChecker)

        return searchProductDelegate
    }

    fun init(viewModelScope: () -> CoroutineScope, scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null, checksEnabled: Boolean = true, limitsChecker: LimitsChecker? = null) {
        Logg.d { "viewModelScope hash: ${viewModelScope.hashCode()}" }
        this.viewModelScope = viewModelScope
        this.scanResultHandler = scanResultHandler
        this.checksEnabled = checksEnabled
        this.limitsChecker = limitsChecker
    }

    fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {
        Logg.d { "hashCode: ${hashCode()}" }

        checksEnabled = true

        if (checksEnabled && isBarCode == null && code.length == 12) {
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
            ).either(::handleFailure, ::handleSearchSuccess)

            screenNavigator.hideProgress()
        }
    }

    fun handleResultCode(code: Int?): Boolean {
        return when (code) {
            requestCodeAddProductWithBadStamp -> {
                checkPermissions()
                true
            }
            requestCodeTypeSap -> {
                searchCode("000000000000${codeWith12Digits?.takeLast(6)}", fromScan = false, isBarCode = false)
                codeWith12Digits = null
                true
            }
            requestCodeTypeBarCode -> {
                searchCode(code = codeWith12Digits.orEmpty(), fromScan = false, isBarCode = true)
                codeWith12Digits = null
                true
            }
            requestCodeAddAddToProduction -> {
                handleSearchResultOrOpenProductScreen()
                true
            }
            else -> false
        }
    }

    private fun checkPermissions() {
        if (checksEnabled && zmpUtz29V001.isChkOwnpr(processServiceManager.getWriteOffTask()?.taskDescription!!.taskType.code)) {
            screenNavigator.showProgress(permissionToWriteoffNetRequest)

            viewModelScope().launch {
                permissionToWriteoffNetRequest(
                        PermissionToWriteoffPrams(
                                matnr = scanInfoResult!!.productInfo.materialNumber,
                                werks = sessionInfo.market!!)
                ).either(::handleFailure, ::handlePermissionsSuccess)
            }

            checksEnabled = false
            screenNavigator.hideProgress()

            return
        }

        handleSearchResultOrOpenProductScreen()
    }

    private fun handleSearchResultOrOpenProductScreen() {
        scanInfoResult?.let { infoResult ->
            scanResultHandler?.let { handle ->
                if (handle(infoResult)) {
                    return
                }
            }

            with(infoResult) {
                openProductScreen(
                        productInfo = productInfo,
                        quantity = if (productInfo.type == ProductType.ExciseAlcohol && !productInfo.isSet) 0.0 else quantity
                )
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

    private fun handlePermissionsSuccess(permissionToWriteoff: PermissionToWriteoffRestInfo) {
        if (permissionToWriteoff.ownr.isEmpty()) {
            screenNavigator.openWriteOffToProductionConfirmationScreen(requestCodeAddAddToProduction)
        } else searchProduct()
    }

    private fun searchProduct() {
        if (!checksEnabled) {
            handleSearchResultOrOpenProductScreen()
            return
        }

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

            } else if (it.productInfo.type == ProductType.General) {
                processServiceManager.getWriteOffTask()?.taskDescription!!.gisControls.forEach { gis ->
                    if (gis == "N") goodsForTask = true
                }
            }

            if (!goodsForTask) {
                screenNavigator.openAlertGoodsNotForTaskScreen()
                return
            }

            it.productInfo.matrixType.let { matrixType ->
                if (checksEnabled && !matrixType.isNormal()) {
                    screenNavigator.openMatrixAlertScreen(matrixType = matrixType, codeConfirmation = requestCodeAddProductWithBadStamp)
                    return
                }
            }
        }

        checkPermissions()
    }

    fun openProductScreen(productInfo: ProductInfo, quantity: Double) {
        when (productInfo.type) {
            ProductType.General -> screenNavigator.openGoodInfoScreen(productInfo, quantity)
            ProductType.ExciseAlcohol -> {
                if (productInfo.isSet) {
                    screenNavigator.openSetsInfoScreen(productInfo, quantity)
                    return
                } else screenNavigator.openExciseAlcoScreen(productInfo)
            }
            else -> screenNavigator.openGoodInfoScreen(productInfo, quantity)
        }

        limitsChecker?.check()
    }

}