package com.lenta.bp9.features.goods_information.marking.uom_st_with_counting_in_boxes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_information.base.BaseGoodsInfo
import com.lenta.bp9.features.goods_information.marking.TypeLastStampScanned
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessMarkingBoxProductService
import com.lenta.bp9.model.task.TaskBlockInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.shared.fmp.resources.dao_ext.getEansFromMaterial
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
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

//https://trello.com/c/vl9wQg0Y
class MarkingBoxInfoViewModel : BaseGoodsInfo(),  OnPositionClickListener {

    @Inject
    lateinit var processMarkingBoxProductService: ProcessMarkingBoxProductService

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var hyperHive: HyperHive

    val tvAccept: MutableLiveData<String> by lazy {
        paramGrzAlternMeins.map {
            val uomName = paramGrzAlternMeins.value?.name.orEmpty()
            val nestingInOneBlock = productInfo.value?.countPiecesBox?.toDouble().toStringFormatted()
            val productUomName = productInfo.value?.uom?.name.orEmpty()
            context.getString(R.string.accept, "$uomName=$nestingInOneBlock $productUomName")
        }
    }

    private val countScannedBlocks: MutableLiveData<Int> = MutableLiveData(0) //только блоки, переменная используется для отображения счетчика в поле Контроль марок
    private val countScannedStamps: MutableLiveData<Int> = MutableLiveData(0) //блоки и GTINы, переменная используется для кнопки Откатить

    val spinQualityEnabled: MutableLiveData<Boolean> = countScannedStamps.map { it == 0 }
    val spinReasonRejectionEnabled: MutableLiveData<Boolean> = countScannedStamps.map { it == 0 }
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val suffix: MutableLiveData<String> = MutableLiveData()
    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)


    private val allTypeDiscrepancies: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val paramGrzExclGtin: MutableLiveData<String> = MutableLiveData("")
    private val paramGrzAlternMeins: MutableLiveData<Uom> = MutableLiveData()
    private val unprocessedQuantityOfBlocks: MutableLiveData<Double> = MutableLiveData(0.0)

    val isVisibilityControlGTIN: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.isControlGTIN == true)
    }

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    val isUnitBox: MutableLiveData<Boolean> = MutableLiveData(true)

    private val enteredCountInBlockUnits: Double
        get() {
            var addNewCount = countValue.value?.toDouble() ?: 0.0
            val countPiecesBox =
                    productInfo.value
                            ?.countPiecesBox
                            ?.toDouble()
                            ?: 1.0

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
                                .takeIf { code -> code == TYPE_DISCREPANCIES_QUALITY_NORM }
                                ?.run { enteredCountInBlockUnits + countAcceptOfProduct }
                                ?: countAcceptOfProduct
                    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val acceptTotalCount = it ?: 0.0
        val purchaseOrderUnits = productInfo.value?.purchaseOrderUnits?.name.orEmpty()
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

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val refusalTotalCount = it ?: 0.0
        val purchaseOrderUnits = productInfo.value?.purchaseOrderUnits?.name.orEmpty()
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

    private fun getTvStampControlVal() : String {
        val acceptTotalCountVal = acceptTotalCount.value ?: 0.0
        val countBlockScanned =
                productInfo.value
                        ?.let { processMarkingBoxProductService.getCountProcessedBlockForDiscrepancies(TYPE_DISCREPANCIES_QUALITY_NORM).toDouble() }
                        ?: 0.0

        val numberStampsControl =
                productInfo.value
                        ?.numberStampsControl
                        ?.toDouble()
                        ?: 0.0

        return if (currentQualityInfoCode == TYPE_DISCREPANCIES_QUALITY_NORM) {
            if (numberStampsControl == 0.0 || acceptTotalCountVal <= 0.0) {
                checkStampControlVisibility.value = false
                context.getString(R.string.not_required)
            } else {
                checkStampControlVisibility.value = true
                val countStampsControl = if (acceptTotalCountVal < numberStampsControl) acceptTotalCountVal else numberStampsControl
                buildString {
                    append(countBlockScanned.toStringFormatted())
                    append(" ")
                    append(context.getString(R.string.of))
                    append(" ")
                    append(countStampsControl.toStringFormatted())
                }
            }
        } else {
            checkStampControlVisibility.value = false
            "" //это поле отображается только при выбранной категории "Норма"
        }
    }

    val checkStampControl: MutableLiveData<Boolean> = checkStampControlVisibility.map {
        val countBlockScanned = processMarkingBoxProductService.getCountProcessedBlockForDiscrepancies(TYPE_DISCREPANCIES_QUALITY_NORM).toDouble()
        val numberStampsControl = productInfo.value?.numberStampsControl?.toDouble() ?: 0.0
        countBlockScanned >= numberStampsControl
    }

    val checkBoxGtinControl: MutableLiveData<Boolean> = MutableLiveData(false)

    val checkBoxGtinStampControl: MutableLiveData<Boolean> = MutableLiveData(false)

    val tvMrcVal: MutableLiveData<String> by lazy {
        MutableLiveData(
                productInfo.value
                        ?.upLimitCondAmount
                        ?.toDoubleOrNull()
                        .toStringFormatted()
        )
    }

    val isVisibilityMRC: MutableLiveData<Boolean> by lazy {
        MutableLiveData((productInfo.value?.upLimitCondAmount?.toDoubleOrNull() ?: 0.0) > 0.0)
    }

    val checkBoxStampListVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvStampListVal: MutableLiveData<String> = refusalTotalCount
            .combineLatest(spinQualitySelectedPosition)
            .combineLatest(spinReasonRejectionSelectedPosition)
            .combineLatest(countScannedBlocks)
            .map {
                getTvStampListVal()

            }

    private fun getTvStampListVal() : String {
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
                    if (currentTypeDiscrepanciesCode != TYPE_DISCREPANCIES_QUALITY_NORM) {
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

    private fun getEnabledApplyButton() : Boolean {
        val checkStampControlValue = checkStampControl.value ?: false
        val checkBoxStampListValue = checkBoxStampList.value ?: false
        val acceptTotalCountValue = acceptTotalCount.value ?: 0.0

        return (enteredCountInBlockUnits > 0.0 || (acceptTotalCountValue > 0.0 && currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM))
                && (currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM
                || checkStampControlValue
                || checkBoxStampListValue)
    }

    val enabledRollbackBtn: MutableLiveData<Boolean> = countScannedStamps.map { (it ?: 0) > 0 }

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

            searchProductDelegate.init(scanResultHandler = this@MarkingBoxInfoViewModel::handleProductSearchResult)

            val paramGrzAlternMeinsCode = dataBase.getGrzAlternMeins().orEmpty()
            val uomInfo = ZmpUtz07V001(hyperHive).getUomInfo(paramGrzAlternMeinsCode)
            paramGrzAlternMeins.value = Uom(
                    code = uomInfo?.uom.orEmpty(),
                    name = uomInfo?.name.orEmpty()
            )

            qualityInfo.value = dataBase.getQualityInfo().orEmpty()
            spinQuality.value = qualityInfo.value?.map { it.name }.orEmpty()

            suffix.value = paramGrzAlternMeins.value?.name.orEmpty()
            paramGrzExclGtin.value = dataBase.getGrzExclGtin().orEmpty()

            val qualityInfoValue = qualityInfo.value.orEmpty()
            val allReasonRejectionInfo = dataBase.getAllReasonRejectionInfo()?.map { it.convertToQualityInfo() }.orEmpty()
            allTypeDiscrepancies.value = qualityInfoValue + allReasonRejectionInfo
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    //https://bitbucket.org/eigenmethodlentatempteam/lenta-pdct-android/pull-requests/534/grz_features_6037/diff
    fun getTitle() : String {
        return "${productInfo.value?.getMaterialLastSix().orEmpty()} ${productInfo.value?.description.orEmpty()}"
    }

    //https://bitbucket.org/eigenmethodlentatempteam/lenta-pdct-android/pull-requests/534/grz_features_6037/diff
    fun initProduct(initProduct: TaskProductInfo)  {
        productInfo.value = initProduct
        processMarkingBoxProductService.initProduct(initProduct)
    }

    fun onClickRollback() {
        //сначала проверяем, неотсканирован ли последним блок
        val lastScannedTypesStamps = processMarkingBoxProductService.getLastScannedTypesStamps()
        if(lastScannedTypesStamps == TypeLastStampScanned.BOX) {
            val countDelBlocksForBox = processMarkingBoxProductService.rollbackTypeLastStampScanned()
            //уменьшаем кол-во отсканированных блоков на кол-во удаленных в текущей сессии
            minusScannedBlocks(countDelBlocksForBox)
            //уменьшаем кол-во отсканированных марок (блок/gtin) на единицу в текущей сессии
            minusScannedStamps(1)
            return
        }

        processMarkingBoxProductService.rollbackTypeLastStampScanned()
        //уменьшаем кол-во отсканированных блоков на единицу в текущей сессии
        minusScannedBlocks()
        //уменьшаем кол-во отсканированных марок (блок/gtin) на единицу в текущей сессии
        minusScannedStamps(1)
    }

    private fun rollbackControlGtin() {
        val checkBoxGtinControlValue = checkBoxGtinControl.value
        val checkBoxGtinStampControlValue = checkBoxGtinStampControl.value

        if (checkBoxGtinControlValue == false && checkBoxGtinStampControlValue == false) {
            /**Чек-боксы не установлены -
             * По кнопке удалять последний отсканированный блок.
             * Уменьшать количество в поле "Список марок",
             * "Контроль марок" на 1 блок. (Очищать этот блок и его GTIN)
             * */
            with(processMarkingBoxProductService) {
                //уменьшаем кол-во отсканированных блоков
                rollbackTypeLastStampScanned()
                //уменьшаем кол-во отсканированных GTIN
                rollbackTypeLastStampScanned()
            }
            //уменьшаем кол-во отсканированных марок (блок/gtin) на два (один блок и один gtin) в текущей сессии
            minusScannedStamps(2)
            //уменьшаем кол-во отсканированных блоков на единицу в текущей сессии
            minusScannedBlocks()
            return
        }

        processMarkingBoxProductService.rollbackTypeLastStampScanned()
        if (checkBoxGtinControlValue == true) {
            /**Установлен чек-бокс GTIN (первый чек-бокс) -
             * удалять отсканированный GTIN из локальной стуктуры,
             * очищать активный чек-бокс для GTIN
             * */
            checkBoxGtinControl.value = false
            //уменьшаем кол-во отсканированных марок (блок/gtin) на единицу в текущей сессии
            minusScannedStamps(1)
        }

        if (checkBoxGtinStampControlValue == true) {
            /**Установлен чек-бокс марки (второй чек-бокс) -
             * удалять отсканированный блок из локальной структуры,
             * очищать активный чек-бокс для марки
             * */
            checkBoxGtinStampControl.value = false
            //уменьшаем кол-во отсканированных блоков на единицу в текущей сессии
            minusScannedBlocks()
            //уменьшаем кол-во отсканированных марок (блок/gtin) на единицу в текущей сессии
            minusScannedStamps(1)
        }
    }

    private fun minusScannedStamps(count: Int) {
        val countScannedStampsValue = countScannedStamps.value ?: 0
        if (countScannedStampsValue >= count) {
            countScannedStamps.value = countScannedStamps.value?.minus(count)
        }
    }

    private fun minusScannedBlocks(count: Int = 1) {
        val countScannedBlocksValue = countScannedBlocks.value ?: 0
        if (countScannedBlocksValue >= count) {
            countScannedBlocks.value = countScannedBlocks.value?.minus(1)
        }
    }

    fun onClickDetails() {
        productInfo.value?.let { screenNavigator.openMarkingGoodsDetailsScreen(it) }
    }

    private fun clearControl(checkBoxGtinControlValue: Boolean, checkBoxGtinStampControl: Boolean) {
        val lastScannedBlock = processMarkingBoxProductService.getLastScannedBlock()
        val lastScannedGtin = processMarkingBoxProductService.getLastScannedGtin()
        processMarkingBoxProductService.clearModifications()
        if (checkBoxGtinControlValue) {
            lastScannedGtin?.let {
                addGtin(lastScannedGtin)
            }
            //обнуляем кол-во отсканированных блоков
            countScannedBlocks.value = 0
        } else if (checkBoxGtinStampControl) {
            addBlock(
                    blockInfo = lastScannedBlock,
                    typeDiscrepancies = TYPE_DISCREPANCIES_QUALITY_NORM,
                    isGtinControlPassed = false
            )
            //оставляем кол-во отсканированных блоков равное 1
            countScannedBlocks.value = 1
        }

        //оставляем кол-во отсканированных марок (блок/gtin) равное 1
        countScannedStamps.value = 1
    }

    private fun addInfo(): Boolean {
        return if (currentTypeDiscrepanciesCode.isNotEmpty()) {
            val checkBoxGtinControlValue = checkBoxGtinControl.value ?: false
            val checkBoxGtinStampControl = checkBoxGtinStampControl.value ?: false

            with(processMarkingBoxProductService) {
                if (currentTypeDiscrepanciesCode != TYPE_DISCREPANCIES_QUALITY_NORM
                        && checkBoxStampListVisibility.value == false) {//выбрана категория брака и в поле Список марок отображается Не требуется
                    //сохраняем все необработанные блоки с текущей категорией брака
                    addAllUntreatedBlocksAsDefect(currentTypeDiscrepanciesCode)
                }
                addProduct(enteredCountInBlockUnits.toStringFormatted(), currentTypeDiscrepanciesCode)
                apply()
            }

            if (currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM
                    && (checkBoxGtinControlValue || checkBoxGtinStampControl)) {
                clearControl(checkBoxGtinControlValue, checkBoxGtinStampControl)
            } else {
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

    fun onClickAdd(): Boolean {
        return if (processMarkingBoxProductService.isOverLimit(enteredCountInBlockUnits)) {
            screenNavigator.openAlertOverLimitPlannedScreen()
            false
        } else {
            addInfo()
        }
    }

    fun onClickApply() {
        if (onClickAdd()) screenNavigator.goBack()
    }

    //https://trello.com/c/vl9wQg0Y https://trello.com/c/N6t51jru https://trello.com/c/vl9wQg0Y
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
        when (data.length) {
            //Отсканирован неверный формат ШК
            in 0..7, in 9..11, in 15..20 -> screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
            //GTIN https://trello.com/c/y2ECoCw4
            8, in 12..14 -> {
                if (currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM) {
                    gtinScannedCheck(data.padStart(14, '0'))
                }
            }
            //коробка, переходить к проверки коробки
            in 21..28 -> boxCheck(data)
            //пачка
            29 -> {
                if (barcodePackCheck(data)) {
                    //Отсканирован недопустимый код для текущего режима
                    screenNavigator.openAlertInvalidCodeScannedForCurrentModeScreen()
                } else {
                    //считать коробкой и переходить к проверки коробки
                    boxCheck(data)
                }
            }
            //Блок
            in 30..44 -> {
                if (barcodeBlockCheck(data)) {
                    blockCheck(data.substring(0, 25)) //обрезаем криптохвост
                } else {
                    //считать коробкой и переходить к проверки коробки
                    boxCheck(data)
                }
            }
            //Блок
            else -> { //блок. отсканировано более 44 символов https://trello.com/c/vl9wQg0Y
                if (barcodeBlockCheck(data)) {
                    blockCheck(data.substring(0, 25)) //обрезаем криптохвост
                } else {
                    //Отсканирован неверный формат ШК
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
            if (productInfo.value?.isGrayZone == true) {
                //todo Если установлен Леша сказал пока не делать...
                /**
                Но вообще нужно вызывать 44 рест
                ZMP_UTZ_GRZ_44_V001
                на вход заполнять:
                iv_task_num   = iv_task_num (номер задания)
                iv_box_num    = iv_box_num
                iv_pack_num   = iv_pack_num (номер блока)
                iv_mark_num   = iv_mark_num
                iv_matnr      = iv_matnr (номер товара)
                и если в этом ресте - пришел ev_stat=5
                переходить далее*/
            } else {
                //Отсканированная марка не числится в текущей поставке. Верните отсканированную марку обратно поставщику
                screenNavigator.openAlertStampNotFoundReturnSupplierScreen(::callbackFuncAlertStampNotFoundReturnSupplierScreen)
                return
            }
        }

        //https://trello.com/c/N6t51jru далее все пункт 4
        if (blockInfo?.materialNumber != productInfo.value?.materialNumber) {
            //Отсканированная марка принадлежит товару <SAP-код> <Название>"
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
        if (productInfo.value?.isControlGTIN == true
                && currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM) {
            gtinControlValueCheck(
                    stampCode = stampCode,
                    blockInfo = blockInfo,
                    typeDiscrepancies = currentTypeDiscrepanciesCode
            )
        } else {
            //этот блок считается обработанным, т.к. здесь нету контроля gtin, сохраняем его для erp
            addBlock(
                    blockInfo = blockInfo,
                    typeDiscrepancies = currentTypeDiscrepanciesCode,
                    isGtinControlPassed = true
            )
            //обновляем кол-во отсканированных марок/блоков для отображения на экране в поле «Контроль марок»
            countScannedBlocks.value = countScannedBlocks.value?.plus(1)
        }
    }

    private fun callbackFuncAlertStampNotFoundReturnSupplierScreen() {
        if (currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM) {
            count.value?.let {
                if (it.toDouble() > 0.0) {
                    count.value = count.value?.toDouble()?.minus(1)?.toStringFormatted()
                    //Количество нормы будет уменьшено
                    screenNavigator.openAlertAmountNormWillBeReducedMarkingScreen()
                }
            }
        }
    }

    private fun addBlock(blockInfo: TaskBlockInfo?, typeDiscrepancies: String?, isGtinControlPassed: Boolean) {
        blockInfo?.let { currentBlock ->
            typeDiscrepancies?.let { currentTypeDiscrepancies ->
                processMarkingBoxProductService
                        .addBlockDiscrepancies(
                                blockInfo = currentBlock,
                                typeDiscrepancies = currentTypeDiscrepancies,
                                isScan = true,
                                isGtinControlPassed = isGtinControlPassed
                        )
                countScannedStamps.value = countScannedStamps.value?.plus(1)
            }
        }
    }

    private fun addGtin(gtinCode: String) {
        processMarkingBoxProductService.addGtin(gtinCode)
        countScannedStamps.value = countScannedStamps.value?.plus(1)
    }

    private fun gtinControlValueCheck(stampCode: String, blockInfo: TaskBlockInfo?, typeDiscrepancies: String?) {
        if (checkBoxGtinStampControl.value == true) {
            //выводить ошибку «Отсканируйте GTIN товара»
            screenNavigator.openAlertScanProductGtinScreen()
            return
        }

        if (checkBoxGtinControl.value == false) {
            notCheckBoxGtinControl(stampCode, blockInfo, typeDiscrepancies)
            return
        }

        //выполнять проверки, описанные в разделе 2. MARK.ППП. Логика сверки GTIN. https://trello.com/c/y2ECoCw4
        blockInfo?.let { currentBlock ->
            typeDiscrepancies?.let { currentTypeDiscrepancies ->
                gtinControlCheck(currentBlock, currentTypeDiscrepancies)
            }
        }
    }

    private fun notCheckBoxGtinControl(stampCode: String, blockInfo: TaskBlockInfo?, typeDiscrepancies: String?) {
        val blockBarcode = stampCode.substring(0, 2)
        val paramGrzExclGtinValue = "0${paramGrzExclGtin.value}"
        if (blockBarcode == paramGrzExclGtinValue) {
            addBoxGtinControl(stampCode, blockInfo, typeDiscrepancies)
        } else {
            checkBoxGtinStampControl.value = true
            //этот блок считается НЕ обработанным, помечаем его как не прошедшего контроль GTIN, и на экране в поле «Контроль марок» кол-во не обновляем, т.е. countScannedBlocks не увеличиваем на единицу
            addBlock(
                    blockInfo = blockInfo,
                    typeDiscrepancies = typeDiscrepancies,
                    isGtinControlPassed = false
            )
        }
    }

    private fun addBoxGtinControl(stampCode: String, blockInfo: TaskBlockInfo?, typeDiscrepancies: String?) {
        checkBoxGtinControl.value = true //аналитики сказали сначала поставить чекбокс, а потом снять
        checkBoxGtinStampControl.value = true //аналитики сказали сначала поставить чекбокс, а потом снять
        val gtinCode = stampCode.substring(2, 16)
        addGtin(gtinCode)
        //этот блок считается обработанным, сохраняем его для erp
        addBlock(
                blockInfo = blockInfo,
                typeDiscrepancies = typeDiscrepancies,
                isGtinControlPassed = true
        )
        checkBoxGtinControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять
        checkBoxGtinStampControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять
        //обновляем кол-во отсканированных марок/блоков для отображения на экране в поле «Контроль марок»
        countScannedBlocks.value = countScannedBlocks.value?.plus(1)
    }

    //https://trello.com/c/y2ECoCw4
    private fun gtinScannedCheck(gtinCode: String) {
        if (checkBoxGtinControl.value == true) {
            //выводить ошибку «Отсканируйте марку товара»
            screenNavigator.openAlertScanProductBarcodeScreen()
            return
        }

        val eanInfo =
                ZmpUtz25V001(hyperHive)
                        .getEansFromMaterial(material = productInfo.value?.materialNumber.orEmpty())
                        .asSequence()
                        .map { eans ->
                            eans.ean?.padStart(14, '0')
                        }
                        .findLast { ean ->
                            ean == gtinCode
                        }
                        .orEmpty()

        if (eanInfo.isEmpty()) {
            //GTIN не соответствует товару
            screenNavigator.openAlertGtinDoesNotMatchProductScreen()
            return
        }

        if (checkBoxGtinStampControl.value == true) {
            checkGtinLastGtin(gtinCode)
        } else {
            checkBoxGtinControl.value = true
            addGtin(gtinCode)
        }

    }

    private fun checkGtinLastGtin(gtinCode: String) {
        val lastScannedBlockInfo = processMarkingBoxProductService.getLastScannedBlock()
        val lastScannedGtin = lastScannedBlockInfo?.blockNumber?.substring(2, 16).orEmpty()
        if (gtinCode == lastScannedGtin) {
            lastScannedBlockInfo?.let { blockInfo -> processGtinBy(gtinCode, blockInfo) }
        } else {
            screenNavigator.openAlertDisparityGTINScreen()
        }
    }

    private fun processGtinBy(gtinCode: String, blockInfo: TaskBlockInfo) {
        checkBoxGtinControl.value = true //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
        //код по сохранению блока для передачи в erp, т.к. он считается обработанным
        processMarkingBoxProductService.markPassageControlBlock(blockInfo.blockNumber)
        addGtin(gtinCode)
        checkBoxGtinControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
        checkBoxGtinStampControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
        //обновляем кол-во отсканированных марок/блоков для отображения на экране в поле «Контроль марок»
        countScannedBlocks.value = countScannedBlocks.value?.plus(1)
    }

    //https://trello.com/c/y2ECoCw4
    private fun gtinControlCheck(blockInfo: TaskBlockInfo, typeDiscrepancies: String) {
        val gtinCode = blockInfo.blockNumber.substring(2, 16)
        if (blockInfo.blockNumber.substring(0, 2) == "0${paramGrzExclGtin.value}") {
            addBoxGtinControlCheck(gtinCode, blockInfo, typeDiscrepancies)
            return
        }

        val lastScannedGtin = processMarkingBoxProductService.getLastScannedGtin().orEmpty()
        if (lastScannedGtin == gtinCode) {
            gtinCodeLastScannedGtin(blockInfo, typeDiscrepancies)
        } else {
            //выводить ошибку «GTIN в марке не соответствует GTIN товара, оформите брак по данному товару»
            screenNavigator.openAlertDisparityGTINScreen()
        }
    }

    private fun addBoxGtinControlCheck(gtinCode: String, blockInfo: TaskBlockInfo?, typeDiscrepancies: String?) {
        checkBoxGtinControl.value = true //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
        checkBoxGtinStampControl.value = true //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
        processMarkingBoxProductService.replaceLastGtin(gtinCode)
        //этот блок считается обработанным, сохраняем его для erp
        addBlock(
                blockInfo = blockInfo,
                typeDiscrepancies = typeDiscrepancies,
                isGtinControlPassed = true
        )
        checkBoxGtinControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
        checkBoxGtinStampControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
        //обновляем кол-во отсканированных марок/блоков для отображения на экране в поле «Контроль марок»
        countScannedBlocks.value = countScannedBlocks.value?.plus(1)
    }

    private fun gtinCodeLastScannedGtin(blockInfo: TaskBlockInfo?, typeDiscrepancies: String?) {
        checkBoxGtinStampControl.value = true //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
        //этот блок считается обработанным, сохраняем его для erp
        addBlock(
                blockInfo = blockInfo,
                typeDiscrepancies = typeDiscrepancies,
                isGtinControlPassed = true
        )
        checkBoxGtinControl.value = false
        checkBoxGtinStampControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
        //обновляем кол-во отсканированных марок/блоков для отображения на экране в поле «Контроль марок»
        countScannedBlocks.value = countScannedBlocks.value?.plus(1)
    }

    private fun boxCheck(boxNumber: String) {
        val boxMaterialNumber =
                taskRepository
                        ?.getBoxesRepository()
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

        if (checkBoxGtinControl.value == true
                || checkBoxGtinStampControl.value == true) {
            //удаляем ранее отсканированный gtin или блок
            onClickRollback()
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
            spinReasonRejection.value = reasonRejectionInfo.value
                    ?.map {
                        it.name
                    }
                    .orEmpty()
            count.value = count.value
            screenNavigator.hideProgress()
        }
    }

    fun onClickUnitChange() {
        isUnitBox.value?.let { isUnitBox.value = !it }
        suffix.value =
                isUnitBox.value
                        ?.takeIf { it }
                        ?.run { paramGrzAlternMeins.value?.name.orEmpty() }
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
