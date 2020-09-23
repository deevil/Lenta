package com.lenta.bp10.features.goods_list

import com.lenta.bp10.features.good_information.LimitsChecker
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.repos.DatabaseRepository
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
import com.lenta.shared.utilities.actionByNumber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SearchProductDelegate @Inject constructor(
        private val database: DatabaseRepository,
        private val navigator: IScreenNavigator,
        private val scanInfoRequest: ScanInfoRequest,
        private val processServiceManager: IWriteOffTaskManager,
        private val sessionInfo: ISessionInfo,
        private var permissionToWriteoffNetRequest: PermissionToWriteoffNetRequest
) : CoroutineScope {

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    private var scanInfoResult: ScanInfoResult? = null

    private var checksEnabled: Boolean = true

    private var limitsChecker: LimitsChecker? = null

    private var scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null

    fun copy(): SearchProductDelegate {
        val searchProductDelegate = SearchProductDelegate(
                database,
                navigator,
                scanInfoRequest,
                processServiceManager,
                sessionInfo,
                permissionToWriteoffNetRequest
        )

        searchProductDelegate.init(scanResultHandler, checksEnabled, limitsChecker)

        return searchProductDelegate
    }

    fun init(scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null, checksEnabled: Boolean = true, limitsChecker: LimitsChecker? = null) {
        this.scanResultHandler = scanResultHandler
        this.checksEnabled = checksEnabled
        this.limitsChecker = limitsChecker
    }

    fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {
        Logg.d { "hashCode: ${hashCode()}" }

        checksEnabled = true

        actionByNumber(
                number = code,
                funcForEan = { ean ->  actionWithEan(ean, fromScan) },
                funcForMaterial = { material ->  actionWithMaterial(material, fromScan) },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showIncorrectEanFormat
        )
    }

    private fun actionWithMaterial(number: String, fromScan: Boolean) {
        searchGoodInfoByNumber(number, fromScan = fromScan, isBarCode = false)
    }

    private fun actionWithEan(number: String, fromScan: Boolean) {
        searchGoodInfoByNumber(number, fromScan = fromScan, isBarCode = true)
    }

    private fun searchGoodInfoByNumber(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {
        launch {
            navigator.showProgress(scanInfoRequest)
            scanInfoRequest(
                    ScanInfoRequestParams(
                            number = code,
                            tkNumber = processServiceManager.getWriteOffTask()!!.taskDescription.tkNumber,
                            fromScan = fromScan,
                            isBarCode = isBarCode
                    )
            ).either(::handleFailure, ::handleSearchSuccess)

            navigator.hideProgress()
        }
    }

    private fun checkPermissions() {
        launch {
            val taskTypeCode = processServiceManager.getWriteOffTask()?.taskDescription?.taskType?.code.orEmpty()
            if (checksEnabled && database.isChkOwnpr(taskTypeCode)) {
                navigator.showProgress(permissionToWriteoffNetRequest)

                permissionToWriteoffNetRequest(
                        PermissionToWriteoffPrams(
                                matnr = scanInfoResult!!.productInfo.materialNumber,
                                werks = sessionInfo.market!!)
                ).either(::handleFailure, ::handlePermissionsSuccess)

                checksEnabled = false
                navigator.hideProgress()

                return@launch
            }

            handleSearchResultOrOpenProductScreen()
        }
    }

    private fun handleSearchResultOrOpenProductScreen() {
        scanInfoResult?.let { infoResult ->
            scanResultHandler?.let { handle ->
                if (handle(infoResult)) {
                    return
                }
            }

            openProductScreen(
                    productInfo = infoResult.productInfo,
                    quantity = if (isMarkedProductType(infoResult)) 0.0 else infoResult.quantity
            )
        }
    }

    private fun isMarkedProductType(infoResult: ScanInfoResult): Boolean {
        return with(infoResult) {
            productInfo.type == ProductType.ExciseAlcohol && !productInfo.isSet ||
                    productInfo.type == ProductType.Marked
        }
    }

    private fun handleFailure(failure: Failure) {
        navigator.openAlertScreen(failure)
    }

    private fun handleSearchSuccess(scanInfoResult: ScanInfoResult) {
        Logg.d { "scanInfoResult: $scanInfoResult" }
        this.scanInfoResult = scanInfoResult
        searchProduct()
    }

    private fun handlePermissionsSuccess(permissionToWriteoff: PermissionToWriteoffRestInfo) {
        if (permissionToWriteoff.ownr.isEmpty()) {
            navigator.showWriteOffToProductionConfirmation {
                handleSearchResultOrOpenProductScreen()
            }
        } else searchProduct()
    }

    private fun searchProduct() {
        if (!checksEnabled) {
            handleSearchResultOrOpenProductScreen()
            return
        }

        scanInfoResult?.let {
            var goodsForTask = false

            val taskDescription = processServiceManager.getWriteOffTask()?.taskDescription

            taskDescription?.materialTypes?.firstOrNull { taskMatType ->
                taskMatType == it.productInfo.materialType
            }?.let {
                goodsForTask = true
            }

            if (!goodsForTask) {
                navigator.openAlertGoodsNotForTaskScreen()
                return
            }

            goodsForTask = false

            taskDescription?.gisControls?.let { gisControls ->
                Logg.d { "--> Valid gis controls for task = $gisControls" }
                if (gisControls.contains(it.productInfo.type.code)) {
                    goodsForTask = true
                }
            }

            if (!goodsForTask) {
                navigator.openAlertGoodsNotForTaskScreen()
                return
            }

            it.productInfo.matrixType.let { matrixType ->
                if (checksEnabled && !matrixType.isNormal()) {
                    navigator.openMatrixAlertScreen(matrixType) {
                        checkPermissions()
                    }
                    return
                }
            }
        }

        checkPermissions()
    }

    fun openProductScreen(productInfo: ProductInfo, quantity: Double) {
        Logg.d { "--> Product info type: ${productInfo.type.name}" }
        when (productInfo.type) {
            ProductType.General -> navigator.openGoodInfoScreen(productInfo, quantity)
            ProductType.ExciseAlcohol -> {
                if (productInfo.isSet) {
                    navigator.openSetsInfoScreen(productInfo, quantity)
                    return
                } else {
                    navigator.openExciseAlcoScreen(productInfo)
                }
            }
            ProductType.Marked -> navigator.openMarkedInfoScreen(productInfo, quantity)
            else -> navigator.openGoodInfoScreen(productInfo, quantity)
        }

        limitsChecker?.check()
    }

}