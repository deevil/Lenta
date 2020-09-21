package com.lenta.bp9.features.goods_information.excise_alco.task_pge.alco_stamp

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessExciseAlcoStampAccPGEService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskExciseStampInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001NetRequest
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001Params
import com.lenta.bp9.requests.network.ZmpUtzGrz31V001Result
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import java.text.ParseException
import java.text.SimpleDateFormat
import javax.inject.Inject


class ExciseAlcoStampAccInfoPGEViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processExciseAlcoStampAccPGEService: ProcessExciseAlcoStampAccPGEService

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var zmpUtzGrz31V001NetRequest: ZmpUtzGrz31V001NetRequest

    @Inject
    lateinit var hyperHive: HyperHive

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinManufacturers: MutableLiveData<List<String>> = MutableLiveData()
    val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinBottlingDate: MutableLiveData<List<String>> = MutableLiveData()
    val spinBottlingDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val exciseStampInfo: MutableLiveData<TaskExciseStampInfo> = MutableLiveData()

    val tvBottlingDate: MutableLiveData<String> by lazy {
        if (productInfo.value?.isRus == true && !(productInfo.value?.origQuantity.isNullOrEmpty())) {
            MutableLiveData(context.getString(R.string.batch_with_bottling_date, productInfo.value?.origQuantity))
        } else if (productInfo.value?.isRus == true && productInfo.value?.origQuantity.isNullOrEmpty()) {
            MutableLiveData(context.getString(R.string.bottling_date))
        } else {
            MutableLiveData(context.getString(R.string.date_of_entry))
        }
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    val isGradeControl: MutableLiveData<Boolean> = MutableLiveData(false)
    val enteredProcessingUnitNumber: MutableLiveData<String> = MutableLiveData("")
    private val scannedStampCode: MutableLiveData<String> = MutableLiveData("")
    val count: MutableLiveData<String> = MutableLiveData("0")
    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    private val countExciseStampsScanned: MutableLiveData<Int> = MutableLiveData(0)
    private val isExciseStampSurplus: MutableLiveData<Boolean> = MutableLiveData(false)
    val isSurplus: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }
    val isGoodsAddedAsSurplus: MutableLiveData<Boolean> by lazy { //https://trello.com/c/WQg659Ww
        MutableLiveData(productInfo.value?.isGoodsAddedAsSurplus == true)
    }
    val isEizUnit: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.purchaseOrderUnits?.code != productInfo.value?.uom?.code && isGoodsAddedAsSurplus.value == false)
    }

    val acceptTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)

        if (qualityInfo.value?.get(it!!.second)?.code == "1" || qualityInfo.value?.get(it!!.second)?.code == "2") {
            convertEizToBei() + countAccept
        } else {
            countAccept
        }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)
        when {
            (it ?: 0.0) > 0.0 -> {
                "+ ${it.toStringFormatted()} ${productInfo.value?.uom?.name}"
            }
            else -> { //если было введено отрицательное значение
                "${if (countAccept > 0.0) "+ " + countAccept.toStringFormatted() else countAccept.toStringFormatted()} ${productInfo.value?.uom?.name}"
            }
        }
    }

    val refusalTotalCount: MutableLiveData<Double> = countValue.combineLatest(spinQualitySelectedPosition).map {
        val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(productInfo.value!!)
        if (qualityInfo.value?.get(it!!.second)?.code == "3" || qualityInfo.value?.get(it!!.second)?.code == "4" || qualityInfo.value?.get(it!!.second)?.code == "5") {
            convertEizToBei() + countRefusal
        } else {
            countRefusal
        }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(productInfo.value!!)

        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${productInfo.value?.uom?.name}"
        } else { //если было введено отрицательное значение
            "${if (countRefusal > 0.0) "- " + countRefusal.toStringFormatted() else countRefusal.toStringFormatted()} ${productInfo.value?.uom?.name}"
        }
    }

    val checkStampControlVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvStampControlVal: MutableLiveData<String> = countValue.combineLatest(spinQualitySelectedPosition).combineLatest(countExciseStampsScanned).map {
        if (qualityInfo.value?.get(it?.first?.second ?: 0)?.code == "1") {
            if ((productInfo.value?.numberBoxesControl?.toInt() == 0 && productInfo.value?.numberStampsControl?.toInt() == 0) ||
                    ((it?.first?.first ?: 0.0) <= 0.0)) {
                checkStampControlVisibility.value = false
                context.getString(R.string.not_required)
            } else {
                checkStampControlVisibility.value = true
                "${it?.second} из ${productInfo.value?.numberStampsControl?.toDouble().toStringFormatted()}"
            }
        } else {
            "" //это поле отображается только при выбранной категории "Норма"
        }
    }

    val checkStampControl: MutableLiveData<Boolean> = checkStampControlVisibility.map {
        (countExciseStampsScanned.value ?: 0) >= (productInfo.value?.numberStampsControl?.toDouble()
                ?: 0.0)
    }

    val checkSurplusControlVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvSurplusControlVal: MutableLiveData<String> = countValue.combineLatest(countExciseStampsScanned).map {
        val enteredCount = it?.first ?: 0.0

        if (enteredCount <= 0.0) {
            checkSurplusControlVisibility.value = false
            context.getString(R.string.not_required)
        } else {
            checkSurplusControlVisibility.value = true
            "${processExciseAlcoStampAccPGEService.getCountExciseStampsSurplusScanned().toDouble().toStringFormatted()} из ${it?.first.toStringFormatted()}"
        }
    }

    val checkBoxSurplusControl: MutableLiveData<Boolean> = checkSurplusControlVisibility.map {
        (countValue.value
                ?: 0.0) > 0.0 && processExciseAlcoStampAccPGEService.getCountExciseStampsSurplusScanned() >= (countValue.value
                ?: 0.0)
    }

    val checkStampListVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvListStampVal: MutableLiveData<String> = countValue.combineLatest(countExciseStampsScanned).map {
        val enteredCount = it?.first ?: 0.0
        val countExciseStampsScannedValue = it?.second.toString()
        val acceptTotalCountValue = acceptTotalCount.value ?: 0.0
        val refusalTotalCountValue = refusalTotalCount.value ?: 0.0
        val productOrderQuantity = productInfo.value?.orderQuantity?.toDoubleOrNull() ?: 0.0
        val totalCount = (acceptTotalCountValue + refusalTotalCountValue + productOrderQuantity).toStringFormatted()

        if (enteredCount <= 0.0) {
            checkStampListVisibility.value = false
            context.getString(R.string.not_required)
        } else {
            checkStampListVisibility.value = true
            "$countExciseStampsScannedValue из $totalCount"
        }
    }

    val checkBoxStampList: MutableLiveData<Boolean> = checkStampListVisibility.map {
        (countValue.value ?: 0.0) > 0.0 && (countExciseStampsScanned.value
                ?: 0) >= (countValue.value ?: 0.0)
    }

    val enabledRollbackBtn: MutableLiveData<Boolean> = countExciseStampsScanned.map {
        (it ?: 0) > 0
    }

    val enabledApplyBtn: MutableLiveData<Boolean> = countValue.combineLatest(spinQualitySelectedPosition).combineLatest(enteredProcessingUnitNumber).map {
        when (qualityInfo.value?.get(it?.first?.second ?: 0)?.code) {
            "1" -> {
                (it?.first?.first ?: 0.0) > 0.0
            }
            "2" -> {
                if (isGoodsAddedAsSurplus.value == true) {
                    checkBoxStampList.value == true && it?.second?.length == 18
                } else {
                    checkBoxSurplusControl.value
                }
            }
            else -> {
                checkBoxStampList.value
            }
        }
    }

    val enabledAddBtn: MutableLiveData<Boolean> = enabledApplyBtn.map {
        if (isGoodsAddedAsSurplus.value == true) {
            false
        } else {
            it
        }
    }

    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat("dd.MM.yyyy")

    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat("yyyy-MM-dd")

    init {
        launchUITryCatch {
            productInfo.value
                    ?.let {
                        if (processExciseAlcoStampAccPGEService.newProcessExciseAlcoStampPGEService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }.orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }
            searchProductDelegate.init(viewModelScope = this@ExciseAlcoStampAccInfoPGEViewModel::viewModelScope,
                    scanResultHandler = this@ExciseAlcoStampAccInfoPGEViewModel::handleProductSearchResult)

            if (isGoodsAddedAsSurplus.value == true) {
                suffix.value = productInfo.value?.uom?.name
                qualityInfo.value = dataBase.getSurplusInfoForPGE()
            } else {
                suffix.value = productInfo.value?.purchaseOrderUnits?.name
                qualityInfo.value = dataBase.getQualityInfoPGENotSurplusNotUnderload()
            }

            spinQuality.value = qualityInfo.value?.map {
                it.name
            }

            count.value = count.value //почему-то без этой строки не выводится в tvBoxControlVal Не требуется, если включить дебаггер, то все отрабатывается, а без дебаггера пришлось дописать эту строчку
        }
    }


    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    fun onClickRollback() {
        processExciseAlcoStampAccPGEService.rollbackScannedExciseStamp()
        //уменьшаем кол-во отсканированных марок на единицу в текущей сессии
        countExciseStampsScanned.value = countExciseStampsScanned.value?.minus(1)
        //возвращаем данные предыдущей остканированной марки, если таковая есть
        val lastExciseStampInfo = processExciseAlcoStampAccPGEService.getLastAddExciseStamp()
        val manufacturerCode = taskManager.getReceivingTask()?.taskRepository?.getBatches()?.getBatches()?.findLast {
            it.batchNumber == lastExciseStampInfo?.batchNumber
        }?.egais ?: ""
        val manufacturerName = repoInMemoryHolder.manufacturers.value?.findLast {
            it.code == manufacturerCode
        }?.name
                ?: repoInMemoryHolder.manufacturers.value?.findLast { manufacture -> //это в случае излишка
                    manufacture.code == lastExciseStampInfo?.organizationCodeEGAIS
                }?.name ?: ""
        spinManufacturers.value = listOf(manufacturerName)

        val dateOfPour = taskManager.getReceivingTask()?.taskRepository?.getBatches()?.getBatches()?.findLast {
            it.batchNumber == lastExciseStampInfo?.batchNumber
        }?.bottlingDate
                ?: lastExciseStampInfo?.bottlingDate //exciseStampInfo.value!!.bottlingDate это в случае излишка
        if (!dateOfPour.isNullOrEmpty()) {
            spinBottlingDate.value = listOf(formatterRU.format(formatterEN.parse(dateOfPour)))
        } else {
            spinBottlingDate.value = listOf("")
        }

        if (countExciseStampsScanned.value!! <= 0) isGradeControl.value = false //отключаем Режим 100% контроля грейда
    }

    fun onClickDetails() {
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickAdd(): Boolean {
        return if (processExciseAlcoStampAccPGEService.overLimit(countValue.value!!)) {
            screenNavigator.openAlertOverLimitAlcoPGEScreen(
                    nextCallbackFunc = {
                        /**По товару ХХХХХХ было превышено количество. Необходимо найти излишек" с кнопками "Назад" и "Далее",
                        по кнопке далее переходить к режиму поиска излишка (14. ПГЕ. Мар.учет. Режим поиска излишка.https://trello.com/c/Axf3evBC)*/
                        setSurplusSearchMode()
                    }
            )
            false
        } else {
            if (isGoodsAddedAsSurplus.value == true) { //GRZ. ПГЕ. Добавление товара, который не числится в задании https://trello.com/c/im9rJqrU
                processExciseAlcoStampAccPGEService.setProcessingUnitNumber(enteredProcessingUnitNumber.value!!)
            }
            processExciseAlcoStampAccPGEService.addProduct(convertEizToBei().toString(), qualityInfo.value!![spinQualitySelectedPosition.value!!].code)
            processExciseAlcoStampAccPGEService.apply()
            true
        }
    }

    fun onClickApply() {
        if (onClickAdd()) screenNavigator.goBack()
    }

    private fun setSurplusSearchMode() {
        launchUITryCatch {
            qualityInfo.value = dataBase.getSurplusInfoForPGE()
            spinQuality.value = qualityInfo.value?.map {
                it.name
            }
            count.value = (acceptTotalCount.value!!.plus(refusalTotalCount.value!!) - productInfo.value!!.orderQuantity.toDouble()).toStringFormatted()
            isSurplus.value = true
        }
    }

    fun onScanResult(data: String) {
        scannedStampCode.value = ""
        when (data.length) {//ПГЕ https://trello.com/c/Bx03dgxE
            68, 150 -> {
                scannedStampCode.value = data
                if (processExciseAlcoStampAccPGEService.exciseStampIsAlreadyProcessed(data)) {
                    screenNavigator.openAlertScannedStampIsAlreadyProcessedScreen() //АМ уже обработана
                } else {
                    exciseStampInfo.value = processExciseAlcoStampAccPGEService.searchExciseStamp(data)
                    if (exciseStampInfo.value == null) {
                        scannedStampNotFound(data)
                    } else {
                        if (exciseStampInfo.value!!.materialNumber != productInfo.value!!.materialNumber) {
                            //Отсканированная марка принадлежит товару <SAP-код> <Название>"
                            screenNavigator.openAlertScannedStampBelongsAnotherProductScreen(exciseStampInfo.value?.materialNumber.orEmpty(), zfmpUtz48V001.getProductInfoByMaterial(exciseStampInfo.value!!.materialNumber)?.name.orEmpty())
                        } else {
                            addExciseStampDiscrepancy()
                        }
                    }
                }
            }
            else -> searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
        }
    }

    private fun addExciseStampDiscrepancy() {
        processExciseAlcoStampAccPGEService.addExciseStampDiscrepancy(
                exciseStamp = exciseStampInfo.value,
                typeDiscrepancies = if (isExciseStampSurplus.value == true) "2" else qualityInfo.value!![spinQualitySelectedPosition.value!!].code, //(Марка-излишек) карточка об этом условии if (isExciseStampSurplus.value == true) "2"
                isScan = true
        )
        //увеличиваем кол-во отсканированных марок на единицу для отображения на экране
        countExciseStampsScanned.value = countExciseStampsScanned.value?.plus(1)
        if (isExciseStampSurplus.value == true || isGoodsAddedAsSurplus.value == true) {
            val manufacturerName = repoInMemoryHolder.manufacturers.value?.findLast { manufacture ->
                manufacture.code == exciseStampInfo.value!!.organizationCodeEGAIS
            }?.name ?: ""
            spinManufacturers.value = listOf(manufacturerName)

            val dateOfPour = exciseStampInfo.value?.bottlingDate.orEmpty()
            if (dateOfPour.isNotEmpty()) {
                spinBottlingDate.value = listOf(formatterRU.format(formatterEN.parse(dateOfPour)))
            }
        } else {
            val manufacturerCode = taskManager.getReceivingTask()?.taskRepository?.getBatches()?.getBatches()?.findLast {
                it.batchNumber == exciseStampInfo.value!!.batchNumber
            }?.egais ?: ""
            val manufacturerName = repoInMemoryHolder.manufacturers.value?.findLast {
                it.code == manufacturerCode
            }?.name ?: repoInMemoryHolder.manufacturers.value?.findLast { manufacture ->
                manufacture.code == exciseStampInfo.value!!.organizationCodeEGAIS
            }?.name ?: ""
            spinManufacturers.value = listOf(manufacturerName)

            val dateOfPour = taskManager.getReceivingTask()?.taskRepository?.getBatches()?.getBatches()?.findLast {
                it.batchNumber == exciseStampInfo.value!!.batchNumber
            }?.bottlingDate
            if (!dateOfPour.isNullOrEmpty()) {
                spinBottlingDate.value = listOf(formatterRU.format(formatterEN.parse(dateOfPour)))
            }
        }

        isExciseStampSurplus.value = false //(Марка-излишек), когда отсканированная марка была сохранена как излишек, сбрасываем эту переменную, чтобы остальные марки при скане не сохранялись как излишек
    }

    private fun scannedStampNotFound(stampCode: String) {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            taskManager.getReceivingTask()?.let { task ->
                val params = ZmpUtzGrz31V001Params(
                        taskNumber = task.taskHeader.taskNumber,
                        materialNumber = productInfo.value!!.materialNumber,
                        boxNumber = "",
                        stampCode = stampCode
                )
                zmpUtzGrz31V001NetRequest(params).either(::handleFailure, ::handleSuccessZmpUtzGrz31)
            }
            screenNavigator.hideProgress()

        }
    }

    private fun handleSuccessZmpUtzGrz31(result: ZmpUtzGrz31V001Result) {
        if (result.taskExciseStamps.isNotEmpty()) {
            exciseStampInfo.value = TaskExciseStampInfo(
                    materialNumber = productInfo.value!!.materialNumber, //берем именно из продукта, уточнила Аюна в скайпе, https://trello.com/c/Axf3evBC
                    code = result.taskExciseStamps[0].code,
                    processingUnitNumber = productInfo.value!!.processingUnit, //берем именно из продукта, уточнила Аюна в скайпе, https://trello.com/c/Axf3evBC
                    batchNumber = result.taskExciseStamps[0].batchNumber,
                    boxNumber = result.taskExciseStamps[0].boxNumber,
                    setMaterialNumber = result.taskExciseStamps[0].setMaterialNumber,
                    organizationCodeEGAIS = result.taskExciseStamps[0].organizationCodeEGAIS,
                    bottlingDate = result.taskExciseStamps[0].bottlingDate
            )
        }

        if (isGoodsAddedAsSurplus.value == true) {
            //https://trello.com/c/WQg659Ww
            when (result.indicatorOnePosition) {
                "1", "2" -> {
                    addExciseStampDiscrepancy()
                }
                "3" -> {
                    screenNavigator.openScannedStampNotIncludedInNetworkLentaDialog("${productInfo.value!!.getMaterialLastSix()} ${productInfo.value!!.description}")
                }
            }
        } else {
            when (result.indicatorOnePosition) {
                "1" -> {
                    screenNavigator.openScannedStampListedInCargoUnitDialog(
                            cargoUnitNumber = result.cargoUnitNumber,
                            nextCallbackFunc = {
                                isExciseStampSurplus.value = true //чтобы сохранить данную марку как излишек
                                isGradeControl.value = true //включаем Режим 100% контроля грейда
                                addExciseStampDiscrepancy()
                            }
                    )
                }
                "2", "3" -> {
                    screenNavigator.openScannedStampNotIncludedInDeliveryDialog(
                            nextCallbackFunc = {
                                isExciseStampSurplus.value = true //чтобы сохранить данную марку как излишек
                                isGradeControl.value = true //включаем Режим 100% контроля грейда
                                addExciseStampDiscrepancy()
                            }
                    )
                }
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure, "97")
    }

    override fun onClickPosition(position: Int) {
        spinQualitySelectedPosition.value = position
    }

    private fun convertEizToBei(): Double {
        var addNewCount = countValue.value!!.toDouble()
        if (isEizUnit.value!!) {
            addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: 1.0
        }
        return addNewCount
    }

    fun onBatchSignsResult(_manufacturerSelectedPosition: Int, _bottlingDate: String) {
        val manufacturerCode = repoInMemoryHolder.manufacturers.value?.get(_manufacturerSelectedPosition)?.code.orEmpty()
        val bottlingDate = _bottlingDate.takeIf { it.length >= 10 }?.run { "${this.substring(6, 10)}-${this.substring(3, 5)}-${this.substring(0, 2)}" }.orEmpty()
        exciseStampInfo.value = TaskExciseStampInfo(
                materialNumber = productInfo.value!!.materialNumber,
                code = scannedStampCode.value.orEmpty(),
                processingUnitNumber = enteredProcessingUnitNumber.value.orEmpty(),
                batchNumber = "",
                boxNumber = "",
                setMaterialNumber = "",
                organizationCodeEGAIS = manufacturerCode.orEmpty(),
                bottlingDate = bottlingDate
        )
        addExciseStampDiscrepancy()
    }

    fun onBatchSignsResult() {
        setSurplusSearchMode()
        val dateOfPour = taskManager.getReceivingTask()?.taskRepository?.getBatches()?.getBatches()?.findLast {
            it.batchNumber == exciseStampInfo.value?.batchNumber.orEmpty()
        }?.bottlingDate.orEmpty()
        try {
            dateOfPour
                    .takeIf { it.isNotEmpty() }
                    ?.let {
                        val formatter = listOf(formatterRU.format(formatterEN.parse(it)))
                        spinBottlingDate.value = formatter
                    }
        } catch (e: ParseException) {
            Logg.e("Parse formatter in onBatchSignsResult()")
        }
    }


    fun onBackPressed() {
        val count = count.value?.toInt()
        if (processExciseAlcoStampAccPGEService.modifications() || count != 0) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        processExciseAlcoStampAccPGEService.clearModifications()
                        screenNavigator.goBack()
                    }
            )
            return
        }
        screenNavigator.goBack()
    }

}
