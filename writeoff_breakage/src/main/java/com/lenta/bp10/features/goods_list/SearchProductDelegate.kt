package com.lenta.bp10.features.goods_list

import com.lenta.bp10.features.good_information.LimitsChecker
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.repos.DatabaseRepository
import com.lenta.bp10.requests.network.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.*
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequest
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
        private val processServiceManager: IWriteOffTaskManager,
        private val sessionInfo: ISessionInfo,
        private var permissionToWriteoffNetRequest: PermissionToWriteoffNetRequest,
        private var goodInfoNetRequest: GoodInfoNetRequest
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
                processServiceManager,
                sessionInfo,
                permissionToWriteoffNetRequest,
                goodInfoNetRequest
        )

        searchProductDelegate.init(scanResultHandler, checksEnabled, limitsChecker)

        return searchProductDelegate
    }

    fun init(scanResultHandler: ((ScanInfoResult?) -> Boolean)? = null, checksEnabled: Boolean = true, limitsChecker: LimitsChecker? = null) {
        this.scanResultHandler = scanResultHandler
        this.checksEnabled = checksEnabled
        this.limitsChecker = limitsChecker
    }

    fun searchCode(code: String) {
        checksEnabled = true

        actionByNumber(
                number = code,
                funcForEan = { ean, weight -> actionWithEan(ean, weight) },
                funcForMaterial = ::actionWithMaterial,
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showIncorrectEanFormat
        )
    }

    private fun actionWithEan(ean: String, weight: Double) {
        launch {
            database.getProductInfoByEan(ean)?.let { productInfo ->
                scanInfoResult = ScanInfoResult(productInfo, weight)
                searchProduct()
            } ?: loadProductInfoByEan(ean, weight)
        }
    }

    private suspend fun loadProductInfoByEan(ean: String, weight: Double) {
        navigator.showProgressLoadingData(::handleFailure)
        goodInfoNetRequest(GoodInfoParams(
                tkNumber = sessionInfo.market.orEmpty(),
                ean = ean
        )).also {
            navigator.hideProgress()
        }.either(::handleFailure) { result ->
            handleLoadProductInfoResult(result, weight)
        }
    }

    private fun actionWithMaterial(material: String) {
        launch {
            database.getProductInfoByMaterial(material)?.let { productInfo ->
                scanInfoResult = ScanInfoResult(productInfo, 0.0)
                searchProduct()
            } ?: loadProductInfoByMaterial(material)
        }
    }

    private suspend fun loadProductInfoByMaterial(material: String) {
        navigator.showProgressLoadingData(::handleFailure)
        goodInfoNetRequest(GoodInfoParams(
                tkNumber = sessionInfo.market.orEmpty(),
                material = material.takeLast(6)
        )).also {
            navigator.hideProgress()
        }.either(::handleFailure) { result ->
            handleLoadProductInfoResult(result, 0.0)
        }
    }

    private fun handleLoadProductInfoResult(result: GoodInfoResult, quantity: Double) {
        launch {
            result.material?.let { material ->
                val isMarkedGood = material.isMark.orEmpty().isNotEmpty() || material.markType.orEmpty().isNotEmpty()
                scanInfoResult = ScanInfoResult(
                        productInfo = ProductInfo(
                                materialNumber = material.material.orEmpty(),
                                description = material.name.orEmpty(),
                                uom = database.getUnitsByCode(result.material.buom.orEmpty()),
                                type = getProductType(isAlco = material.isAlco?.isNotEmpty() == true, isExcise = material.isExcise?.isNotEmpty() == true, isMarkedGood = isMarkedGood),
                                isSet = !result.set.isNullOrEmpty(),
                                sectionId = material.abtnr.orEmpty(),
                                matrixType = getMatrixType(material.matrixType.orEmpty()),
                                materialType = material.materialType.orEmpty(),
                                markedGoodType = material.markType.orEmpty()
                        ),
                        quantity = quantity)

                searchProduct()
            }
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

            val defaultQuantity = if (isMarkedProductType(infoResult)) 0.0 else infoResult.quantity
            openProductScreen(
                    productInfo = infoResult.productInfo,
                    quantity = defaultQuantity
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