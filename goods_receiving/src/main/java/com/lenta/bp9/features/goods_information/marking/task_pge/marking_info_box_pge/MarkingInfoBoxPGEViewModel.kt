package com.lenta.bp9.features.goods_information.marking.task_pge.marking_info_box_pge

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_information.base.BaseGoodsInfo
import com.lenta.bp9.model.processing.ProcessMarkingBoxPGEProductService
import com.lenta.bp9.model.task.TaskBoxInfo
import com.lenta.bp9.model.task.TaskExciseStampInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.requests.network.ZmpUtzGrz44V001NetRequest
import com.lenta.bp9.requests.network.ZmpUtzGrz44V001Params
import com.lenta.bp9.requests.network.ZmpUtzGrz44V001Result
import com.lenta.shared.exception.Failure
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
import com.mobrun.plugin.api.HyperHive
import javax.inject.Inject

class MarkingInfoBoxPGEViewModel : BaseGoodsInfo() {

    @Inject
    lateinit var processMarkingBoxPGEProductService: ProcessMarkingBoxPGEProductService

    @Inject
    lateinit var zmpUtzGrz44V001NetRequest: ZmpUtzGrz44V001NetRequest

    @Inject
    lateinit var hyperHive: HyperHive

    val tvAccept: MutableLiveData<String> by lazy {
        paramGrzAlternMeins.map {
            val uomName = paramGrzAlternMeins.value?.name?.toLowerCase().orEmpty()
            val nestingInOneStamp = productInfo.value?.quantityInvest?.toDouble().toStringFormatted()
            val productUomName = productInfo.value?.uom?.name.orEmpty()
            context.getString(R.string.accept, "$uomName=$nestingInOneStamp $productUomName")
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

    private val unprocessedQuantityOfStamps: MutableLiveData<Double> = MutableLiveData(0.0)
    val isUnitBox: MutableLiveData<Boolean> = MutableLiveData(true)
    val enabled: MutableLiveData<Boolean> = MutableLiveData(false)
    private val currentTypeDiscrepanciesCode = acceptTotalCount.toString()

    private val enteredCountInStampUnits: Double
        get() {
            var addNewCount = countValue.value?.toDouble() ?: 0.0

            if (isUnitBox.value == true) {
                var countPiecesBox = productInfo.value?.countPiecesBox?.toDouble() ?: 1.0
                if (countPiecesBox == 0.0) countPiecesBox = 1.0
                addNewCount *= countPiecesBox
            }
            return addNewCount
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
                    val totalStampProduct = product.origQuantity.toDouble()
                    val countProcessedBoxesCurrentDiscrepancies = processMarkingBoxPGEProductService.getTotalScannedBoxes()
                    if (countBoxScannedValue <= 0) { //фиксируем необработанное количество после первого сканирования марок, чтобы не учитывать их в текущей сессии, иначе это кол-во будет уменьшаться и появиться текст Не требуется
                        unprocessedQuantityOfStamps.value = totalStampProduct - countProcessedBoxesCurrentDiscrepancies
                    }
                    val unprocessedQuantityOfBoxesVal = unprocessedQuantityOfStamps.value ?: 0.0
                    if (currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM) {
                        checkBoxes(unprocessedQuantityOfBoxesVal)
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

    private fun checkBoxes(unprocessedQuantityOfBoxesVal: Double): String {
        return if (unprocessedQuantityOfBoxesVal == enteredCountInStampUnits) {
            checkBoxListVisibility.value = false
            context.getString(R.string.not_required)
        } else {
            checkBoxListVisibility.value = true
            val  numberStampsControl = if (productInfo.value?.numberStampsControl.orEmpty().toInt() > enteredCountInStampUnits.toInt()){
                enteredCountInStampUnits.toStringFormatted()
            } else {
                productInfo.value?.numberStampsControl.orEmpty()
            }
            buildString {
                append(numberStampsControl)
                append(" ")
                append(context.getString(R.string.of))
                append(" ")
                append(enteredCountInStampUnits.toStringFormatted())
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
                append(countStampsControl.toInt())
                append(" ")
                append(paramGrzAlternMeins.value?.name?.toLowerCase().orEmpty())
                append(" ")
                append(MULTIPLY)
                append(" ")
                append(productInfo.value?.numberStampsControl.orEmpty())
            }
        }
    }

    val checkStampControl: MutableLiveData<Boolean> = checkStampControlVisibility.map {
        val countStampScanned = processMarkingBoxPGEProductService.getCountProcessedStampForDiscrepancies(TYPE_DISCREPANCIES_QUALITY_NORM).toDouble()
        val numberStampsControl = productInfo.value?.numberStampsControl?.toDouble() ?: 0.0
        countStampScanned >= numberStampsControl
    }


    val checkBoxListVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val checkBoxList: MutableLiveData<Boolean> = checkBoxListVisibility.map {
        val countBoxScanned = countScannedBoxes.value ?: 0
        (currentTypeDiscrepanciesCode != TYPE_DISCREPANCIES_QUALITY_NORM && it == false)
                || (countBoxScanned >= enteredCountInStampUnits && enteredCountInStampUnits > 0.0)
    }

    val enabledApplyButton: MutableLiveData<Boolean> =
            spinQualitySelectedPosition
                    .combineLatest(checkStampControl)
                    .combineLatest(checkBoxList)
                    .combineLatest(acceptTotalCount)
                    .map {
                        getEnabledApplyButton()
                    }

    private fun getEnabledApplyButton(): Boolean {
        val checkStampControlValue = checkStampControl.value ?: false
        val checkBoxStampListValue = checkBoxList.value ?: false
        val acceptTotalCountValue = acceptTotalCount.value ?: 0.0

        return (enteredCountInStampUnits > 0.0 || (acceptTotalCountValue > 0.0 && currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM))
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
                        if (processMarkingBoxPGEProductService.newProcessMarkingProductService(it) == null) {
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
        processMarkingBoxPGEProductService.initProduct(initProduct)
    }

    fun onClickBoxes() {
        if (processMarkingBoxPGEProductService.isOverLimit(enteredCountInStampUnits)) {
            screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen()
            return
        }
    }

    fun onClickDetails() {
        productInfo.value?.let { screenNavigator.openMarkingGoodsDetailsScreen(it) }
    }


    fun onClickApply() {
        val isAll = if (processMarkingBoxPGEProductService.isOverLimit(enteredCountInStampUnits)) {
                    screenNavigator.openAlertOverLimitPlannedScreen()
                    false
                } else {
                    addInfo()
                }
        if (isAll) screenNavigator.goBack()
    }

    private fun addInfo(): Boolean {
        return if (currentTypeDiscrepanciesCode.isNotEmpty()) {

            if (currentTypeDiscrepanciesCode != TYPE_DISCREPANCIES_QUALITY_NORM) {
                processMarkingBoxPGEProductService.clearModifications()
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

    fun onScanResult(data: String) {
        val acceptTotalCountValue = acceptTotalCount.value ?: 0.0
        if (enteredCountInStampUnits <= 0.0) {
            if (!(currentTypeDiscrepanciesCode == TYPE_DISCREPANCIES_QUALITY_NORM && acceptTotalCountValue > 0.0)) {
                screenNavigator.openAlertMustEnterQuantityInfoGreenScreen()
                return
            }
        }
        checkScanResult(data)
    }

    private fun checkScanResult(data: String) {
        if (barcodeStampCheck(data)) {
            val barcode = barcodeStamp(data)
            checkStampInTask(barcode)
        } else {
            when (data.length) {
                in 18..20 -> checkBoxInTask(data)
                in 0..18 -> checkStampInTask(data)
                else -> screenNavigator.openAlertInvalidBarcodeFormatScreen()
            }
        }
    }

    private fun barcodeStampCheck(data: String): Boolean {
        val regex = REGEX_BARCODE_STAMP.toRegex()
        return regex.find(data) != null
    }

    private fun checkStampInTask(barcode: String) {
        if (processMarkingBoxPGEProductService.stampIsAlreadyProcessed(barcode)) {
            //   screenNavigator.openBoxCard() ->  передаем № короба, в котором карточка
        } else {
            val stamp = taskRepository?.getExciseStamps()?.findExciseStampsOfProduct(productMaterialNumber)?.findLast { it.code == barcode }
            stamp?.let {  //Марка есть в задании
                checkStampCurrentProduct(stamp, barcode)
            }.orIfNull {  // марки нет в задании
                addInfo(barcode, productMaterialNumber)
            }
        }
    }

    private fun checkBoxInTask(barcode: String) {
        if (processMarkingBoxPGEProductService.boxIsAlreadyProcessed(barcode)) {
            screenNavigator.openAlertScannedStampIsAlreadyProcessedAlternativeScreen()
        } else {
            productInfo.value?.let { productInfoValue ->
                val box = taskRepository?.getBoxesRepository()?.findBoxesOfProduct(productInfoValue)?.findLast { it.boxNumber == barcode }
                box?.let {  // Короб в задании
                    boxCheck(barcode, it)
                }.orIfNull {  // Короба нет в задании
                    addInfo(barcode, productMaterialNumber)
                }
            }
        }
    }

    private fun checkStampCurrentProduct(stamp: TaskExciseStampInfo, barcode: String) {
        if (stamp.materialNumber == productMaterialNumber) { //  марка соответствует текущему товару
            addStamp(
                    stampInfo = stamp,
                    typeDiscrepancies = currentTypeDiscrepanciesCode
            )
            stamp.boxNumber?.let {
                checkBoxInTask(it)
            }

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
        val stampInfo = taskManager.getReceivingTask()?.getProcessedExciseStamps()?.findLast {
            it.materialNumber == barcode
        }
        val boxInfo = taskManager.getReceivingTask()?.getProcessedBoxes()?.findLast {
            it.boxNumber == barcode
        }

        if (stampInfo != null) {
            addStamp(
                    stampInfo = stampInfo,
                    typeDiscrepancies = currentTypeDiscrepanciesCode
            )
            //   screenNavigator.openBoxCard() ->  передаем № короба, в котором карточка
        }

        if (boxInfo != null) {
            //сохраняем короб
            addBox(boxInfo, currentTypeDiscrepanciesCode, barcode)
        }
    }

    private fun addInfo(stamp: String, materialNumberStamp: String) {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            zmpUtzGrz44V001NetRequest(ZmpUtzGrz44V001Params(
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber.orEmpty(),
                    stampCode = stamp,
                    materialNumber = materialNumberStamp,
                    boxCode = stamp,
                    blockCode = ""
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


    private fun boxCheck(data: String, taskBoxInfo: TaskBoxInfo) {

        val barcode = if (data.length == 20) {
            data.substring(2, 20)
        } else {
            data
        }

        productInfo.value?.let { productInfoValue ->
            val boxInTask = taskRepository?.getBoxesRepository()?.findBoxesOfProduct(productInfoValue)?.findLast { it.boxNumber == barcode }?.boxNumber.orEmpty()
            if (boxInTask.isEmpty()) {                  //Нет в задании Отсканированная коробка принадлежит товару <SAP-код> <Название>
                val materialName = ZfmpUtz48V001(hyperHive).getProductInfoByMaterial(boxInTask)?.name.orEmpty()
                val materialNumber = ZfmpUtz48V001(hyperHive).getProductInfoByMaterial(boxInTask)?.matcode.orEmpty()
                screenNavigator.openAlertScannedBoxBelongsAnotherProductScreen(
                        materialNumber = materialNumber,
                        materialName = materialName
                )
            } else {
                checkCountProcessed(barcode)
            }
        }
    }



    private fun addBox(boxInfo: TaskBoxInfo, typeDiscrepancies: String, data: String) {
        boxInfo.let {
            typeDiscrepancies.let { currentTypeDiscrepancies ->
                processMarkingBoxPGEProductService
                        .addBoxDiscrepancy(
                                boxNumber = data,
                                typeDiscrepancies = currentTypeDiscrepancies,
                                isScan = true,
                                isDenialOfFullProductAcceptance = false
                        )
            }
        }
    }


    private fun addStamp(stampInfo: TaskExciseStampInfo, typeDiscrepancies: String?) {
        stampInfo.let {
            processMarkingBoxPGEProductService
                    .addStampDiscrepancies(
                            isScan = true,
                            stampInfo = stampInfo
                    )
            countScannedStamps.value = countScannedStamps.value?.plus(1)
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
        if (processMarkingBoxPGEProductService.modifications() || count.value?.toString() != DEFAULT_COUNT) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        processMarkingBoxPGEProductService.clearModifications()
                        screenNavigator.goBack()
                    }
            )
            return
        }
        screenNavigator.goBack()
    }

    companion object {
        private const val DEFAULT_COUNT = "0"
        private const val MULTIPLY = "x"
        const val REGEX_BARCODE_STAMP = """^(?<barcode>01(?<gtin>\d{14})21(?<serial>\S{13})).?(?:240(?<tradeCode>\d{4}))?.?(?:91(?<verificationKey>\S{4}))?.?(?:92(?<verificationCode>\S{88}))?${'$'}"""
    }
}