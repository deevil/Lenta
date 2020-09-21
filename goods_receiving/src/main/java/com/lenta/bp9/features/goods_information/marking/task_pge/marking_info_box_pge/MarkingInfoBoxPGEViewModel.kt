package com.lenta.bp9.features.goods_information.marking.task_pge.marking_info_box_pge

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.features.goods_information.base.BaseGoodsInfo
import com.lenta.bp9.features.goods_information.marking.TypeLastStampScanned
import com.lenta.bp9.model.processing.ProcessMarkingBoxPGEProductService
import com.lenta.bp9.model.task.TaskBlockInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class MarkingInfoBoxPGEViewModel : BaseGoodsInfo(), OnPositionClickListener {

    @Inject
    lateinit var processMarkingBoxProductService: ProcessMarkingBoxPGEProductService

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var hyperHive: HyperHive

    val tvAccept: MutableLiveData<String> by lazy {
        paramGrzAlternMeins.map {
            val uomName = paramGrzAlternMeins.value?.name?.toLowerCase()

            var nestingInOneBlock = productInfo.value?.countPiecesBox?.toDouble().toStringFormatted()
            if (nestingInOneBlock == "0") {
                nestingInOneBlock = "1"
            }
            val productUomName = productInfo.value?.uom?.name.orEmpty()
            context.getString(R.string.accept, "$uomName=$nestingInOneBlock $productUomName")
        }
    }

    private val countScannedBlocks: MutableLiveData<Int> = MutableLiveData(0) //только блоки, переменная используется для отображения счетчика в поле Контроль марок
    private val countScannedStamps: MutableLiveData<Int> = MutableLiveData(0) //блоки и GTINы, переменная используется для кнопки Откатить

    val spinQualityEnabled: MutableLiveData<Boolean> = countScannedStamps.map { it == 0 }
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
        get() { var addNewCount = countValue.value?.toDouble() ?: 0.0

            var countPiecesBox =
                    productInfo.value
                            ?.countPiecesBox
                            ?.toDouble()
                            ?: 1.0
            if (countPiecesBox == 0.0 ) countPiecesBox = 1.0

            isUnitBox.value
                    ?.takeIf { it }
                    ?.let { addNewCount *= countPiecesBox }

            return addNewCount
        }

    val acceptTotalCount: MutableLiveData<Double> =
            isUnitBox
                    .combineLatest(countValue)
                    .combineLatest(spinQualitySelectedPosition)
                    .map {
                        currentQualityInfoCode
                                .takeIf { code -> code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM }
                               ?.run { enteredCountInBlockUnits + countAcceptOfProduct }
                                ?: countAcceptOfProduct
                    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val acceptTotalCount = it ?: 0.0
        val purchaseOrderUnits =  productInfo.value?.uom?.name.orEmpty()
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
                                .takeIf { code -> code != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM }
                                ?.run { enteredCountInBlockUnits + countRefusalOfProduct }
                                ?: countRefusalOfProduct
                    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val refusalTotalCount = it ?: 0.0
        val purchaseOrderUnits = productInfo.value?.uom?.name.orEmpty()
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

    val checkStampControlVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvStampControlVal: MutableLiveData<String> = acceptTotalCount
            .combineLatest(spinQualitySelectedPosition)
            .combineLatest(countScannedBlocks)
            .map {
                getTvStampControlVal()
            }

    private fun getTvStampControlVal(): String {
        val acceptTotalCountVal = acceptTotalCount.value ?: 0.0
        val countBlockScanned =
                productInfo.value
                        ?.let { processMarkingBoxProductService.getCountProcessedBlockForDiscrepancies(TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM).toDouble() }
                        ?: 0.0

        val numberStampsControl =
                productInfo.value
                        ?.numberStampsControl
                        ?.toDouble()
                        ?: 0.0

        return if (currentQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
            if (numberStampsControl == 0.0 || acceptTotalCountVal <= 0.0) {
                checkStampControlVisibility.value = false
                context.getString(R.string.not_required)
            } else {
                checkStampControlVisibility.value = true
                val countStampsControl = if (acceptTotalCountVal < numberStampsControl) acceptTotalCountVal else numberStampsControl
                buildString {
                    append(countBlockScanned.toStringFormatted())
                    append(" ")
                    append(paramGrzAlternMeins.value?.name?.toLowerCase())
                    append(" ")
                    append("x")
                    append(" ")
                    append(countStampsControl.toInt())
                }
            }
        } else {
            checkStampControlVisibility.value = false
            "" //это поле отображается только при выбранной категории, кроме "Норма"
        }
    }

    val checkStampControl: MutableLiveData<Boolean> = checkStampControlVisibility.map {
        val countBlockScanned = processMarkingBoxProductService.getCountProcessedBlockForDiscrepancies(TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM).toDouble()
        val numberStampsControl = productInfo.value?.numberStampsControl?.toDouble() ?: 0.0
        countBlockScanned >= numberStampsControl
    }


    val checkBoxStampListVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvStampListVal: MutableLiveData<String> = refusalTotalCount
            .combineLatest(spinQualitySelectedPosition)
            .combineLatest(spinReasonRejectionSelectedPosition)
            .combineLatest(countScannedBlocks)
            .map {
                getTvStampListVal()
            }

    private fun getTvStampListVal(): String {
        return productInfo.value
                ?.let { product ->
                    val countBlockScannedValue = countScannedBlocks.value ?: 0
                    val totalBlocksProduct = product.origQuantity.toDouble()
                    val countProcessedBlocksCurrentDiscrepancies = processMarkingBoxProductService.getTotalScannedBlocks()
                    if (countBlockScannedValue <= 0) { //фиксируем необработанное количество после первого сканирования марок, чтобы не учитывать их в текущей сессии, иначе это кол-во будет уменьшаться и появиться текст Не требуется
                        unprocessedQuantityOfBlocks.value = totalBlocksProduct - countProcessedBlocksCurrentDiscrepancies
                    }
                    val unprocessedQuantityOfBlocksVal = unprocessedQuantityOfBlocks.value
                            ?: 0.0
                    if (currentTypeDiscrepanciesCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                        if (unprocessedQuantityOfBlocksVal == enteredCountInBlockUnits) {
                            checkBoxStampListVisibility.value = false
                            context.getString(R.string.not_required)
                        } else {
                            checkBoxStampListVisibility.value = true
                            buildString {
                                append(countBlockScannedValue.toDouble().toStringFormatted())
                                append(" ")
                                append(context.getString(R.string.of))
                                append(" ")
                                append(enteredCountInBlockUnits.toStringFormatted())
                            }
                        }
                    } else {
                        checkBoxStampListVisibility.value = false
                        "" //это поле отображается только при выбранной категории брака
                    }
                }.orEmpty()
    }


    val checkBoxStampList: MutableLiveData<Boolean> = checkBoxStampListVisibility.map {
        val countBlockScanned = countScannedBlocks.value ?: 0
        (currentTypeDiscrepanciesCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM && it == false)
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

        return (enteredCountInBlockUnits > 0.0 || (acceptTotalCountValue > 0.0 && currentTypeDiscrepanciesCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM))
                && (currentTypeDiscrepanciesCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                || checkStampControlValue
                || checkBoxStampListValue)
    }

    val enabledBox: MutableLiveData<Boolean> = MutableLiveData(true)

    init {
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

            searchProductDelegate.init(viewModelScope = this@MarkingInfoBoxPGEViewModel::viewModelScope,
                    scanResultHandler = this@MarkingInfoBoxPGEViewModel::handleProductSearchResult)

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
        if (processMarkingBoxProductService.isOverLimit(enteredCountInBlockUnits)){
//            screenNavigator.openAlertMustEnterQuantityScreen()
            return
        }
    }

    fun onClickDetails() {
        productInfo.value?.let { screenNavigator.openMarkingGoodsDetailsScreen(it) }
    }

    private fun addInfo(): Boolean {
        return if (currentTypeDiscrepanciesCode.isNotEmpty()) {

            with(processMarkingBoxProductService) {
                if (currentTypeDiscrepanciesCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                        && checkBoxStampListVisibility.value == false) {//выбрана категория брака и в поле Список марок отображается Не требуется
                    //сохраняем все необработанные блоки с текущей категорией брака
                    addAllUntreatedBlocksAsDefect(currentTypeDiscrepanciesCode)
                }
                addProduct(enteredCountInBlockUnits.toStringFormatted(), currentTypeDiscrepanciesCode)
                apply()
            }

            if (currentTypeDiscrepanciesCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                processMarkingBoxProductService.clearModifications()
                //обнуляем кол-во отсканированных марок (блок/gtin)
                countScannedStamps.value = 0
                //обнуляем кол-во отсканированных блоков
                countScannedBlocks.value = 0
            }

            spinQualitySelectedPosition.value = 0
            count.value = "0"
            true
        } else {
            false
        }
    }

    fun onClickApply() {
        if (processMarkingBoxProductService.isOverLimit(enteredCountInBlockUnits)) screenNavigator.goBack()
    }

    fun onScanResult(data: String) {
        val acceptTotalCountValue = acceptTotalCount.value ?: 0.0
        if (enteredCountInBlockUnits <= 0.0) {
            if (!(currentTypeDiscrepanciesCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM && acceptTotalCountValue > 0.0)) {
                screenNavigator.openAlertMustEnterQuantityInfoGreenScreen()
                return
            }
        }

        checkScanResult(data)
    }

    private fun checkScanResult(data: String) {
        when (data.length) {
            in 0..7, in 9..11, in 15..20 -> screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
            8, in 12..14 -> { }
            in 21..28 -> boxCheck(data)
            29 -> {
                if (barcodePackCheck(data)) {
                    screenNavigator.openAlertInvalidCodeScannedForCurrentModeScreen()
                } else {
                    boxCheck(data)
                }
            }
            in 30..44 -> {
                if (barcodeBlockCheck(data)) {
                    blockCheck(data.substring(0, 25))
                } else {
                    boxCheck(data)
                }
            }

            else -> {
                if (barcodeBlockCheck(data)) {
                    blockCheck(data.substring(0, 25))
                } else {
                    screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
                }
            }
        }
    }

    private fun barcodePackCheck(data: String): Boolean {
        val regex = REGEX_BARCODE_PACK.toRegex()
        return regex.find(data) != null
    }

    private fun barcodeBlockCheck(data: String): Boolean {
        val regex = REGEX_BARCODE_BLOCK.toRegex()
        return regex.find(data) != null
    }

    private fun blockCheck(stampCode: String) {
        val blockInfo = processMarkingBoxProductService.searchBlock(stampCode)

        if (processMarkingBoxProductService.blockIsAlreadyProcessed(stampCode)) {
            screenNavigator.openAlertScannedStampIsAlreadyProcessedAlternativeScreen() //Марка уже обработана
            return
        }

        if (blockInfo == null) {
            if (productInfo.value?.isGrayZone != true) {
                screenNavigator.openAlertStampNotFoundReturnSupplierScreen(::callbackFuncAlertStampNotFoundReturnSupplierScreen)
                return
            }
        }

        if (blockInfo?.materialNumber != productInfo.value?.materialNumber) {
            val blockMaterialNumber = blockInfo?.materialNumber.orEmpty()
            screenNavigator.openAlertScannedStampBelongsAnotherProductScreen(
                    materialNumber = blockMaterialNumber,
                    materialName = ZfmpUtz48V001(hyperHive).getProductInfoByMaterial(blockMaterialNumber)?.name.orEmpty()
            )
            return
        }

        blockProcessing(stampCode, blockInfo)
    }

    private fun blockProcessing(stampCode: String, blockInfo: TaskBlockInfo?) {
        //этот блок считается обработанным, т.к. здесь нету контроля gtin, сохраняем его для erp
        addBlock(
                blockInfo = blockInfo,
                typeDiscrepancies = currentTypeDiscrepanciesCode
        )
        //обновляем кол-во отсканированных марок/блоков для отображения на экране в поле «Контроль марок»
        countScannedBlocks.value = countScannedBlocks.value?.plus(1)
    }

    private fun callbackFuncAlertStampNotFoundReturnSupplierScreen() {
        if (currentTypeDiscrepanciesCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
            count.value?.let {
                if (it.toDouble() > 0.0) {
                    count.value = count.value?.toDouble()?.minus(1)?.toStringFormatted()
                    //Количество нормы будет уменьшено
                    screenNavigator.openAlertAmountNormWillBeReducedMarkingScreen()
                }
            }
        }
    }

    private fun addBlock(blockInfo: TaskBlockInfo?, typeDiscrepancies: String?) {
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


    private fun boxCheck(boxNumber: String) {
        val boxMaterialNumber =
                taskRepository
                        ?.getBoxes()
                        ?.findBox(boxNumber)
                        ?.materialNumber
                        .orEmpty()

        val productMaterialNumber = productInfo.value?.materialNumber.orEmpty()
        if (boxMaterialNumber.isEmpty()) {
            //Отсканированная коробка не числится в поставке. Отсканируйте все марки из этого короба
            screenNavigator.openMarkingBoxNotIncludedDeliveryScreen()
            return
        }

        if (boxMaterialNumber != productMaterialNumber) {
            //Отсканированная коробка принадлежит товару <SAP-код> <Название>
            val materialName = ZfmpUtz48V001(hyperHive).getProductInfoByMaterial(boxMaterialNumber)?.name.orEmpty()
            screenNavigator.openAlertScannedBoxBelongsAnotherProductScreen(
                    materialNumber = boxMaterialNumber,
                    materialName = materialName
            )
            return
        }

        val checkStampControlValue = checkStampControl.value ?: false
        if (!checkStampControlValue) {
            //Сначала произведите контроль нормы путем сканирования МАРОК, а затем добавляйте марки сканированием коробок.
            screenNavigator.openMarkingPerformRateControlScreen()
            return
        }

        val checkBlocksCategoriesDifferentCurrent = processMarkingBoxProductService.checkBlocksCategoriesDifferentCurrent(boxMaterialNumber, currentTypeDiscrepanciesCode)
        if (checkBlocksCategoriesDifferentCurrent) {
            //Марки данной коробки были заявлены в другую категорию, для добавления оставшихся марок в категорию <Текущая категория> необходимо отсканировать каждую оставшуюся марку.
            val typeDiscrepanciesName = getCurrentTypeDiscrepanciesName(currentTypeDiscrepanciesCode)
            screenNavigator.openMarkingBlockDeclaredDifferentCategoryScreen(typeDiscrepanciesName)
            return
        }


        //сохраняем короб и все оставшиеся неотсканированными блоки без is_scan
        val countAddBlocksForBox = processMarkingBoxProductService.addBoxDiscrepancy(
                boxNumber = boxNumber,
                typeDiscrepancies = currentTypeDiscrepanciesCode,
                isScan = true,
                isDenialOfFullProductAcceptance = false
        )
        //обновляем кол-во отсканированных блоков/марок для отображения на экране в поле «Контроль марок»
        countScannedBlocks.value = countScannedBlocks.value?.plus(countAddBlocksForBox)
        countScannedStamps.value = countScannedStamps.value?.plus(1)

    }

    private fun getCurrentTypeDiscrepanciesName(typeDiscrepancies: String): String {
        return allTypeDiscrepancies.value
                ?.findLast { it.code == typeDiscrepancies }
                ?.name
                .orEmpty()
    }

    override fun onClickPosition(position: Int) {
        spinReasonRejectionSelectedPosition.value = position
    }

    fun onClickPositionSpinQuality(position: Int) {
        launchUITryCatch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(currentQualityInfoCode)
        }
    }

    private suspend fun updateDataSpinReasonRejection(selectedQuality: String) {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData()
            spinReasonRejectionSelectedPosition.value = 0
            reasonRejectionInfo.value = dataBase.getReasonRejectionInfoOfQuality(selectedQuality)
            count.value = count.value
            screenNavigator.hideProgress()
        }
    }

    fun onClickUnitChange() {
        isUnitBox.value?.let { isUnitBox.value = !it }
        suffix.value =
                isUnitBox.value
                        ?.takeIf { it }
                        ?.run { paramGrzAlternMeins.value?.name?.toLowerCase() }
                        ?: productInfo.value
                                ?.purchaseOrderUnits
                                ?.name
                                .orEmpty()

        count.value = count.value
    }

    fun onBackPressed() {
        if (processMarkingBoxProductService.modifications()) {
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
        const val REGEX_BARCODE_PACK = """^(?<packBarcode>(?<gtin>\d{14})(?<serial>\S{7}))(?<MRC>\S{4})(?:\S{4})${'$'}"""
        const val REGEX_BARCODE_BLOCK = """^.?(?<blockBarcode>01(?<gtin2>\d{14})21(?<serial>\S{7})).?8005(?<MRC>\d{6}).?93(?<verificationKey>\S{4}).?(?<other>\S{1,})?${'$'}"""
    }
}