package com.lenta.bp9.features.goods_information.marking.uom_st_with_counting_in_boxes

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_information.marking.TypeLastStampScanned
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessMarkingBoxProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskBlockInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.fmp.resources.dao_ext.getEansFromMaterial
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.dao_ext.getUomInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz07V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz25V001
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

//https://trello.com/c/vl9wQg0Y
class MarkingBoxInfoViewModel : CoreViewModel(),
        OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processMarkingBoxProductService: ProcessMarkingBoxProductService

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var hyperHive: HyperHive

    private val taskRepository by lazy { taskManager.getReceivingTask()?.taskRepository }

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val tvAccept: MutableLiveData<String> by lazy {
        paramGrzAlternMeins.map {
            val uomName = paramGrzAlternMeins.value?.name.orEmpty()
            val nestingInOneBlock = productInfo.value?.nestingInOneBlock?.toDouble().toStringFormatted()
            val productUomName = productInfo.value?.uom?.name.orEmpty()
            context.getString(R.string.accept, "$uomName=$nestingInOneBlock $productUomName")
        }
    }

    private val countScannedBlocks: MutableLiveData<Int> = MutableLiveData(0) //только блоки, переменная используется для отображения счетчика в поле Контроль марок
    private val countScannedStamps: MutableLiveData<Int> = MutableLiveData(0) //блоки и GTINы, переменная используется для кнопки Откатить
    val spinQualityEnabled: MutableLiveData<Boolean> = countScannedStamps.map { it == 0 }
    val spinReasonRejectionEnabled: MutableLiveData<Boolean> = countScannedStamps.map { it == 0 }
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(-1)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map { it != 0 }
    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
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
                    ?.run { addNewCount *= countPiecesBox }

            return addNewCount
        }

    private val currentQualityInfoCode: String
        get() {
            val position = spinQualitySelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.run {
                        qualityInfo.value
                                ?.takeIf { it.isNotEmpty() }
                                ?.run { this[position].code }
                                .orEmpty()
                    }
                    .orEmpty()
        }

    private val currentReasonRejectionInfoCode: String
        get() {
            val position = spinReasonRejectionSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.run {
                        reasonRejectionInfo.value
                                ?.takeIf { it.isNotEmpty() }
                                ?.run { this[position].code }
                                .orEmpty()
                    }
                    .orEmpty()
        }

    private val currentTypeDiscrepanciesCode: String
        get() {
            return currentQualityInfoCode
                    .takeIf { it == TYPE_DISCREPANCIES_QUALITY_NORM }
                    ?: currentReasonRejectionInfoCode
        }

    private val countAcceptOfProduct: Double
        get() {
            return productInfo.value
                    ?.let { product ->
                        taskRepository
                                ?.getProductsDiscrepancies()
                                ?.getCountAcceptOfProduct(product)
                    }
                    ?: 0.0
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

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map { acceptTotalCountValue ->
        val currentAcceptTotalCount = acceptTotalCountValue ?: 0.0
        val purchaseOrderUnits = productInfo.value?.purchaseOrderUnits?.name.orEmpty()
        val totalCountAcceptOfProduct =
                countAcceptOfProduct
                        .takeIf { count -> count > 0.0 }
                        ?.run { "+ ${this.toStringFormatted()}" }
                        ?: countAcceptOfProduct.toStringFormatted()

        currentAcceptTotalCount
                .takeIf { count -> count > 0.0 }
                ?.run { "+ ${this.toStringFormatted()} $purchaseOrderUnits" }
                ?: "$totalCountAcceptOfProduct $purchaseOrderUnits"
    }

    private val countRefusalOfProduct: Double
        get() {
            return productInfo.value
                    ?.let { product ->
                        taskRepository
                                ?.getProductsDiscrepancies()
                                ?.getCountRefusalOfProduct(product)
                    }
                    ?: 0.0
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

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map { refusalTotalCountValue ->
        val currentRefusalTotalCount = refusalTotalCountValue ?: 0.0
        val purchaseOrderUnits = productInfo.value?.purchaseOrderUnits?.name.orEmpty()
        val totalCountRefusalOfProduct =
                countRefusalOfProduct
                        .takeIf { count -> count > 0.0 }
                        ?.run { "- ${this.toStringFormatted()}" }
                        ?: countRefusalOfProduct.toStringFormatted()

        currentRefusalTotalCount
                .takeIf { count -> count > 0.0 }
                ?.run { "- ${this.toStringFormatted()} $purchaseOrderUnits" }
                ?: "$totalCountRefusalOfProduct $purchaseOrderUnits"
    }

    val checkStampControlVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvStampControlVal: MutableLiveData<String> = acceptTotalCount
            .combineLatest(spinQualitySelectedPosition)
            .combineLatest(countScannedBlocks)
            .map {
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

                if (currentQualityInfoCode == TYPE_DISCREPANCIES_QUALITY_NORM) {
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
                productInfo.value
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
                        val checkStampControlValue = checkStampControl.value ?: false
                        val checkBoxStampListValue = checkBoxStampList.value ?: false
                        val acceptTotalCountValue = acceptTotalCount.value ?: 0.0

                        (enteredCountInBlockUnits > 0.0 || (acceptTotalCountValue > 0.0 && currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM))
                                && (currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM
                                || checkStampControlValue
                                || checkBoxStampListValue)
                    }

    val enabledRollbackBtn: MutableLiveData<Boolean> = countScannedStamps.map { (it ?: 0) > 0 }

    init {
        launchUITryCatch {
            productInfo.value
                    ?.let {
                        if (processMarkingBoxProductService.newProcessMarkingBoxProductService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }.orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }

            searchProductDelegate.init(viewModelScope = this@MarkingBoxInfoViewModel::viewModelScope,
                    scanResultHandler = this@MarkingBoxInfoViewModel::handleProductSearchResult)

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

    fun onClickRollback() {
        //сначала проверяем, неотсканирован ли последним блок
        val lastScannedTypesStamps = processMarkingBoxProductService.getLastScannedTypesStamps()
        lastScannedTypesStamps
                .takeIf { it == TypeLastStampScanned.BOX }
                ?.run {
                    val countDelBlocksForBox = processMarkingBoxProductService.rollbackTypeLastStampScanned()
                    //уменьшаем кол-во отсканированных блоков на кол-во удаленных в текущей сессии
                    minusScannedBlocks(countDelBlocksForBox)
                    //уменьшаем кол-во отсканированных марок (блок/gtin) на единицу в текущей сессии
                    minusScannedStamps(1)
                    return
                }

        if (productInfo.value?.isControlGTIN == true) {
            rollbackControlGtin()
            return
        }

        processMarkingBoxProductService.rollbackTypeLastStampScanned()
        //уменьшаем кол-во отсканированных блоков на единицу в текущей сессии
        minusScannedBlocks()
        //уменьшаем кол-во отсканированных марок (блок/gtin) на единицу в текущей сессии
        minusScannedStamps(1)
    }

    private fun rollbackControlGtin() {
        if (checkBoxGtinControl.value == false && checkBoxGtinStampControl.value == false) {
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
        if (checkBoxGtinControl.value == true) {
            /**Установлен чек-бокс GTIN (первый чек-бокс) -
             * удалять отсканированный GTIN из локальной стуктуры,
             * очищать активный чек-бокс для GTIN
             * */
            checkBoxGtinControl.value = false
            //уменьшаем кол-во отсканированных марок (блок/gtin) на единицу в текущей сессии
            minusScannedStamps(1)
        }

        if (checkBoxGtinStampControl.value == true) {
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
        countScannedStamps.value
                ?.takeIf { it >= count }
                ?.run { countScannedStamps.value = countScannedStamps.value?.minus(count) }
    }

    private fun minusScannedBlocks(count: Int = 1) {
        countScannedBlocks.value
                ?.takeIf { it >= count }
                ?.run { countScannedBlocks.value = countScannedBlocks.value?.minus(1) }
    }

    fun onClickDetails() {
        productInfo.value?.let {
            screenNavigator.openMarkingGoodsDetailsScreen(it)
        }
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
        }

        if (checkBoxGtinStampControl) {
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
        return if (processMarkingBoxProductService.overLimit(enteredCountInBlockUnits)) {
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
        val regex = Regex("""^(?<packBarcode>(?<gtin>\d{14})(?<serial>\S{7}))(?<MRC>\S{4})(?:\S{4})${'$'}""")
        return regex.find(data) != null
    }

    private fun barcodeBlockCheck(data: String): Boolean {
        val regex = Regex("""^.?(?<blockBarcode>01(?<gtin2>\d{14})21(?<serial>\S{7})).?8005(?<MRC>\d{6}).?93(?<verificationKey>\S{4}).?(?<other>\S{1,})?${'$'}""")
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
                screenNavigator.openAlertStampNotFoundReturnSupplierScreen(
                        backCallbackFunc = {
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
                )
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
            if (stampCode.substring(0, 2) == "0${paramGrzExclGtin.value}") {
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
            } else {
                checkBoxGtinStampControl.value = true
                //этот блок считается НЕ обработанным, помечаем его как не прошедшего контроль GTIN, и на экране в поле «Контроль марок» кол-во не обновляем, т.е. countScannedBlocks не увеличиваем на единицу
                addBlock(
                        blockInfo = blockInfo,
                        typeDiscrepancies = typeDiscrepancies,
                        isGtinControlPassed = false
                )
            }
            return
        }

        //выполнять проверки, описанные в разделе 2. MARK.ППП. Логика сверки GTIN. https://trello.com/c/y2ECoCw4
        blockInfo?.let { currentBlock ->
            typeDiscrepancies?.let { currentTypeDiscrepancies ->
                gtinControlCheck(currentBlock, currentTypeDiscrepancies)
            }
        }
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
                            eans.ean.padStart(14, '0')
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
            lastScannedBlockInfo?.let { blockInfo ->
                checkBoxGtinControl.value = true //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
                //код по сохранению блока для передачи в erp, т.к. он считается обработанным
                processMarkingBoxProductService.markPassageControlBlock(blockInfo.blockNumber)
                addGtin(gtinCode)
                checkBoxGtinControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
                checkBoxGtinStampControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
                //обновляем кол-во отсканированных марок/блоков для отображения на экране в поле «Контроль марок»
                countScannedBlocks.value = countScannedBlocks.value?.plus(1)
            }
        } else {
            screenNavigator.openAlertDisparityGTINScreen()
        }
    }

    //https://trello.com/c/y2ECoCw4
    private fun gtinControlCheck(blockInfo: TaskBlockInfo, typeDiscrepancies: String) {
        val gtinCode = blockInfo.blockNumber.substring(2, 16)
        if (blockInfo.blockNumber.substring(0, 2) == "0${paramGrzExclGtin.value}") {
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
            return
        }

        val lastScannedGtin = processMarkingBoxProductService.getLastScannedGtin().orEmpty()
        if (lastScannedGtin == gtinCode) {
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
        } else {
            //выводить ошибку «GTIN в марке не соответствует GTIN товара, оформите брак по данному товару»
            screenNavigator.openAlertDisparityGTINScreen()
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
        boxMaterialNumber
                .takeIf { it.isEmpty() }
                ?.run {
                    //Отсканированная коробка не числится в поставке. Отсканируйте все марки из этого короба
                    screenNavigator.openMarkingBoxNotIncludedDeliveryScreen()
                    return
                }

        boxMaterialNumber
                .takeIf { it != productMaterialNumber }
                ?.run {
                    //Отсканированная коробка принадлежит товару <SAP-код> <Название>
                    val materialName = ZfmpUtz48V001(hyperHive).getProductInfoByMaterial(boxMaterialNumber)?.name.orEmpty()
                    screenNavigator.openAlertScannedBoxBelongsAnotherProductScreen(
                            materialNumber = boxMaterialNumber,
                            materialName = materialName
                    )
                    return
                }

        val checkStampControlValue = checkStampControl.value ?: false
        checkStampControlValue
                .takeIf { !it }
                ?.run {
                    //Сначала произведите контроль нормы путем сканирования МАРОК, а затем добавляйте марки сканированием коробок.
                    screenNavigator.openMarkingPerformRateControlScreen()
                    return
                }

        val checkBlocksCategoriesDifferentCurrent = processMarkingBoxProductService.checkBlocksCategoriesDifferentCurrent(boxMaterialNumber, currentTypeDiscrepanciesCode)
        checkBlocksCategoriesDifferentCurrent
                .takeIf { it }
                ?.run {
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
        val countAddBlocksForBox = processMarkingBoxProductService.addBoxDiscrepancy(boxNumber, currentTypeDiscrepanciesCode)
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

}
