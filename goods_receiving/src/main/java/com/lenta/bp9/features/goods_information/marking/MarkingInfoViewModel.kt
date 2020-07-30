package com.lenta.bp9.features.goods_information.marking

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessMarkingProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskBlockInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
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
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

//https://trello.com/c/NGsFfWgB
class MarkingInfoViewModel : CoreViewModel(),
        OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processMarkingProductService: ProcessMarkingProductService

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var hyperHive: HyperHive

    private val zmpUtz07V001: ZmpUtz07V001 by lazy {
        ZmpUtz07V001(hyperHive)
    }

    private val zmpUtz25V001: ZmpUtz25V001 by lazy {
        ZmpUtz25V001(hyperHive)
    }

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val tvAccept: MutableLiveData<String> = MutableLiveData("")
    private val countScannedBlocks: MutableLiveData<Int> = MutableLiveData(0) //только блоки, переменная используется для отображения счетчика в поле Контроль марок
    private val countScannedStamps: MutableLiveData<Int> = MutableLiveData(0) //блоки и GTINы, переменная используется для кнопки Откатить
    val spinQualityEnabled: MutableLiveData<Boolean> = countScannedBlocks.map {
        it == 0
    }
    val spinReasonRejectionEnabled: MutableLiveData<Boolean> = countScannedBlocks.map {
        it == 0
    }
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData()
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
    private val paramGrzMeinsPack: MutableLiveData<String> = MutableLiveData("")
    private val paramGrzExclGtin: MutableLiveData<String> = MutableLiveData("")
    private val uom: MutableLiveData<Uom> = MutableLiveData()
    private val unprocessedQuantityOfBlocks: MutableLiveData<Double> = MutableLiveData(0.0)
    val isVisibilityControlGTIN: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.isControlGTIN == true)
    }

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { countVal ->
        productInfo.value
                ?.let {
                    processMarkingProductService.getCountAttachmentInBlock(countVal)
                }
                .orIfNull {
                    countVal?.toDoubleOrNull() ?: 0.0
                }

    }

    val visibilityImgUnit: MutableLiveData<Boolean> = MutableLiveData(true)

    val acceptTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        val productCountAccept =
                productInfo.value
                        ?.let { product ->
                            taskManager
                                    .getReceivingTask()
                                    ?.taskRepository
                                    ?.getProductsDiscrepancies()
                                    ?.getCountAcceptOfProduct(product)
                        }
                        ?: 0.0

        val totalCount = it?.first ?: 0.0
        val spinQualitySelectedPositionValue = it?.second ?: 0

        qualityInfo.value
                ?.get(spinQualitySelectedPositionValue)
                ?.code
                ?.takeIf { code ->
                    code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                }
                ?.run {
                    totalCount + productCountAccept
                }
                ?: productCountAccept
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val productCountAccept =
                productInfo.value
                        ?.let { product ->
                            taskManager
                                    .getReceivingTask()
                                    ?.taskRepository
                                    ?.getProductsDiscrepancies()
                                    ?.getCountAcceptOfProduct(product)
                        }
                        ?: 0.0
        when {
            (it ?: 0.0) > 0.0 -> {
                "+ ${it.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name.orEmpty()}"
            }
            else -> { //если было введено отрицательное значение
                "${if (productCountAccept > 0.0) "+ " + productCountAccept.toStringFormatted() else productCountAccept.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name.orEmpty()}"
            }
        }
    }

    val refusalTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        val productCountRefusal =
                productInfo.value
                        ?.let { product ->
                            taskManager
                                    .getReceivingTask()
                                    ?.taskRepository
                                    ?.getProductsDiscrepancies()
                                    ?.getCountRefusalOfProduct(product)
                        }
                        ?: 0.0

        val totalCount = it?.first ?: 0.0
        val spinQualitySelectedPositionValue = it?.second ?: 0

        qualityInfo.value
                ?.get(spinQualitySelectedPositionValue)
                ?.code
                ?.takeIf { code ->
                    code != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                }
                ?.run {
                    totalCount + productCountRefusal
                }
                ?: productCountRefusal
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val productCountRefusal =
                productInfo.value
                        ?.let { product ->
                            taskManager
                                    .getReceivingTask()
                                    ?.taskRepository
                                    ?.getProductsDiscrepancies()
                                    ?.getCountRefusalOfProduct(product)
                        }
                        ?: 0.0

        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name.orEmpty()}"
        } else { //если было введено отрицательное значение
            "${if (productCountRefusal > 0.0) "- " + productCountRefusal.toStringFormatted() else productCountRefusal.toStringFormatted()} ${productInfo.value?.purchaseOrderUnits?.name.orEmpty()}"
        }
    }

    val checkStampControlVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvStampControlVal: MutableLiveData<String> = acceptTotalCount
            .combineLatest(spinQualitySelectedPosition)
            .combineLatest(countScannedBlocks)
            .map {
                val acceptTotalCountVal = acceptTotalCount.value ?: 0.0
                val acceptTotalCountBlock = processMarkingProductService.getCountBlocksByAttachments(acceptTotalCountVal.toStringFormatted())
                val countBlockScanned =
                        productInfo.value
                                ?.let {//проверяем productInfo т.к. он используется в processMarkingProductService.getCountProcessedBlockForDiscrepancies() и если он null, тогда он неинициализирован в processMarkingProductService, и получим lateinit property productInfo has not been initialized
                                    processMarkingProductService.getCountProcessedBlockForDiscrepancies(TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM).toDouble()
                                }
                                ?: 0.0
                val spinQualitySelectedPositionVal = spinQualitySelectedPosition.value ?: 0
                val qualityInfoCode =
                        qualityInfo.value
                                ?.get(spinQualitySelectedPositionVal)
                                ?.code
                                .orEmpty()
                val numberStampsControl =
                        productInfo.value
                                ?.numberStampsControl
                                ?.toDouble()
                                ?: 0.0
                if (qualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                    if (numberStampsControl == 0.0 || acceptTotalCountVal <= 0.0) {
                        checkStampControlVisibility.value = false
                        context.getString(R.string.not_required)
                    } else {
                        checkStampControlVisibility.value = true
                        if (acceptTotalCountBlock < numberStampsControl) {
                            buildString {
                                append(countBlockScanned.toStringFormatted())
                                append(" ")
                                append(context.getString(R.string.of))
                                append(" ")
                                append(acceptTotalCountBlock.toStringFormatted())
                            }
                        } else {
                            buildString {
                                append(countBlockScanned.toStringFormatted())
                                append(" ")
                                append(context.getString(R.string.of))
                                append(" ")
                                append(numberStampsControl.toStringFormatted())
                            }
                        }
                    }
                } else {
                    checkStampControlVisibility.value = false
                    "" //это поле отображается только при выбранной категории "Норма"
                }
            }

    val checkStampControl: MutableLiveData<Boolean> = checkStampControlVisibility.map {
        val countBlockScanned = processMarkingProductService.getCountProcessedBlockForDiscrepancies(TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM).toDouble()
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
                //проверяем productInfo т.к. он используется в processMarkingProductService.getCountProcessedBlockForDiscrepancies() и если он null, тогда он неинициализирован в processMarkingProductService, и получим lateinit property productInfo has not been initialized
                productInfo.value
                        ?.let { product ->
                            val enteredCount = count.value?.toDoubleOrNull() ?: 0.0
                            val countBlockScannedValue = countScannedBlocks.value ?: 0
                            val typeDiscrepancies = getCurrentTypeDiscrepancies()
                            val productOrigQuantity = product.origQuantity
                            val totalBlocksProduct = processMarkingProductService.getCountBlocksByAttachments(productOrigQuantity)
                            val countProcessedBlocksCurrentDiscrepancies = processMarkingProductService.getCountProcessedBlockForDiscrepancies(typeDiscrepancies)
                            if (countBlockScannedValue <= 0) { //фиксируем необработанное количество после первого сканирования марок, чтобы не учитывать их в текущей сессии, иначе это кол-во будет уменьшаться и появиться текст Не требуется
                                unprocessedQuantityOfBlocks.value = totalBlocksProduct - countProcessedBlocksCurrentDiscrepancies
                            }
                            val unprocessedQuantityOfBlocksVal = unprocessedQuantityOfBlocks.value ?: 0.0
                            if (typeDiscrepancies != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                                if (unprocessedQuantityOfBlocksVal == enteredCount) {
                                    checkBoxStampListVisibility.value = false
                                    context.getString(R.string.not_required)
                                } else {
                                    checkBoxStampListVisibility.value = true
                                    buildString {
                                        append(countProcessedBlocksCurrentDiscrepancies.toDouble().toStringFormatted())
                                        append(" ")
                                        append(context.getString(R.string.of))
                                        append(" ")
                                        append(enteredCount.toStringFormatted())
                                    }
                                }
                            } else {
                                checkBoxStampListVisibility.value = false
                                "" //это поле отображается только при выбранной категории брака
                            }
                        }.orEmpty()

            }

    val checkBoxStampList: MutableLiveData<Boolean> = checkBoxStampListVisibility.map {
        //проверяем productInfo т.к. он используется в processMarkingProductService.getCountProcessedBlockForDiscrepancies() и если он null, тогда он неинициализирован в processMarkingProductService, и получим lateinit property productInfo has not been initialized
        productInfo.value
                ?.let {
                    val enteredCount = count.value?.toDoubleOrNull() ?: 0.0
                    val countBlockScanned = processMarkingProductService.getCountProcessedBlockForDiscrepancies(getCurrentTypeDiscrepancies()).toDouble()
                    countBlockScanned >= enteredCount && enteredCount > 0.0
                }
                ?: false
    }

    val enabledApplyButton: MutableLiveData<Boolean> =
            spinQualitySelectedPosition
                    .combineLatest(checkStampControl)
                    .combineLatest(checkBoxStampList)
                    .combineLatest(acceptTotalCount)
                    .map {
                        val enteredCount = count.value?.toDoubleOrNull() ?: 0.0
                        val typeDiscrepancies = getCurrentTypeDiscrepancies()
                        val checkStampControlValue = checkStampControl.value ?: false
                        val checkBoxStampListValue = checkBoxStampList.value ?: false
                        val acceptTotalCountValue = acceptTotalCount.value ?: 0.0

                        (enteredCount > 0.0 || (acceptTotalCountValue > 0.0 && typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM))
                                && (typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                                ||checkStampControlValue
                                || checkBoxStampListValue)
                    }

    val enabledRollbackBtn: MutableLiveData<Boolean> = countScannedStamps.map {
        (it ?: 0) > 0
    }

    init {
        launchUITryCatch {
            productInfo.value
                    ?.let {
                        if (processMarkingProductService.newProcessMarkingProductService(it) == null) {
                            with(screenNavigator) {
                                goBack()
                                openAlertWrongProductType()
                            }
                            return@launchUITryCatch
                        }
                    }.orIfNull {
                        with(screenNavigator) {
                            goBack()
                            openAlertWrongProductType()
                        }
                        return@launchUITryCatch
                    }

            searchProductDelegate.init(viewModelScope = this@MarkingInfoViewModel::viewModelScope,
                    scanResultHandler = this@MarkingInfoViewModel::handleProductSearchResult)

            val isBlockMode =
                    productInfo.value
                            ?.let {
                                processMarkingProductService.getIsBlockMode()
                            }
                            ?: false
            uom.value = if (isBlockMode) {
                paramGrzMeinsPack.value = dataBase.getGrzMeinsPack().orEmpty()
                val uomInfo = zmpUtz07V001.getUomInfo(paramGrzMeinsPack.value)
                Uom(
                        code = uomInfo?.uom.orEmpty(),
                        name = uomInfo?.name.orEmpty()
                )
            } else {
                Uom(
                        code = productInfo.value?.purchaseOrderUnits?.code.orEmpty(),
                        name = productInfo.value?.purchaseOrderUnits?.name.orEmpty()
                )
            }

            tvAccept.value = if (!isBlockMode) {
                context.getString(R.string.accept_txt)
            } else {
                context.getString(
                        R.string.accept,
                        "${uom.value?.name.orEmpty()}=${productInfo.value?.nestingInOneBlock?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"
                )
            }

            qualityInfo.value = dataBase.getQualityInfo() ?: emptyList()
            spinQuality.value = qualityInfo.value
                    ?.map {
                        it.name
                    }
                    ?: emptyList()

            suffix.value = uom.value?.name.orEmpty()
            paramGrzExclGtin.value = dataBase.getGrzExclGtin().orEmpty()

            //эту строку необходимо прописывать только после того, как были установлены данные для переменных count  и suffix, а иначе фокус в поле et_count не установится
            requestFocusToCount.value = true
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    fun onClickRollback() {
        if (productInfo.value?.isControlGTIN == true) {
            rollbackControlGtin()
            return
        }

        processMarkingProductService.rollbackTypeLastStampScanned()
        //уменьшаем кол-во отсканированных марок (блок/gtin) на единицу в текущей сессии
        countScannedStamps.value = countScannedStamps.value?.minus(1)
        //уменьшаем кол-во отсканированных блоков на единицу в текущей сессии
        countScannedBlocks.value = countScannedBlocks.value?.minus(1)
    }

    private fun rollbackControlGtin() {
        if (checkBoxGtinControl.value == false && checkBoxGtinStampControl.value == false) {
            /**Чек-боксы не установлены -
             * По кнопке удалять последний отсканированный блок.
             * Уменьшать количество в поле "Список марок",
             * "Контроль марок" на 1 блок. (Очищать этот блок и его GTIN)
             * */
            with(processMarkingProductService) {
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

        if (checkBoxGtinControl.value == true) {
            /**Установлен чек-бокс GTIN (первый чек-бокс) -
             * удалять отсканированный GTIN из локальной стуктуры,
             * очищать активный чек-бокс для GTIN
             * */
            processMarkingProductService.rollbackTypeLastStampScanned()
            checkBoxGtinControl.value = false
            //уменьшаем кол-во отсканированных марок (блок/gtin) на единицу в текущей сессии
            minusScannedStamps(1)
        }

        if (checkBoxGtinStampControl.value == true) {
            /**Установлен чек-бокс марки (второй чек-бокс) -
             * удалять отсканированный блок из локальной структуры,
             * очищать активный чек-бокс для марки
             * */
            processMarkingProductService.rollbackTypeLastStampScanned()
            checkBoxGtinStampControl.value = false
            //уменьшаем кол-во отсканированных марок (блок/gtin) на единицу в текущей сессии
            minusScannedStamps(1)
            //уменьшаем кол-во отсканированных блоков на единицу в текущей сессии
            minusScannedBlocks()
        }
    }

    private fun minusScannedStamps(count: Int) {
        countScannedStamps.value
                ?.takeIf {
                    it > 0
                }
                ?.run {
                    countScannedStamps.value = countScannedStamps.value?.minus(count)
                }
    }

    private fun minusScannedBlocks() {
        countScannedBlocks.value
                ?.takeIf {
                    it > 0
                }
                ?.run {
                    countScannedBlocks.value = countScannedBlocks.value?.minus(1)
                }
    }

    fun onClickDetails() {
        productInfo.value?.let {
            screenNavigator.openMarkingGoodsDetailsScreen(it)
        }
    }

    fun onClickAdd(): Boolean {
        var enteredCount = countValue.value ?: 0.0
        return if (processMarkingProductService.overLimit(enteredCount)) {
            screenNavigator.openAlertOverLimitPlannedScreen()
            false
        } else {
            val typeDiscrepancies = getCurrentTypeDiscrepancies()
            if (typeDiscrepancies.isNotEmpty()) {
                val countProcessedBlock = processMarkingProductService.getCountProcessedBlockForDiscrepancies(getCurrentTypeDiscrepancies()).toDouble()
                val countProcessedProduct = processMarkingProductService.getCountAttachmentInBlock(countProcessedBlock.toStringFormatted())
                if (countProcessedProduct > enteredCount && enteredCount > 0.0) {
                    enteredCount = countProcessedProduct
                }
                processMarkingProductService.addProduct(enteredCount.toStringFormatted(), typeDiscrepancies)
                processMarkingProductService.apply()
                processMarkingProductService.clearModifications()
                spinQualitySelectedPosition.value = 0
                count.value = "0"
                //обнуляем кол-во отсканированных марок (блок/gtin)
                countScannedStamps.value = 0
                //обнуляем кол-во отсканированных блоков
                countScannedBlocks.value = 0
                true
            } else {
                false
            }

        }
    }

    fun onClickApply() {
        if (onClickAdd()) screenNavigator.goBack()
    }

    //https://trello.com/c/N6t51jru
    fun onScanResult(data: String) {
        val enteredCount = count.value?.toDoubleOrNull() ?: 0.0
        val acceptTotalCountValue = acceptTotalCount.value ?: 0.0
        if (enteredCount <= 0.0) {
            if (!(getCurrentTypeDiscrepancies() == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                            && acceptTotalCountValue > 0.0)) {
                screenNavigator.openAlertMustEnterQuantityInfoGreenScreen()
                return
            }
        }

        when (data.length) {
            in 0..7, in 9..11, in 15..20 -> {
                screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
            }
            in 21..40, 42, 43 -> {
                screenNavigator.openAlertInvalidCodeScannedForCurrentModeScreen()
            }
            8, in 12..14 -> {//GTIN
                if (getCurrentTypeDiscrepancies() == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                    gtinScannedCheck(data.padStart(14, '0'))
                }
            }
            else -> { //марка/блок. отсканировано 41, 44 или более 44 символов
                if (barcodeCheck(data)) {
                    blockCheck(data.substring(0, 25)) //обрезаем криптохвост
                } else {
                    if (data.length == 41 || data.length == 44) {
                        screenNavigator.openAlertInvalidCodeScannedForCurrentModeScreen()
                    } else {
                        screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
                    }
                }
            }
        }
    }

    private fun barcodeCheck(data: String): Boolean {
        val regex = """\w+""".toRegex()
        val barcode = when (data.length) {
            41 -> data
            44 -> data.removeRange(43, 44).removeRange(36, 37).removeRange(25, 26)
            else -> {
                if (data.substring(0, 41).matches(regex)) {
                    data.substring(0, 41)
                } else {
                    data.substring(0, 44)
                            .removeRange(43, 44)
                            .removeRange(36, 37)
                            .removeRange(25, 26)
                }
            }
        }

        return if (barcode.isNotEmpty()) {
            val isBlockBarcode = barcode.substring(0, 2) == "01"
            val gtinCode = barcode.substring(2, 16)
            val isGtin = gtinCode.toLongOrNull() != null && gtinCode.length == 14
            val isSerialStartCode = barcode.substring(16, 18) == "21"
            val isSerial = barcode.substring(18, 25).length == 7
            val isMrcStartCode = barcode.substring(25, 29) == "8005"
            val mrcCode = barcode.substring(29, 35)
            val isMrc = mrcCode.toIntOrNull() != null && mrcCode.length == 6
            val isVerificationKeyStartCode = barcode.substring(35, 37) == "93"
            val isVerificationKey = barcode.substring(37, 41).length == 4

            isBlockBarcode
                    && isGtin
                    && isSerialStartCode
                    && isSerial
                    && isMrcStartCode
                    && isMrc
                    && isVerificationKeyStartCode
                    && isVerificationKey
        } else {
            false
        }
    }

    private fun blockCheck(stampCode: String) {
        val typeDiscrepancies = getCurrentTypeDiscrepancies()
        val blockInfo = processMarkingProductService.searchBlock(stampCode)

        if (processMarkingProductService.blockIsAlreadyProcessed(stampCode)) {
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
                            if (typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
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
                    materialName = zfmpUtz48V001.getProductInfoByMaterial(blockMaterialNumber)?.name.orEmpty()
            )
            return
        }

        if (productInfo.value?.isControlGTIN == true
                && typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
            gtinControlValueCheck(
                    stampCode = stampCode,
                    blockInfo = blockInfo,
                    typeDiscrepancies = typeDiscrepancies
            )
        } else {
            addBlock(
                    blockInfo = blockInfo,
                    typeDiscrepancies = typeDiscrepancies
            )
            //обновляем кол-во отсканированных марок/блоков для отображения на экране в поле «Контроль марок»
            countScannedBlocks.value = countScannedBlocks.value?.plus(1)
        }
    }

    private fun addBlock(blockInfo: TaskBlockInfo?, typeDiscrepancies: String?) {
        blockInfo?.let { currentBlock ->
            typeDiscrepancies?.let { currentTypeDiscrepancies ->
                processMarkingProductService.addBlocksDiscrepancies(
                        blockInfo = currentBlock,
                        typeDiscrepancies = currentTypeDiscrepancies,
                        isScan = true
                )
                countScannedStamps.value = countScannedStamps.value?.plus(1)
            }
        }
    }

    private fun addGtin(gtinCode: String) {
        processMarkingProductService.addGtin(gtinCode)
        countScannedStamps.value = countScannedStamps.value?.plus(1)
    }

    private fun gtinControlValueCheck(stampCode: String, blockInfo: TaskBlockInfo?, typeDiscrepancies: String?) {
        /**• Марка/блок соответствуют текущему товару – Проверять активен ли чекбокс контроля GTIN:
        o чекбокс не активен - проверить начинается ли марка на <0GRZ_EXCL_GTIN> (GRZ_EXCL_GTIN параметр из 14 справочника):
        Если начинается - то необходимо выбирать GTIN из марки подставлять его "как будто" отсканированный ШК=>проставлять чек-боксы в полях КМ и GTIN => проходить контроль GTIN
        Если не начинается - устанавливать чек бокс в поле «Сверка КМ» в поле «Контроль GTIN», количество марок с пройденным контролем не увеличивать
        o чекбокс активен – выполнять проверки, описанные в разделе 2. MARK.ППП. Логика сверки GTIN.*/
        if (checkBoxGtinControl.value == false) {
            if (stampCode.substring(0, 2) == "0${paramGrzExclGtin.value}") {
                checkBoxGtinControl.value = true //аналитики сказали сначала поставить чекбокс, а потом снять
                checkBoxGtinStampControl.value = true //аналитики сказали сначала поставить чекбокс, а потом снять
                val gtinCode = stampCode.substring(2, 16)
                addGtin(gtinCode)
                addBlock(blockInfo, typeDiscrepancies)
                checkBoxGtinControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять
                checkBoxGtinStampControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять
                //обновляем кол-во отсканированных марок/блоков для отображения на экране в поле «Контроль марок»
                countScannedBlocks.value = countScannedBlocks.value?.plus(1)
            } else {
                checkBoxGtinStampControl.value = true
                //обновляем кол-во отсканированных марок/блоков НО на экране в поле «Контроль марок» кол-во не обновляем, т.е. countScannedBlocks не увеличиваем на единицу
                addBlock(blockInfo, typeDiscrepancies)
            }
        } else {
            //выполнять проверки, описанные в разделе 2. MARK.ППП. Логика сверки GTIN. https://trello.com/c/y2ECoCw4
            blockInfo?.let { currentBlock ->
                typeDiscrepancies?.let { currentTypeDiscrepancies ->
                    gtinControlCheck(currentBlock, currentTypeDiscrepancies)
                }
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

        val eanInfo = zmpUtz25V001.getEansFromMaterial(material = productInfo.value?.materialNumber.orEmpty())
                .asSequence()
                .map { eans ->
                    eans.ean.padStart(14, '0')
                }.findLast { ean ->
                    ean == gtinCode
                }.orEmpty()

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
        val lastScannedBlockInfo = processMarkingProductService.getLastScannedBlock()
        val lastScannedGtin = lastScannedBlockInfo?.blockNumber?.substring(2, 16).orEmpty()
        if (gtinCode == lastScannedGtin) {
            lastScannedBlockInfo?.let { blockInfo ->
                checkBoxGtinControl.value = true //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
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
            processMarkingProductService.replaceLastGtin(gtinCode)
            addBlock(blockInfo, typeDiscrepancies)
            checkBoxGtinControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
            checkBoxGtinStampControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
            //обновляем кол-во отсканированных марок/блоков для отображения на экране в поле «Контроль марок»
            countScannedBlocks.value = countScannedBlocks.value?.plus(1)
            return
        }

        val lastScannedGtin = processMarkingProductService.getLastScannedGtin().orEmpty()
        if (lastScannedGtin == gtinCode) {
            checkBoxGtinStampControl.value = true //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
            addBlock(blockInfo, typeDiscrepancies)
            checkBoxGtinControl.value = false
            checkBoxGtinStampControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять чисто для показа пользователю на мгновение
            //обновляем кол-во отсканированных марок/блоков для отображения на экране в поле «Контроль марок»
            countScannedBlocks.value = countScannedBlocks.value?.plus(1)
        } else {
            //выводить ошибку «GTIN в марке не соответствует GTIN товара, оформите брак по данному товару»
            screenNavigator.openAlertDisparityGTINScreen()
        }
    }

    override fun onClickPosition(position: Int) {
        spinReasonRejectionSelectedPosition.value = position
    }

    fun onClickPositionSpinQuality(position: Int) {
        launchUITryCatch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(qualityInfo.value!![position].code)
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
                    ?: emptyList()
            count.value = count.value
            screenNavigator.hideProgress()
        }
    }

    private fun getCurrentTypeDiscrepancies(): String {
        val spinQualityPosition = spinQualitySelectedPosition.value ?: 0
        val spinRejectionPosition = spinReasonRejectionSelectedPosition.value ?: 0
        val selectedQualityInfo = qualityInfo.value?.get(spinQualityPosition)
        val selectedReasonRejectionInfo = reasonRejectionInfo.value?.get(spinRejectionPosition)
        return selectedQualityInfo
                ?.code
                ?.takeIf { qualityInfoCode ->
                    qualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                }
                ?: selectedReasonRejectionInfo?.code.orEmpty()
    }

    fun onBackPressed() {
        if (processMarkingProductService.modifications()) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        processMarkingProductService.clearModifications()
                        screenNavigator.goBack()
                    }
            )
            return
        }

        screenNavigator.goBack()
    }

    fun scanMark1() {//todo
        onScanResult("010460026601136721000004G.8005012345.938000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
    }

    fun scanMark2() {//todo
        onScanResult("010460026601136721000003Z.8005012345.938000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
    }

    fun scanMark3() {//todo
        onScanResult("010460026601136721000003Y.8005012345.938000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
    }

}
