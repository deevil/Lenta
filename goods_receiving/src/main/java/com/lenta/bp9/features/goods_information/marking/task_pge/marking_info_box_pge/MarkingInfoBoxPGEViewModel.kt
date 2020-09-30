package com.lenta.bp9.features.goods_information.marking.task_pge.marking_info_box_pge

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.features.goods_information.base.BaseGoodsInfo
import com.lenta.bp9.model.processing.ProcessMarkingBoxPGEProductService
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.requestCodeAddGoodsSurplus
import com.lenta.bp9.requests.network.*
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class MarkingInfoBoxPGEViewModel : BaseGoodsInfo() {

    @Inject
    lateinit var processMarkingBoxProductService: ProcessMarkingBoxPGEProductService

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var zmpUtzGrz44V001NetRequest: ZmpUtzGrz44V001NetRequest

    @Inject
    lateinit var hyperHive: HyperHive

    val tvAccept: MutableLiveData<String> by lazy {
        paramGrzAlternMeins.map {
            val uomName = paramGrzAlternMeins.value?.name?.toLowerCase().orEmpty()

            var nestingInOneBlock = productInfo.value?.quantityInvest?.toDouble().toStringFormatted()
            if (nestingInOneBlock == COUNT_PIECES_BOX_IS_ZERO) {
                nestingInOneBlock = COUNT_PIECES_BOX
            }
            val productUomName = productInfo.value?.uom?.name.orEmpty()
            context.getString(R.string.accept, "$uomName=$nestingInOneBlock $productUomName")
        }
    }

    private val countScannedBoxes: MutableLiveData<Int> = MutableLiveData(0)
    private val countScannedStamps: MutableLiveData<Int> = MutableLiveData(0) //блоки и GTINы, переменная используется для кнопки Откатить

    val spinQualityEnabled: MutableLiveData<Boolean> = MutableLiveData(false)
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val suffix: MutableLiveData<String> = MutableLiveData()

    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)
    private val allTypeDiscrepancies: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val paramGrzAlternMeins: MutableLiveData<Uom> = MutableLiveData()

    private val unprocessedQuantityOfBlocks: MutableLiveData<Double> = MutableLiveData(0.0)

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    val isUnitBox: MutableLiveData<Boolean> = MutableLiveData(true)
    val enabled: MutableLiveData<Boolean> = MutableLiveData(false)


    private val enteredCountInBlockUnits: Double
        get() {
            var addNewCount = countValue.value?.toDouble() ?: 0.0

            if (isUnitBox.value == true) {
                var countPiecesBox = productInfo.value?.countPiecesBox?.toDouble() ?: 1.0
                if (countPiecesBox == 0.0) countPiecesBox = 1.0
                addNewCount *= countPiecesBox
            }
            return addNewCount
        }

    val acceptTotalCount: MutableLiveData<Double> =
            isUnitBox
                    .combineLatest(countValue)
                    .combineLatest(spinQualitySelectedPosition)
                    .map {
                        currentQualityInfoCode
                                .takeIf { code -> code == TYPE_DISCREPANCIES_QUALITY_NORM }
                                ?.run { enteredCountInBlockUnits + countAcceptOfProduct }
                                ?: countAcceptOfProduct
                    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val acceptTotalCount = it ?: 0.0
        val purchaseOrderUnits = productInfo.value?.uom?.name.orEmpty()
        val countAcceptOfProductValue = countAcceptOfProduct
        val totalCountAcceptOfProduct =
                countAcceptOfProductValue
                        .takeIf { count -> count > 0.0 }
                        ?.run { "+ ${this.toStringFormatted()}" }
                        ?: countAcceptOfProductValue.toStringFormatted()
        acceptTotalCount
                .takeIf { count1 -> count1 > 0.0 }
                ?.run { "+ ${this.toStringFormatted()} $purchaseOrderUnits" }
                ?: "$totalCountAcceptOfProduct $purchaseOrderUnits"
    }

    val refusalTotalCount: MutableLiveData<Double> =
            isUnitBox
                    .combineLatest(countValue)
                    .combineLatest(spinQualitySelectedPosition)
                    .map {
                        currentQualityInfoCode
                                .takeIf { code -> code != TYPE_DISCREPANCIES_QUALITY_NORM }
                                ?.run { enteredCountInBlockUnits + countRefusalOfProduct }
                                ?: countRefusalOfProduct
                    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.mapSkipNulls {
        productInfo.value.let { productInfoValue ->
            val refusalTotalCount = it
            val purchaseOrderUnits = productInfoValue?.uom?.name
            val totalCountRefusalOfProduct =
                    countRefusalOfProduct
                            .takeIf { count -> count > 0.0 }
                            ?.run { "- ${this.toStringFormatted()}" }
                            ?: countRefusalOfProduct.toStringFormatted()
            refusalTotalCount
                    .takeIf { count -> count > 0.0 }
                    ?.run { "- ${this.toStringFormatted()} $purchaseOrderUnits" }
                    ?: "$totalCountRefusalOfProduct $purchaseOrderUnits"
        }
    }

    val checkStampControlVisibility: MutableLiveData<Boolean> = MutableLiveData()


    val tvBoxListVal: MutableLiveData<String> = refusalTotalCount
            .combineLatest(spinQualitySelectedPosition)
            .combineLatest(spinReasonRejectionSelectedPosition)
            .combineLatest(countScannedBoxes)
            .map {
                getTvBoxListVal()

            }

    private fun getTvBoxListVal(): String? {
        return productInfo.value
                ?.let { product ->
                    val countBoxScannedValue = countScannedBoxes.value ?: 0
                    val totalBlocksProduct = product.origQuantity.toDouble()
                    val countProcessedBlocksCurrentDiscrepancies = processMarkingBoxProductService.getTotalScannedBlocks()
                    if (countBoxScannedValue <= 0) { //фиксируем необработанное количество после первого сканирования марок, чтобы не учитывать их в текущей сессии, иначе это кол-во будет уменьшаться и появиться текст Не требуется
                        unprocessedQuantityOfBlocks.value = totalBlocksProduct - countProcessedBlocksCurrentDiscrepancies
                    }
                    val unprocessedQuantityOfBlocksVal = unprocessedQuantityOfBlocks.value
                            ?: 0.0
                    if (currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM) {
                        checkBoxes(unprocessedQuantityOfBlocksVal, countBoxScannedValue)
                    } else {
                        ""
                    }
                }
    }

    val tvStampControlVal: MutableLiveData<String> = acceptTotalCount
            .combineLatest(spinQualitySelectedPosition)
            .combineLatest(countScannedBoxes)
            .map {
                getTvStampControlVal()
            }

    private fun checkBoxes(unprocessedQuantityOfBlocksVal: Double, countBoxesScannedValue: Int): String {
        return if (unprocessedQuantityOfBlocksVal == enteredCountInBlockUnits) {
            checkBoxStampListVisibility.value = false
            context.getString(R.string.not_required)
        } else {
            checkBoxStampListVisibility.value = true
            buildString {
                append(countBoxesScannedValue.toDouble().toStringFormatted())
                append(" ")
                append(context.getString(R.string.of))
                append(" ")
                append(enteredCountInBlockUnits.toStringFormatted())
            }
        }
    }


    private fun getTvStampControlVal(): String {
        return if (currentQualityInfoCode == TYPE_DISCREPANCIES_QUALITY_NORM) {
            checkStamps()
        } else {
            ""
        }
    }

    private fun checkStamps(): String {
        val acceptTotalCountVal = acceptTotalCount.value ?: 0.0

        val numberStampsControl =
                productInfo.value
                        ?.numberStampsControl
                        ?.toDouble()
                        ?: 0.0
        return if (numberStampsControl == 0.0 || acceptTotalCountVal <= 0.0) {
            checkStampControlVisibility.value = false
            context.getString(R.string.not_required)
        } else {
            checkStampControlVisibility.value = true
            val countStampsControl = if (acceptTotalCountVal < numberStampsControl) acceptTotalCountVal else numberStampsControl
            buildString {
                append(enteredCountInBlockUnits.toStringFormatted())
                append(" ")
                append(paramGrzAlternMeins.value?.name?.toLowerCase().orEmpty())
                append(" ")
                append(MULTIPLY)
                append(" ")
                append(countStampsControl.toInt())
            }
        }
    }

    val checkStampControl: MutableLiveData<Boolean> = checkStampControlVisibility.map {
        val countBlockScanned = processMarkingBoxProductService.getCountProcessedBlockForDiscrepancies(TYPE_DISCREPANCIES_QUALITY_NORM).toDouble()
        val numberStampsControl = productInfo.value?.numberStampsControl?.toDouble() ?: 0.0
        countBlockScanned >= numberStampsControl
    }


    val checkBoxStampListVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val checkBoxStampList: MutableLiveData<Boolean> = checkBoxStampListVisibility.map {
        val countBlockScanned = countScannedBoxes.value ?: 0
        (currentTypeDiscrepanciesCode != TYPE_DISCREPANCIES_QUALITY_NORM && it == false)
                || (countBlockScanned >= enteredCountInBlockUnits && enteredCountInBlockUnits > 0.0)
    }

    val enabledApplyButton: MutableLiveData<Boolean> =
            spinQualitySelectedPosition
                    .combineLatest(checkStampControl)
                    .combineLatest(checkBoxStampList)
                    .combineLatest(acceptTotalCount)
                    .map {
                        getEnabledApplyButton()
                    }

    private fun getEnabledApplyButton(): Boolean {
        val checkStampControlValue = checkStampControl.value ?: false
        val checkBoxStampListValue = checkBoxStampList.value ?: false
        val acceptTotalCountValue = acceptTotalCount.value ?: 0.0

        return (enteredCountInBlockUnits > 0.0 || (acceptTotalCountValue > 0.0 && currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM))
                && (currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM
                || checkStampControlValue
                || checkBoxStampListValue)
    }

    val enabledBox: MutableLiveData<Boolean> = MutableLiveData(true)

    init {
        launchUITryCatch {
            initProduct()
        }
    }

    private suspend fun initProduct() {
        launchUITryCatch {
            productInfo.value
                    ?.let {
                        if (processMarkingBoxProductService.newProcessMarkingProductService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }.orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }

            searchProductDelegate.init(scanResultHandler = this@MarkingInfoBoxPGEViewModel::handleProductSearchResult)

            val paramGrzAlternMeinsCode = dataBase.getGrzAlternMeins().orEmpty()
            val uomInfo = ZmpUtz07V001(hyperHive).getUomInfo(paramGrzAlternMeinsCode)
            paramGrzAlternMeins.value = Uom(
                    code = uomInfo?.uom.orEmpty(),
                    name = uomInfo?.name.orEmpty()
            )

            qualityInfo.value = dataBase.getQualityInfo().orEmpty()
            spinQuality.value = qualityInfo.value?.map { it.name }.orEmpty()

            suffix.value = paramGrzAlternMeins.value?.name?.toLowerCase()


            val qualityInfoValue = qualityInfo.value.orEmpty()
            val allReasonRejectionInfo = dataBase.getAllReasonRejectionInfo()?.map { it.convertToQualityInfo() }.orEmpty()
            allTypeDiscrepancies.value = qualityInfoValue + allReasonRejectionInfo
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }


    fun getTitle(): String {
        return "${productInfo.value?.getMaterialLastSix().orEmpty()} ${productInfo.value?.description.orEmpty()}"
    }


    fun initProduct(initProduct: TaskProductInfo) {
        productInfo.value = initProduct
        processMarkingBoxProductService.initProduct(initProduct)
    }

    fun onClickBoxes() {
        if (processMarkingBoxProductService.isOverLimit(enteredCountInBlockUnits)) {
            screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen()
            return
        }
    }

    fun onClickDetails() {
        productInfo.value?.let { screenNavigator.openMarkingGoodsDetailsScreen(it) }
    }

    private fun addInfo(): Boolean {
        return if (currentTypeDiscrepanciesCode.isNotEmpty()) {

            with(processMarkingBoxProductService) {
                if (currentTypeDiscrepanciesCode != TYPE_DISCREPANCIES_QUALITY_NORM
                        && checkBoxStampListVisibility.value == false) {//выбрана категория брака и в поле Список марок отображается Не требуется
                    //сохраняем все необработанные блоки с текущей категорией брака
                    addAllUntreatedBlocksAsDefect(currentTypeDiscrepanciesCode)
                }
                addProduct(enteredCountInBlockUnits.toStringFormatted(), currentTypeDiscrepanciesCode)
                filterAndUpdateBlockDiscrepansies()
            }

            if (currentTypeDiscrepanciesCode != TYPE_DISCREPANCIES_QUALITY_NORM) {
                processMarkingBoxProductService.clearModifications()
                //обнуляем кол-во отсканированных марок (блок/gtin)
                countScannedStamps.value = 0
                //обнуляем кол-во отсканированных блоков
                countScannedBoxes.value = 0
            }

            spinQualitySelectedPosition.value = 0
            count.value = "0"
            true
        } else {
            false
        }
    }

    fun onClickApply() {
        val isAll = if (processMarkingBoxProductService.isOverLimit(enteredCountInBlockUnits)) {
                    screenNavigator.openAlertOverLimitPlannedScreen()
                    false
                } else {
                    addInfo()
                }
        if (isAll) screenNavigator.goBack()
    }

    fun onScanResult(data: String) {
        val acceptTotalCountValue = acceptTotalCount.value ?: 0.0
        if (enteredCountInBlockUnits <= 0.0) {
            if (!(currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM && acceptTotalCountValue > 0.0)) {
                screenNavigator.openAlertMustEnterQuantityInfoGreenScreen()
                return
            }
        }
        checkScanResult(data)
    }

    private fun checkScanResult(data: String) {
        if (barcodeBlockCheck(data)) {
            val barcode = barcodeStamp(data)
            stampInTask(barcode)
        } else {
            when (data.length) {
                in 18..20 -> boxCheck(data)
                in 0..18 -> stampInTask(data)
                else -> screenNavigator.openAlertInvalidBarcodeFormatScreen()
            }
        }
    }

    private fun stampInTask(barcode: String) {
        val stamp = taskRepository?.getExciseStamps()?.findExciseStampsOfProduct(productMaterialNumber)?.findLast { it.code == barcode }
        if (stamp != null) {  //Марка есть в задании
            checkStampCurrentProduct(stamp, barcode)
        } else {  // марки нет в задании
            getInfo(barcode, productMaterialNumber)
        }
    }

    private fun checkStampCurrentProduct(stamp: TaskExciseStampInfo, barcode: String) {
        if (stamp.materialNumber == productMaterialNumber) { //  марка соответствует текущему товару
            boxCheck(barcode)
        } else {
            screenNavigator.openAlertScannedStampBelongsAnotherProductScreen(
                    materialNumber = stamp.materialNumber.orEmpty(),
                    materialName = ZfmpUtz48V001(hyperHive).getProductInfoByMaterial(stamp.materialNumber.orEmpty())?.name.orEmpty()
            )
        }
    }


    private fun checkCountProcessed(barcode: String) {
        val countProcessed = taskRepository?.getBoxesDiscrepancies()?.getBoxesDiscrepancies()?.size
                ?: 0
        val count = countValue.value?.toInt() ?: 0
        if (countProcessed >= count) {   //обработанных >= чем в задании
            screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen()
        } else {
            checkBoxOrStamp(barcode)
        }

    }

    private fun checkBoxOrStamp(barcode: String) {
        val blockInfo = taskManager.getReceivingTask()?.getProcessedBlocks()?.findLast {
            it.blockNumber == barcode
        }
        val boxInfo = taskManager.getReceivingTask()?.getProcessedBoxes()?.findLast {
            it.boxNumber == barcode
        }

        if (blockInfo != null) {
            addStamp(
                    blockInfo = blockInfo,
                    typeDiscrepancies = currentTypeDiscrepanciesCode
            )
            //   screenNavigator.openBoxCard() ->  передаем № короба, в котором карточка
        }

        if (boxInfo != null) {
            //сохраняем короб
            addBox(boxInfo, currentTypeDiscrepanciesCode, barcode)
        }
    }

    private fun getInfo(stamp: String, materialNumberStamp: String) {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            zmpUtzGrz44V001NetRequest(ZmpUtzGrz44V001Params(
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber.orEmpty(),
                    stampCode = stamp,
                    materialNumber = materialNumberStamp,
                    blockCode = "",
                    boxCode = ""
            )).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(result: ZmpUtzGrz44V001Result) {
        launchUITryCatch {
            val indicatorOnePosition = result.indicatorOnePosition.toInt()
            val retCode = result.retCode
            val errorText = result.errorText
            if (indicatorOnePosition in 1..3) {  //Излишек
//                screenNavigator. https://trello.com/c/8esE7TKN
            } else if (retCode == 1) {
                Failure.SapError(errorText)
            }
        }
    }

    private fun barcodeStamp(data: String): String {
        val index = data.indexOf("92")
        return if (index in 31..38) {
            data.substring(0, index)
        } else {
            data
        }
    }

    private fun barcodeBlockCheck(data: String): Boolean {
        val regex = REGEX_BARCODE_BLOCK.toRegex()
        return regex.find(data) != null
    }


    private fun boxCheck(data: String) {

        val barcode = if (data.length == 20) {
            data.substring(2, 20)
        } else {
            data
        }
        val boxMaterialNumber = taskRepository?.getBoxesRepository()?.findBox(barcode)?.materialNumber.orEmpty()
        if (boxMaterialNumber.isNotEmpty()) { //короб числится в обработанных
            //   screenNavigator.openBoxCard() -> isScan = true //переход на карточку коробу, проставляя по марке признак IS_SCAN (если его не было)
        } else {
            val boxNumber = processMarkingBoxProductService.searchBoxDiscrepancies(data)?.materialNumber.orEmpty()
            if (boxNumber != boxMaterialNumber) {
                //Нет в задании Отсканированная коробка принадлежит товару <SAP-код> <Название>
                val materialName = ZfmpUtz48V001(hyperHive).getProductInfoByMaterial(boxNumber)?.name.orEmpty()
                screenNavigator.openAlertScannedBoxBelongsAnotherProductScreen(
                        materialNumber = boxNumber,
                        materialName = materialName
                )
                return
            }
            checkCountProcessed(data)
        }
    }


    private fun addBox(boxInfo: TaskBoxInfo, typeDiscrepancies: String, data: String) {
        boxInfo.let {
            typeDiscrepancies.let { currentTypeDiscrepancies ->
                processMarkingBoxProductService
                        .addBoxDiscrepancy(
                                boxNumber = data,
                                typeDiscrepancies = currentTypeDiscrepancies,
                                isScan = true,
                                isDenialOfFullProductAcceptance = false
                        )
                countScannedBoxes.value = countScannedBoxes.value?.plus(1)
            }
        }
    }


    private fun addStamp(blockInfo: TaskBlockInfo?, typeDiscrepancies: String?) {
        blockInfo?.let { currentBlock ->
            typeDiscrepancies?.let { currentTypeDiscrepancies ->
                processMarkingBoxProductService
                        .addBlockDiscrepancies(
                                blockInfo = currentBlock,
                                typeDiscrepancies = currentTypeDiscrepancies,
                                isScan = true,
                                isGtinControlPassed = false
                        )
                countScannedStamps.value = countScannedStamps.value?.plus(1)
            }
        }
    }


    fun onClickPositionSpinQuality(position: Int) {
        launchUITryCatch {
            spinQualitySelectedPosition.value = position
        }
    }

    fun onClickUnitChange() {
        suffix.value = if (isUnitBox.value == false) {
            paramGrzAlternMeins.value?.name?.toLowerCase().orEmpty()
        } else {
            isUnitBox.value = false
            productInfo.value?.purchaseOrderUnits?.name.orEmpty()
        }
        count.value = count.value
    }

    fun onBackPressed() {
        if (processMarkingBoxProductService.modifications() || count.value?.toString() != DEFAULT_COUNT) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        processMarkingBoxProductService.clearModifications()
                        screenNavigator.goBack()
                    }
            )
            return
        }
        screenNavigator.goBack()
    }

    companion object {
        private const val COUNT_PIECES_BOX = "1"
        private const val COUNT_PIECES_BOX_IS_ZERO = "0"
        private const val DEFAULT_COUNT = "0"
        private const val MULTIPLY = "x"
        const val REGEX_BARCODE_BLOCK = """^(?<barcode>01(?<gtin>\d{14})21(?<serial>\S{13})).?(?:240(?<tradeCode>\d{4}))?.?(?:91(?<verificationKey>\S{4}))?.?(?:92(?<verificationCode>\S{88}))?${'$'}"""
    }
}