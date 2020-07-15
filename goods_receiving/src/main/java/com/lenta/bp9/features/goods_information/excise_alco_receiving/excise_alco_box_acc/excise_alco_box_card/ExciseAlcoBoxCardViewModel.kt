package com.lenta.bp9.features.goods_information.excise_alco_receiving.excise_alco_box_acc.excise_alco_box_card

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskBoxInfo
import com.lenta.bp9.model.task.TaskExciseStampInfo
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

class ExciseAlcoBoxCardViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processExciseAlcoBoxAccService: ProcessExciseAlcoBoxAccService

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var hyperHive: HyperHive

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val boxInfo: MutableLiveData<TaskBoxInfo> = MutableLiveData()
    val massProcessingBoxesNumber: MutableLiveData<List<String>> = MutableLiveData()
    val exciseStampInfo: MutableLiveData<TaskExciseStampInfo> = MutableLiveData()
    val selectQualityCode: MutableLiveData<String> = MutableLiveData()
    val selectReasonRejectionCode: MutableLiveData<String> = MutableLiveData()
    val initialCount: MutableLiveData<String> = MutableLiveData()
    val isScan: MutableLiveData<Boolean> = MutableLiveData()
    val enabledSpinCategorySubcategory: MutableLiveData<Boolean> = MutableLiveData()
    val tvAccept: MutableLiveData<String> by lazy {
        MutableLiveData(context.getString(R.string.accept, "${productInfo.value?.purchaseOrderUnits?.name}=${productInfo.value?.quantityInvest?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"))
    }
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinManufacturers: MutableLiveData<List<String>> = MutableLiveData()
    val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinBottlingDate: MutableLiveData<List<String>> = MutableLiveData()
    val spinBottlingDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val tvBottlingDate: MutableLiveData<String> by lazy {
        if (productInfo.value!!.isRus) {
            MutableLiveData(context.getString(R.string.bottling_date))
        } else {
            MutableLiveData(context.getString(R.string.date_of_entry))
        }
    }
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData()
    private val countExciseStampsScanned: MutableLiveData<Int> = MutableLiveData(0)

    val tvStampControlVal: MutableLiveData<String> = countExciseStampsScanned.map {
        "${processExciseAlcoBoxAccService.getCountExciseStampDiscrepanciesOfBox(boxInfo.value?.boxNumber ?: "", "1")} из ${productInfo.value?.numberStampsControl}"
    }

    val checkStampControl: MutableLiveData<Boolean> = countExciseStampsScanned.map {
        //https://trello.com/c/Hve509E5
        if (boxInfo.value != null) {
            processExciseAlcoBoxAccService.stampControlOfBox(boxInfo.value!!)
        } else {
            false
        }
    }

    val checkBoxControl: MutableLiveData<Boolean> = countExciseStampsScanned.map {
        //https://trello.com/c/Hve509E5
        if (boxInfo.value != null) {
            processExciseAlcoBoxAccService.boxControl(boxInfo.value!!)
        } else {
            false
        }
    }

    val visibilityRollbackBtn: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it == 0
    }

    val enabledRollbackBtn: MutableLiveData<Boolean> = spinQualitySelectedPosition.combineLatest(countExciseStampsScanned).map {
        it?.first == 0 && it.second > 0
    }

    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat("dd.MM.yyyy")

    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat("yyyy-MM-dd")

    private val paramGrzCrGrundcatCode: MutableLiveData<String> = MutableLiveData("")
    private val paramGrzCrGrundcatName: MutableLiveData<String> = MutableLiveData("")

    init {
        viewModelScope.launch {
            count.value = initialCount.value
            suffix.value = productInfo.value?.purchaseOrderUnits?.name

            paramGrzCrGrundcatCode.value = dataBase.getParamGrzCrGrundcat() ?: ""
            paramGrzCrGrundcatName.value = dataBase.getGrzCrGrundcatName(paramGrzCrGrundcatCode.value!!)
                    ?: ""

            if (selectReasonRejectionCode.value != null) {
                qualityInfo.value = dataBase.getQualityBoxesDefectInfo()
                enabledSpinCategorySubcategory.value = false
            } else {
                qualityInfo.value = dataBase.getQualityInfo()
                enabledSpinCategorySubcategory.value = true
            }
            spinQuality.value = qualityInfo.value?.map {
                it.name
            }

            val positionQuality = qualityInfo.value?.indexOfLast { it.code == selectQualityCode.value }
                    ?: 0
            spinQualitySelectedPosition.value = if (positionQuality == -1) {
                0
            } else {
                positionQuality
            }
            if (selectReasonRejectionCode.value != null) {
                reasonRejectionInfo.value = dataBase.getReasonRejectionInfoOfQuality(selectQualityCode.value.toString())
                spinReasonRejection.value = reasonRejectionInfo.value?.map {
                    it.name
                }
                val positionReasonRejection = reasonRejectionInfo.value?.indexOfLast { it.code == selectReasonRejectionCode.value }
                        ?: 0
                spinReasonRejectionSelectedPosition.value = if (positionReasonRejection == -1) {
                    0
                } else {
                    positionReasonRejection
                }
            }

            val exciseStampInfoValue = exciseStampInfo.value
            if (exciseStampInfoValue != null) { //значит была отсканирована марка
                boxInfo.value = taskManager
                        .getReceivingTask()
                        ?.taskRepository?.getBoxes()
                        ?.getBoxes()
                        ?.findLast {
                            it.boxNumber == exciseStampInfoValue.boxNumber
                        }

                //typeDiscrepancies передаем 1, т.к. сканирование марок возможно только при выбранной категории Норма
                processExciseAlcoBoxAccService.addExciseStampDiscrepancy(
                        exciseStamp = exciseStampInfoValue,
                        typeDiscrepancies = TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM,
                        isScan = true
                )
                //обновляем кол-во отсканированных марок с категорией норма для отображения на экране
                countExciseStampsScanned.value = processExciseAlcoBoxAccService
                        .getCountExciseStampDiscrepanciesOfBox(
                                boxNumber = boxInfo.value?.boxNumber.orEmpty(),
                                typeDiscrepancies = TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                        )

                //выводим данные о производителе и дате розлива
                updateDateScreenManufacturerDateOfPour()
            }
        }
    }

    fun getDescription(): String {
        return if (massProcessingBoxesNumber.value != null) {
            context.getString(R.string.bulk_box_processing)
        } else {
            val boxNumber = exciseStampInfo.value?.let { stampInfo -> ////значит была отсканирована марка
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getBoxes()
                        ?.getBoxes()
                        ?.findLast { box ->
                            box.boxNumber == stampInfo.boxNumber
                        }
            }?.boxNumber
                    ?: boxInfo.value?.boxNumber
            "${boxNumber?.substring(0, BOX_NUMBER_START_OFFSET)}...${boxNumber?.substring(boxNumber.length - BOX_NUMBER_FINISH_OFFSET)}"
        }
    }

    fun onClickRollback() {
        processExciseAlcoBoxAccService.rollbackScannedExciseStamp()
        //обновляем кол-во отсканированных марок с категорией норма для отображения на экране
        countExciseStampsScanned.value = processExciseAlcoBoxAccService
                .getCountExciseStampDiscrepanciesOfBox(
                        boxNumber = boxInfo.value?.boxNumber.orEmpty(),
                        typeDiscrepancies = TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                )
        //возвращаем данные предыдущей остканированной марки, если таковая есть
        exciseStampInfo.value = processExciseAlcoBoxAccService.getLastAddExciseStamp()
        //выводим данные о производителе и дате розлива
        updateDateScreenManufacturerDateOfPour()
    }

    fun onClickApply() {
        //массовая обработка коробов, по постановке задачи может быть только для брака, можем сюда попасть только с экрана Список коробов ExciseAlcoBoxListFragment
        if (massProcessingBoxesNumber.value != null) {
            massProcessingBoxesNumber.value?.map { boxNumber ->
                processExciseAlcoBoxAccService.searchBox(boxNumber)?.let { box ->
                    reasonRejectionInfo.value?.let { reasonRejection ->
                        processExciseAlcoBoxAccService.addBoxDiscrepancy(
                                boxNumber = box.boxNumber,
                                typeDiscrepancies = reasonRejection[spinReasonRejectionSelectedPosition.value
                                        ?: 0].code,
                                isScan = false
                        )
                        processExciseAlcoBoxAccService.applyBoxCard(box, reasonRejection[spinReasonRejectionSelectedPosition.value
                                ?: 0].code)
                    }
                }
            }
            screenNavigator.goBack()
            return
        }

        //обработка одной коробки
        boxInfo.value?.let { boxInfoValue ->
            val spinQualityPosition = spinQualitySelectedPosition.value ?: 0
            val spinRejectionPosition = spinReasonRejectionSelectedPosition.value ?: 0
            val selectedQualityInfo = qualityInfo.value?.get(spinQualityPosition)
            val selectedReasonRejectionInfo = reasonRejectionInfo.value?.get(spinRejectionPosition)
            val typeDiscrepancies = selectedQualityInfo
                    ?.code
                    ?.takeIf { qualityInfoCode ->
                        qualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                    }
                    ?: selectedReasonRejectionInfo
                            ?.code
            typeDiscrepancies?.let {
                if (processExciseAlcoBoxAccService.searchCurrentBoxDiscrepancies(boxInfoValue.boxNumber) == null) {
                    processExciseAlcoBoxAccService.addBoxDiscrepancy(
                            boxNumber = boxInfoValue.boxNumber,
                            typeDiscrepancies = it,
                            isScan = true
                    )
                }
                processExciseAlcoBoxAccService.applyBoxCard(
                        box = boxInfoValue,
                        typeDiscrepancies = it
                )
            }
            screenNavigator.goBack()
        }
    }

    fun onScanResult(data: String) {
        when (data.length) {
            68, 150 -> {
                if (isDefect.value == false) {//сканирование марок доступно только при категории Норма https://trello.com/c/Wr4xe6L8
                    exciseStampInfo.value = processExciseAlcoBoxAccService.searchExciseStamp(data)
                    if (exciseStampInfo.value == null) {
                        screenNavigator.openScannedStampNotFoundDialog( //Марка не найдена в поставке. Верните товар поставщику. Отсканированная марка будет помечена как проблемная
                                yesCallbackFunc = {
                                    processExciseAlcoBoxAccService.addExciseStampBad(data)
                                }
                        )
                    } else {
                        if (processExciseAlcoBoxAccService.exciseStampIsAlreadyProcessed(data)) {
                            screenNavigator.openAlertScannedStampIsAlreadyProcessedScreen() //АМ уже обработана
                        } else {
                            if (exciseStampInfo.value?.materialNumber != productInfo.value?.materialNumber) {
                                //Отсканированная марка принадлежит товару <SAP-код> <Название>"
                                screenNavigator.openAlertScannedStampBelongsAnotherProductScreen(
                                        materialNumber = exciseStampInfo
                                                .value
                                                ?.materialNumber
                                                .orEmpty(),
                                        materialName = zfmpUtz48V001.getProductInfoByMaterial(
                                                material = exciseStampInfo
                                                        .value
                                                        ?.materialNumber
                                                        .orEmpty()
                                        )?.name.orEmpty()
                                )
                            } else {
                                if (exciseStampInfo.value!!.boxNumber == boxInfo.value?.boxNumber.orEmpty()) {
                                    //typeDiscrepancies передаем 1, т.к. сканирование марок возможно только при выбранной категории Норма
                                    processExciseAlcoBoxAccService.addExciseStampDiscrepancy(
                                            exciseStamp = exciseStampInfo.value!!,
                                            typeDiscrepancies = TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM,
                                            isScan = true
                                    )
                                    //обновляем кол-во отсканированных марок с категорией норма для отображения на экране
                                    countExciseStampsScanned.value = processExciseAlcoBoxAccService.getCountExciseStampDiscrepanciesOfBox(
                                            boxNumber = boxInfo.value?.boxNumber.orEmpty(),
                                            typeDiscrepancies = TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM)
                                    //выводим данные о производителе и дате розлива
                                    updateDateScreenManufacturerDateOfPour()
                                } else {
                                    val realBoxNumber = processExciseAlcoBoxAccService.searchBox(boxNumber = exciseStampInfo.value!!.boxNumber)?.boxNumber.orEmpty()
                                    screenNavigator.openDiscrepancyScannedMarkCurrentBoxDialog( //Отсканированная марка числится в коробке XXXXX...XXXXX. Пометить текущую коробку XXXXX...XXXXX в коробку XXXXX...XXXXX как <GRZ_CR_GRUNDCAT>
                                            yesCallbackFunc = {
                                                processExciseAlcoBoxAccService.addDiscrepancyScannedMarkCurrentBox(
                                                        currentBoxNumber = boxInfo.value!!.boxNumber,
                                                        realBoxNumber = realBoxNumber,
                                                        scannedExciseStampInfo = exciseStampInfo.value!!,
                                                        typeDiscrepancies = paramGrzCrGrundcatCode.value!!
                                                )
                                                //обновляем кол-во отсканированных марок с категорией норма для отображения на экране
                                                countExciseStampsScanned.value = processExciseAlcoBoxAccService
                                                        .getCountExciseStampDiscrepanciesOfBox(
                                                                boxNumber = boxInfo.value?.boxNumber.orEmpty(),
                                                                typeDiscrepancies = TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM)
                                                //выводим данные о производителе и дате розлива
                                                updateDateScreenManufacturerDateOfPour()
                                            },
                                            currentBoxNumber = boxInfo.value?.boxNumber.orEmpty(),
                                            realBoxNumber = realBoxNumber,
                                            paramGrzCrGrundcatName = paramGrzCrGrundcatName.value.orEmpty()
                                    )
                                }
                            }
                        }
                    }
                }
            }
            26 -> {
                val spinQualityPosition = spinQualitySelectedPosition.value
                        ?: 0
                val spinRejectionPosition = spinReasonRejectionSelectedPosition.value
                        ?: 0
                val selectedQualityInfo = qualityInfo.value?.get(spinQualityPosition)
                val selectedReasonRejectionInfo = reasonRejectionInfo.value?.get(spinRejectionPosition)
                val typeDiscrepancies = selectedQualityInfo
                        ?.code
                        ?.takeIf { qualityInfoCode ->
                            qualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                        }
                        ?: selectedReasonRejectionInfo
                                ?.code.orEmpty()
                val box = processExciseAlcoBoxAccService.searchBox(boxNumber = data)
                if (box == null) {
                    screenNavigator.openAlertScannedBoxNotFoundInDeliveryScreen() //Коробка не найдена в поставке.
                } else {
                    if (box.boxNumber == boxInfo.value?.boxNumber) {
                        processExciseAlcoBoxAccService.addBoxDiscrepancy(box.boxNumber, typeDiscrepancies, true)
                        //обновляем кол-во отсканированных марок с категорией норма для отображения на экране
                        countExciseStampsScanned.value = processExciseAlcoBoxAccService
                                .getCountExciseStampDiscrepanciesOfBox(
                                        boxNumber = box.boxNumber,
                                        typeDiscrepancies = TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                                )
                    } else {
                        if (box.materialNumber != productInfo.value!!.materialNumber) {
                            //Отсканированная коробка принадлежит товару <SAP-код> <Название>
                            val productInfoByMaterial = zfmpUtz48V001.getProductInfoByMaterial(box.materialNumber)
                            val materialName = productInfoByMaterial?.name.orEmpty()
                            screenNavigator.openAlertScannedBoxBelongsAnotherProductScreen(
                                    materialNumber = box.materialNumber,
                                    materialName = materialName
                            )
                        } else {
                            boxInfo.value?.let { boxInfoValue ->
                                processExciseAlcoBoxAccService.addBoxDiscrepancy(
                                        boxNumber = boxInfoValue.boxNumber,
                                        typeDiscrepancies = typeDiscrepancies,
                                        isScan = true
                                )
                            }
                            screenNavigator.openExciseAlcoBoxCardScreen(
                                    productInfo = productInfo.value!!,
                                    boxInfo = box,
                                    massProcessingBoxesNumber = null,
                                    exciseStampInfo = null,
                                    selectQualityCode = TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM,
                                    selectReasonRejectionCode = null,
                                    initialCount = "1",
                                    isScan = true
                            )
                        }
                    }
                }
            }
            else -> screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
        }
    }

    override fun onClickPosition(position: Int) {
        spinReasonRejectionSelectedPosition.value = position
    }

    fun onClickPositionSpinQuality(position: Int) {
        viewModelScope.launch {
            spinQualitySelectedPosition.value = position
            if (selectReasonRejectionCode.value != null) { //если это первый вход на экран, то в ф-ции init мы установили значение для spinReasonRejectionSelectedPosition, которое пришло
                selectReasonRejectionCode.value = null //делаем, чтобы в последствии при выборе другого qualityInfo устанавливался первый элемент из списка spinReasonRejection
            } else {
                updateDataSpinReasonRejection(qualityInfo.value!![position].code)
            }
        }
    }

    private suspend fun updateDataSpinReasonRejection(selectedQuality: String) {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            spinReasonRejectionSelectedPosition.value = 0
            reasonRejectionInfo.value = dataBase.getReasonRejectionInfoOfQuality(selectedQuality)
            spinReasonRejection.value = reasonRejectionInfo.value?.map {
                it.name
            }
            count.value = count.value
            screenNavigator.hideProgress()
        }
    }

    fun onBackPressed() {
        if (processExciseAlcoBoxAccService.modifications()) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        processExciseAlcoBoxAccService.clearModifications()
                        screenNavigator.goBack()
                        screenNavigator.openExciseAlcoBoxListScreen(productInfo.value!!, selectQualityCode.value!!, selectReasonRejectionCode.value, initialCount.value!!)
                    }
            )
            return
        }

        screenNavigator.goBack()
    }

    private fun updateDateScreenManufacturerDateOfPour() {
        val manufacturerCode = taskManager.getReceivingTask()?.taskRepository?.getBatches()?.getBatches()?.findLast {
            it.batchNumber == exciseStampInfo.value?.batchNumber
        }?.egais ?: ""
        val manufacturerName = repoInMemoryHolder.manufacturers.value?.findLast {
            it.code == manufacturerCode
        }?.name ?: ""
        spinManufacturers.value = listOf(manufacturerName)

        val dateOfPour = taskManager.getReceivingTask()?.taskRepository?.getBatches()?.getBatches()?.findLast {
            it.batchNumber == exciseStampInfo.value?.batchNumber
        }?.bottlingDate
        if (!dateOfPour.isNullOrEmpty()) {
            spinBottlingDate.value = listOf(formatterRU.format(formatterEN.parse(dateOfPour)))
        } else {
            spinBottlingDate.value = listOf("")
        }
    }

    companion object {
        private const val BOX_NUMBER_START_OFFSET = 4
        private const val BOX_NUMBER_FINISH_OFFSET = 10
    }

}
