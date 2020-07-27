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
import com.lenta.shared.fmp.resources.dao_ext.getEanInfo
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
    val isDiscrepancy: MutableLiveData<Boolean> = MutableLiveData(false)
    private val countBlockScanned: MutableLiveData<Int> = MutableLiveData(0)
    val spinQualityEnabled: MutableLiveData<Boolean> = countBlockScanned.map {
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
    private val isBlockMode: MutableLiveData<Boolean> = MutableLiveData(false)
    private val uom: MutableLiveData<Uom> = MutableLiveData()
    val isVisibilityControlGTIN: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.isControlGTIN == true)
    }

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map {
        val nestingInOneBlock = productInfo.value?.nestingInOneBlock?.toDouble() ?: 0.0
        if (isBlockMode.value == true) {
            (it?.toDoubleOrNull() ?: 0.0) * nestingInOneBlock
        } else {
            it?.toDoubleOrNull() ?: 0.0
        }
    }

    val visibilityImgUnit: MutableLiveData<Boolean> = MutableLiveData(true)

    val acceptTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        val productCountAccept = productInfo.value
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
        val productCountAccept = productInfo.value
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
        val productCountRefusal = productInfo.value
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
        val productCountRefusal = productInfo.value
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
            .combineLatest(countBlockScanned)
            .map {
                val acceptTotalCountVal = acceptTotalCount.value ?: 0.0
                val countBlockScannedVal = countBlockScanned.value ?: 0
                val qualityInfoCode = qualityInfo.value?.get(it?.second ?: 0)?.code.orEmpty()
                val numberStampsControl = productInfo.value?.numberStampsControl?.toDouble() ?: 0.0
                if (qualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                    if (numberStampsControl == 0.0 || acceptTotalCountVal <= 0.0) {
                        checkStampControlVisibility.value = false
                        context.getString(R.string.not_required)
                    } else {
                        checkStampControlVisibility.value = true
                        if (acceptTotalCountVal < numberStampsControl) {
                            "${countBlockScannedVal.toDouble().toStringFormatted()} ${context.getString(R.string.of)} ${acceptTotalCountVal.toStringFormatted()}"
                        } else {
                            "${countBlockScannedVal.toDouble().toStringFormatted()} ${context.getString(R.string.of)} ${numberStampsControl.toStringFormatted()}"
                        }
                    }
                } else {
                    checkStampControlVisibility.value = false
                    "" //это поле отображается только при выбранной категории "Норма"
                }
            }

    val checkStampControl: MutableLiveData<Boolean> = checkStampControlVisibility.map {
        val countBlockScannedVal = countBlockScanned.value?.toDouble() ?: 0.0
        val numberStampsControl = productInfo.value?.numberStampsControl?.toDouble() ?: 0.0
        countBlockScannedVal == numberStampsControl
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

    val tvStampListVal: MutableLiveData<String> = count
            .combineLatest(spinQualitySelectedPosition)
            .combineLatest(countBlockScanned)
            .map {
                val enteredCount = it?.first?.first?.toDoubleOrNull() ?: 0.0
                val spinQualitySelectedPositionValue = it?.first?.second ?: 0
                val qualityInfoCode = qualityInfo.value?.get(spinQualitySelectedPositionValue)?.code.orEmpty()
                //проверяем productInfo т.к. он используется в processMarkingProductService.getCountProcessedBlockForDiscrepancies() и если он null, тогда он неинициализирован в processMarkingProductService, и получим lateinit property productInfo has not been initialized
                productInfo.value
                        ?.let {
                            if (qualityInfoCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                                if (enteredCount == 0.0 || enteredCount == processMarkingProductService.getCountProcessedBlockForDiscrepancies(qualityInfoCode)) {
                                    checkBoxStampListVisibility.value = false
                                    context.getString(R.string.not_required)
                                } else {
                                    checkBoxStampListVisibility.value = true
                                    "${processMarkingProductService.getCountProcessedBlockForDiscrepancies(qualityInfoCode).toStringFormatted()} ${context.getString(R.string.of)} ${count.value.orEmpty()}"
                                }
                            } else {
                                checkBoxStampListVisibility.value = false
                                "" //это поле отображается только при выбранной категории брака
                            }
                        }.orEmpty()

            }

    val checkBoxStampList: MutableLiveData<Boolean> = checkBoxStampListVisibility.map {
        //проверяем productInfo т.к. он используется в processMarkingProductService.getCountProcessedBlockForDiscrepancies() и если он null, тогда он неинициализирован в processMarkingProductService, и получим lateinit property productInfo has not been initialized
        productInfo.value?.let {
            val enteredCount = count.value?.toDoubleOrNull() ?: 0.0
            val spinQualitySelectedPositionValue = spinQualitySelectedPosition.value ?: 0
            val qualityInfoCode = qualityInfo.value?.get(spinQualitySelectedPositionValue)?.code.orEmpty()
            enteredCount == processMarkingProductService.getCountProcessedBlockForDiscrepancies(qualityInfoCode)
        } ?: false

    }

    val enabledApplyButton: MutableLiveData<Boolean> =
            spinQualitySelectedPosition
                    .combineLatest(checkStampControl)
                    .combineLatest(checkBoxStampList)
                    .map {
                        val spinQualitySelectedPositionValue = it?.first?.first ?: 0
                        val qualityInfoCode = qualityInfo.value?.get(spinQualitySelectedPositionValue)?.code.orEmpty()
                        val checkStampControlValue = it?.first?.second ?: false
                        val checkBoxStampListValue = it?.second ?: false

                        qualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                                || checkStampControlValue
                                || checkBoxStampListValue
                    }

    val enabledRollbackBtn: MutableLiveData<Boolean> = countBlockScanned.map {
        (it ?: 0) > 0
    }

    init {
        launchUITryCatch {
            searchProductDelegate.init(viewModelScope = this@MarkingInfoViewModel::viewModelScope,
                    scanResultHandler = this@MarkingInfoViewModel::handleProductSearchResult)

            val purchaseOrderUnitsCode = productInfo.value?.purchaseOrderUnits?.code.orEmpty()
            isBlockMode.value = (purchaseOrderUnitsCode == "ST" || purchaseOrderUnitsCode == "P09") && productInfo.value?.isCountingBoxes == false
            uom.value = if (isBlockMode.value == true) {
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

            tvAccept.value = if (isBlockMode.value == false) {
                context.getString(R.string.accept_txt)
            } else {
                context.getString(
                        R.string.accept,
                        "${uom.value?.name.orEmpty()}=${productInfo.value?.nestingInOneBlock?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"
                )
            }

            if (isDiscrepancy.value == true) {
                productInfo.value?.let {
                    count.value = taskManager.getReceivingTask()
                            ?.taskRepository
                            ?.getProductsDiscrepancies()
                            ?.getCountProductNotProcessedOfProduct(it)
                            .toStringFormatted()
                }
                qualityInfo.value = dataBase.getQualityInfoForDiscrepancy() ?: emptyList()
                spinQualitySelectedPosition.value = qualityInfo.value?.indexOfLast { it.code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_REASON_REJECTION_DELIVERY_ERRORS }
            } else {
                qualityInfo.value = dataBase.getQualityInfo() ?: emptyList()
            }
            spinQuality.value = qualityInfo.value
                    ?.map {
                        it.name
                    }
                    ?: emptyList()

            suffix.value = uom.value?.name.orEmpty()
            paramGrzExclGtin.value = dataBase.getGrzExclGtin().orEmpty()

            //эту строку необходимо прописывать только после того, как были установлены данные для переменных count  и suffix, а иначе фокус в поле et_count не установится
            requestFocusToCount.value = true

            if (processMarkingProductService.newProcessMarkingProductService(productInfo.value!!) == null) {
                screenNavigator.goBack()
                screenNavigator.openAlertWrongProductType()
            }
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    fun onClickRollback() {
        if (productInfo.value?.isControlGTIN == true) {
            if (checkBoxGtinControl.value == false && checkBoxGtinStampControl.value == false) {
                processMarkingProductService.rollbackScannedBlock()
                processMarkingProductService.rollbackScannedGtin()
                //уменьшаем кол-во отсканированных марок на единицу в текущей сессии
                countBlockScanned.value = countBlockScanned.value?.minus(1)
            } else {
                if (checkBoxGtinControl.value == true) {
                    processMarkingProductService.rollbackScannedGtin()
                    checkBoxGtinControl.value = false
                }
                if (checkBoxGtinStampControl.value == true) {
                    processMarkingProductService.rollbackScannedBlock()
                    //уменьшаем кол-во отсканированных марок на единицу в текущей сессии
                    countBlockScanned.value = countBlockScanned.value?.minus(1)
                }
            }
        } else {
            processMarkingProductService.rollbackScannedBlock()
            //уменьшаем кол-во отсканированных марок на единицу в текущей сессии
            countBlockScanned.value = countBlockScanned.value?.minus(1)
        }
    }

    fun onClickDetails() {
        //todo
        /**productInfo.value?.let {
            screenNavigator.openMarkingGoodsDetailsScreen(it)
        }*/
        onScanResult("01046000660121422100000Ga.8005012345.938000.92NGkg+wRXz36kBFjpfwOub5DBIIpD2iS/DMYpZuuDLU0Y3pZt1z20/1ksr4004wfhDhRxu4dgUV4QN96Qtdih9g==")
    }

    fun onClickAdd(): Boolean {
        val countVal = countValue.value ?: 0.0
        return if (processMarkingProductService.overLimit(countVal)) {
            screenNavigator.openAlertOverLimitPlannedScreen()
            false
        } else {
            val spinQualityPosition = spinQualitySelectedPosition.value ?: 0
            val selectedQualityInfo = qualityInfo.value?.get(spinQualityPosition)
            val spinRejectionPosition = spinReasonRejectionSelectedPosition.value ?: 0
            val selectedReasonRejectionInfo = reasonRejectionInfo.value?.get(spinRejectionPosition)
            val typeDiscrepancies = selectedQualityInfo?.code
                    ?.takeIf { selectedQualityInfoCode ->
                        selectedQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                    }
                    ?: selectedReasonRejectionInfo?.code
            if (typeDiscrepancies != null) {
                processMarkingProductService.addProduct(countVal.toStringFormatted(), typeDiscrepancies)
                processMarkingProductService.apply()
                processMarkingProductService.clearModifications()
                //обнуляем кол-во отсканированных марок
                countBlockScanned.value = 0
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
        when (data.length) {
            in 0..13, in 15..20 -> {
                screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
            }
            in 21..40, 42, 43 -> {
                screenNavigator.openAlertInvalidCodeScannedForCurrentModeScreen()
            }
            14 -> {//GTIN
                gtinScannedCheck(data)
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
        val spinQualityPosition = spinQualitySelectedPosition.value ?: 0
        val selectedQualityInfo = qualityInfo.value?.get(spinQualityPosition)
        val spinRejectionPosition = spinReasonRejectionSelectedPosition.value ?: 0
        val selectedReasonRejectionInfo = reasonRejectionInfo.value?.get(spinRejectionPosition)
        val typeDiscrepancies = selectedQualityInfo?.code
                ?.takeIf { selectedQualityInfoCode ->
                    selectedQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                }
                ?: selectedReasonRejectionInfo?.code
        val blockInfo = processMarkingProductService.searchBlock(stampCode)

        if (processMarkingProductService.blockIsAlreadyProcessed(stampCode)) {
            screenNavigator.openAlertScannedStampIsAlreadyProcessedScreen() //АМ уже обработана
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
                            if (selectedQualityInfo?.code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                                count.value?.let {
                                    if (it.toDouble() > 0.0) {
                                        count.value = count.value?.toDouble()?.minus(1)?.toStringFormatted()
                                        //Количество нормы будет уменьшено
                                        screenNavigator.openAlertAmountNormWillBeReduced()
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

        if (productInfo.value?.isControlGTIN == true) {
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
                //обновляем кол-во отсканированных марок/блоков для отображения на экране в поле «Контроль марок»
                countBlockScanned.value = countBlockScanned.value
            }
        }
    }

    private fun gtinControlValueCheck(stampCode: String, blockInfo: TaskBlockInfo?, typeDiscrepancies: String?) {
        /**• Марка/блок соответствуют текущему товару – Проверять активен ли чекбокс контроля GTIN:
        o чекбокс не активен - проверить начинается ли марка на <0GRZ_EXCL_GTIN> (GRZ_EXCL_GTIN параметр из 14 справочника):
        Если начинается - то необходимо выбирать GTIN из марки подставлять его "как будто" отсканированный ШК=>проставлять чек-боксы в полях КМ и GTIN => проходить контроль GTIN
        Если не начинается - устанавливать чек бокс в поле «Сверка КМ» в поле «Контроль GTIN», количество марок с пройденным контролем не увеличивать
        o чекбокс активен – выполнять проверки, описанные в разделе 2. MARK.ППП. Логика сверки GTIN.*/
        if (checkBoxGtinControl.value == false) {
            if (stampCode.substring(0, 2) == "0${paramGrzExclGtin.value}") {
                checkBoxGtinControl.value = true
                checkBoxGtinStampControl.value = true
            } else {
                checkBoxGtinStampControl.value = true
            }
            addBlock(blockInfo, typeDiscrepancies)
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
            screenNavigator.openAlertGtinDoesNotMatchProductScreen()
            return
        }

        if (checkBoxGtinStampControl.value == true) { //а иначе ничего не делаем (else нету) https://trello.com/c/y2ECoCw4
            val lastScannedGtin =
                    processMarkingProductService.getLastScannedBlock()
                            ?.blockNumber
                            ?.substring(2, 16)
                            .orEmpty()

            if (gtinCode == lastScannedGtin) {
                checkBoxGtinControl.value = true //аналитики сказали сначала поставить чекбокс, а потом снять
                processMarkingProductService.addGtin(gtinCode)
                checkBoxGtinControl.value = false
                checkBoxGtinStampControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять
            } else {
                screenNavigator.openAlertDisparityGTINScreen()
            }
        }

    }

    //https://trello.com/c/y2ECoCw4
    private fun gtinControlCheck(blockInfo: TaskBlockInfo, typeDiscrepancies: String) {
        val gtinCode = blockInfo.blockNumber.substring(2, 16)
        if (blockInfo.blockNumber.substring(0, 2) == "0${paramGrzExclGtin.value}") {
            processMarkingProductService.replaceLastGtin(gtinCode)
            checkBoxGtinControl.value = true
            checkBoxGtinStampControl.value = true
            return
        }

        val lastScannedGtin = processMarkingProductService.getLastScannedGtin().orEmpty()
        if (lastScannedGtin == gtinCode) {
            checkBoxGtinStampControl.value = true //аналитики сказали сначала поставить чекбокс, а потом снять
            processMarkingProductService.addBlocksDiscrepancies(
                    blockInfo = blockInfo,
                    typeDiscrepancies = typeDiscrepancies,
                    isScan = true
            )
            //обновляем кол-во отсканированных марок/блоков для отображения на экране в поле «Контроль марок»
            countBlockScanned.value = countBlockScanned.value
            checkBoxGtinControl.value = false
            checkBoxGtinStampControl.value = false //аналитики сказали сначала поставить чекбокс, а потом снять
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
            spinReasonRejection.value = reasonRejectionInfo.value?.map {
                it.name
            } ?: emptyList()
            count.value = count.value
            screenNavigator.hideProgress()
        }
    }

    fun onClickUnitChange() {

    }

}
