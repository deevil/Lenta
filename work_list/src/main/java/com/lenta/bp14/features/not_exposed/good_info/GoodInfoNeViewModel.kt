package com.lenta.bp14.features.not_exposed.good_info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.features.base.BaseGoodInfoViewModel
import com.lenta.bp14.models.check_price.IPriceInfoParser
import com.lenta.bp14.models.data.GoodType
import com.lenta.bp14.models.data.getGoodType
import com.lenta.bp14.models.not_exposed.INotExposedTask
import com.lenta.bp14.models.ui.ItemStockUi
import com.lenta.bp14.models.ui.ZPartUi
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.platform.resource.IResourceFormatter
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getMaxPositionsProdWkl
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.getMatrixType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanCodeInfo
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequest
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequestParams
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.actionByNumber
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class GoodInfoNeViewModel : BaseGoodInfoViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var task: INotExposedTask

    @Inject
    lateinit var scanInfoRequest: ScanInfoRequest

    @Inject
    lateinit var priceInfoParser: IPriceInfoParser

    @Inject
    lateinit var hyperHive: HyperHive

    private val maxQuantity: Double? by lazy {
        ZmpUtz14V001(hyperHive).getMaxPositionsProdWkl()
    }

    val goodInfo by lazy {
        task.getProcessedProductInfoResult()!!.goodInfo
    }

    val productParamsUi: MutableLiveData<ProductParamsUi> by lazy {
        MutableLiveData<ProductParamsUi>(
                goodInfo.let {
                    ProductParamsUi(
                            matrixType = getMatrixType(it.productInfo.matrixType),
                            sectionId = it.productInfo.sectionNumber,
                            type = it.productInfo.getGoodType(),
                            isNew = it.productInfo.isNew.isSapTrue(),
                            isHealthyFood = it.productInfo.isHealthyFood.isSapTrue()
                    )
                }
        )
    }

    val stocks: MutableLiveData<List<ItemStockUi>> by lazy {
        MutableLiveData<List<ItemStockUi>>(
                goodInfo.let { goodInfo ->
                    goodInfo.stocks.mapIndexed { index, stock ->
                        val goodInfoUnits = goodInfo.units?.name.orEmpty()
                        val quantity = "${stock.quantity.toStringFormatted()} $goodInfoUnits"
                        ItemStockUi(
                                number = "${index + 1}",
                                storage = stock.storage,
                                quantity = quantity,
                                zPartsQuantity = stock.getZPartQuantity(goodInfoUnits)
                        )
                    }
                }
        )
    }

    val zParts: LiveData<List<ZPartUi>> by unsafeLazy {
        asyncLiveData<List<ZPartUi>> {
            val result = goodInfo.zParts.mapToZPartUiList(goodInfo.units?.name.orEmpty())
            emit(result)
        }
    }


    val originalProcessedProductInfo by lazy {
        task.getProcessedCheckInfo()
    }

    val marketStorage by lazy {
        "${(goodInfo.stocks.sumByDouble { it.quantity }).toStringFormatted()} ${
            goodInfo.units?.name
                    .orEmpty()
        }"
    }

    val quantityField by lazy {
        MutableLiveData("0").apply {
            val needUseQuantity = task.getProcessedCheckInfo()?.quantity ?: 0.0 > 0.0
            if (needUseQuantity) {
                this.value = task.getProcessedProductInfoResult()?.quantity?.toStringFormatted()
            }
        }
    }

    private val quantityValue by lazy {
        quantityField.map {
            it?.toDoubleOrNull() ?: 0.0
        }
    }

    private val totalQuantityValue: MutableLiveData<Double> by lazy {
        quantityValue.map {
            val saved = task.getProcessedCheckInfo()?.quantity ?: 0.0
            saved.sumWith(it)
        }
    }

    val totalQuantity: MutableLiveData<String> by lazy {
        totalQuantityValue.map {
            "${it.dropZeros()} ${goodInfo.units?.name.orEmpty()}"
        }
    }

    val applyButtonEnabled: MutableLiveData<Boolean> by lazy {
        quantityValue.map { it ?: 0.0 != 0.0 }
    }

    val isEmptyPlaceMarked by lazy {
        MutableLiveData<Boolean>(originalProcessedProductInfo?.isEmptyPlaceMarked)
    }

    val isInputNumberEnabled by lazy { isEmptyPlaceMarked.map { it == null } }

    val cancelButtonEnabled by lazy { isEmptyPlaceMarked.map { it != null } }

    val framedButtonEnabled by lazy {
        isEmptyPlaceMarked.map { it == null }
    }
    val notFramedButtonEnabled by lazy {
        isEmptyPlaceMarked.map { it == null }
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    // -----------------------------

    fun onClickCancel() {
        isEmptyPlaceMarked.value = null
    }

    fun onClickFramed() {
        task.setCheckInfo(quantity = null, isEmptyPlaceMarked = true)
        navigator.goBack()
    }

    fun onClickNotFramed() {
        task.setCheckInfo(quantity = null, isEmptyPlaceMarked = false)
        navigator.goBack()
    }

    fun onClickApply() {
        totalQuantityValue.value?.let {
            task.setCheckInfo(quantity = it, isEmptyPlaceMarked = null)
        }
        navigator.goBack()
    }

    fun getTitle(): String? {
        goodInfo.productInfo.let {
            return "${it.matNr.takeLast(6)} ${it.name}"
        }
    }

    fun onBackPressed(): Boolean {
        if (isHaveChangedData()) {
            navigator.openConfirmationNotSaveChanges {
                navigator.goBack()
            }
            return false
        }
        return true
    }

    private fun isHaveChangedData(): Boolean {
        return quantityValue.value ?: 0.0 != originalProcessedProductInfo?.quantity ?: 0.0
    }

    fun onScanResult(data: String) {
        checkCode(data)
    }

    private fun checkCode(code: String?) {
        actionByNumber(
                number = code.orEmpty(),
                funcForEan = { ean, _ -> searchCode(ean) },
                funcForMaterial = { material -> searchCode(material) },
                funcForPriceQrCode = { qrCode ->
                    priceInfoParser.getPriceInfoFromRawCode(qrCode)?.let {
                        searchCode(it.eanCode)
                    } ?: navigator.showGoodNotFound()
                },
                funcForSapOrBar = navigator::showTwelveCharactersEntered,
                funcForNotValidFormat = navigator::showGoodNotFound
        )
    }

    private fun searchCode(code: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)

            scanInfoRequest(
                    ScanInfoRequestParams(
                            number = code,
                            tkNumber = task.getDescription().tkNumber,
                            fromScan = true,
                            isBarCode = true
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure) { scanInfoResult ->
                if (scanInfoResult.productInfo.materialNumber == goodInfo.productInfo.matNr) {
                    if (isEmptyPlaceMarked.value == null) {
                        val newQuantity = ((quantityValue.value ?: 0.0) + scanInfoResult.quantity)
                        //TODO maxQuantity - это максимальное количество позиций в задании. Нужно переделать
                        /*if (maxQuantity != null && newQuantity > maxQuantity!!) {
                            navigator.showMaxCountProductAlert()
                        } else {
                            quantityField.value = newQuantity.toStringFormatted()
                        }*/
                        quantityField.value = newQuantity.toStringFormatted()
                    }
                } else {
                    if (applyButtonEnabled.value == true) {
                        launchUITryCatch {
                            if (task.isAllowedProduct(scanInfoResult.productInfo.materialNumber)) {
                                navigator.showProgressLoadingData(::handleFailure)
                                task.setCheckInfo(
                                        quantity = quantityField.value?.toDoubleOrNull() ?: 0.0,
                                        isEmptyPlaceMarked = null
                                )

                                val scanCodeInfo = ScanCodeInfo(code)
                                val quantity = scanCodeInfo.getQuantity(units = scanInfoResult.productInfo.uom)

                                task.getProductInfoAndSetProcessed(
                                        matNr = scanInfoResult.productInfo.materialNumber,
                                        quantity = quantity
                                ).also {
                                    navigator.hideProgress()
                                }.either(
                                        {
                                            navigator.openAlertScreen(failure = it)
                                        }
                                ) {
                                    navigator.goBack()
                                    navigator.openGoodInfoNeScreen()
                                }
                            } else {
                                navigator.showGoodIsNotPartOfTask()
                            }
                        }
                    }
                }
            }
        }
    }

    fun onStockItemClick(itemIndex: Int) {
        stocks.value?.getOrNull(itemIndex)?.let { stock ->
            navigator.openStorageZPartsNeScreen(stock.storage)
        }.orIfNull {
            Logg.w { "Stock value is null!" }
            navigator.showAlertWithStockItemNotFound()
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }
}

data class ProductParamsUi(
        val matrixType: MatrixType,
        val sectionId: String,
        val type: GoodType,
        val isNew: Boolean,
        val isHealthyFood: Boolean
)

