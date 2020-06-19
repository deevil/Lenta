package com.lenta.bp9.features.goods_information.excise_alco_pge.excise_alco_box_acc_pge.excise_alco_box_card

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccPGEService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskBoxInfo
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
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.mapSkipNulls
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import javax.inject.Inject

class ExciseAlcoBoxCardPGEViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var processExciseAlcoBoxAccPGEService: ProcessExciseAlcoBoxAccPGEService
    @Inject
    lateinit var dataBase: IDataBaseRepo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var hyperHive: HyperHive
    @Inject
    lateinit var zmpUtzGrz31V001NetRequest: ZmpUtzGrz31V001NetRequest

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val boxInfo: MutableLiveData<TaskBoxInfo> = MutableLiveData()
    val massProcessingBoxesNumber: MutableLiveData<List<String>> = MutableLiveData()
    val exciseStampInfo: MutableLiveData<TaskExciseStampInfo> = MutableLiveData()
    val selectQualityCode: MutableLiveData<String> = MutableLiveData()
    val isScan: MutableLiveData<Boolean> = MutableLiveData()
    val isBoxNotIncludedInNetworkLenta: MutableLiveData<Boolean> = MutableLiveData()
    val tvAccept: MutableLiveData<String> by lazy {
        MutableLiveData(context.getString(R.string.accept, "${productInfo.value?.purchaseOrderUnits?.name}=${productInfo.value?.quantityInvest?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"))
    }
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
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
    private val scannedBoxNumber: MutableLiveData<String> = MutableLiveData("")
    private val modifications: MutableLiveData<Boolean> = MutableLiveData(false)
    private val isExciseStampSurplus: MutableLiveData<Boolean> = MutableLiveData(false)

    val count: MutableLiveData<String> = MutableLiveData()
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    private val countExciseStampsScanned: MutableLiveData<Int> = MutableLiveData(0)
    val isEizUnit: MutableLiveData<Boolean> = MutableLiveData(true)
    val enabledETQuantity: MutableLiveData<Boolean> = isEizUnit.combineLatest(isBoxNotIncludedInNetworkLenta).map {
        if (it?.second == true) {
            false
        } else {
            !it?.first!!
        }
    }

    val checkStampControlVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvStampControlVal: MutableLiveData<String> = countExciseStampsScanned.map {
        //ПГЕ https://trello.com/c/iOmIb6N7
        if (productInfo.value?.numberBoxesControl?.toInt() == 0 && productInfo.value?.numberStampsControl?.toInt() == 0 && it == 0) {
            checkStampControlVisibility.value = false
            context.getString(R.string.not_required)
        } else {
            checkStampControlVisibility.value = true
            "${processExciseAlcoBoxAccPGEService.getCountExciseStampDiscrepanciesOfBox(boxInfo.value?.boxNumber ?: "")} из ${productInfo.value?.numberStampsControl}"
        }
    }

    val checkStampControl: MutableLiveData<Boolean> = countExciseStampsScanned.map {
        //ПГЕ https://trello.com/c/iOmIb6N7
        if (boxInfo.value != null) {
            processExciseAlcoBoxAccPGEService.stampControlOfBox(boxInfo.value!!)
        } else {
            false
        }
    }

    val checkBoxControlVisibility: MutableLiveData<Boolean> = MutableLiveData()

    val tvBoxControlVal: MutableLiveData<String> = countExciseStampsScanned.map {
        //ПГЕ https://trello.com/c/iOmIb6N7
        if (productInfo.value?.numberBoxesControl?.toInt() == 0 && productInfo.value?.numberStampsControl?.toInt() == 0) {
            checkBoxControlVisibility.value = false
            context.getString(R.string.not_required)
        } else {
            checkBoxControlVisibility.value = true
            "" //это поле отображается только при 0 грейде
        }
    }

    val checkBoxControl: MutableLiveData<Boolean> = countExciseStampsScanned.map {
        //ПГЕ https://trello.com/c/iOmIb6N7
        if (boxInfo.value != null) {
            processExciseAlcoBoxAccPGEService.boxControl(boxInfo.value!!)
        } else {
            false
        }
    }

    val tvListStampVal: MutableLiveData<String> = count.combineLatest(countExciseStampsScanned).map {
        "${it?.second} из ${it?.first}"
    }

    val checkBoxStampList: MutableLiveData<Boolean> = countExciseStampsScanned.map {
        //ПГЕ https://trello.com/c/iOmIb6N7
        if (boxInfo.value != null && isBoxNotIncludedInNetworkLenta.value == false) {
            processExciseAlcoBoxAccPGEService.stampControlOfBox(boxInfo.value!!)
        } else (it ?: 0) > 0 && isBoxNotIncludedInNetworkLenta.value == true //https://trello.com/c/6NyHp2jB устанавливать чек при первом добавлении марки
    }

    val tvBoxTotalVal: MutableLiveData<String> = countExciseStampsScanned.map {
        "${processExciseAlcoBoxAccPGEService.getCountExciseStampDiscrepanciesOfBox(boxInfo.value?.boxNumber ?: "")} из ${productInfo.value?.quantityInvest?.toDouble().toStringFormatted()}"
    }

    val checkBoxTotal: MutableLiveData<Boolean> = countExciseStampsScanned.map {
        //ПГЕ https://trello.com/c/iOmIb6N7
        if (boxInfo.value != null) {
            processExciseAlcoBoxAccPGEService.boxControl(boxInfo.value!!)
        } else {
            false
        }
    }

    val enabledRollbackBtn: MutableLiveData<Boolean> = countExciseStampsScanned.map {
        (it ?: 0) > 0
    }

    val enabledSpinQuality: MutableLiveData<Boolean> = enabledRollbackBtn.combineLatest(isBoxNotIncludedInNetworkLenta).map {
        if (it?.second == true) {
            false
        } else {
            !it?.first!!
        }
    }

    val enabledApplyBtn: MutableLiveData<Boolean> = isEizUnit.combineLatest(checkBoxStampList).map {
        /**https://trello.com/c/iOmIb6N7
         * Кнопка становится доступной для нажатия, если:
        • режим пересчета равен ЕИЗ
        • режим пересчета равен БЕИ и установлен чекбокс в поле "Список марок"
        • режим пересчета равен БЕИ + Если введенное значение в поле ввода равно всему свободному кол-ву (<свободное кол-во> = <MENGE> - <кол-во марок с не пустой категорией>) -> этот пункт не реализовывал, т.к. на WM такого поведения не наблдюдалось
         */
        it?.first == true || (it?.first == false && it.second == true)
    }

    val enabledAddBtn: MutableLiveData<Boolean> = enabledApplyBtn.combineLatest(isBoxNotIncludedInNetworkLenta).map {
        //https://trello.com/c/iOmIb6N7 и https://trello.com/c/6NyHp2jB
        if (it?.second == true) {
            false
        } else {
            it?.first
        }
    }

    val enabledDetailsBtn: MutableLiveData<Boolean> = isBoxNotIncludedInNetworkLenta.map {
        //https://trello.com/c/6NyHp2jB
        it == false
    }

    val visibilityImgUnit: MutableLiveData<Boolean> = MutableLiveData(true)


    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat("dd.MM.yyyy")
    @SuppressLint("SimpleDateFormat")
    private val formatterEN = SimpleDateFormat("yyyy-MM-dd")

    init {
        viewModelScope.launch {
            count.value = processExciseAlcoBoxAccPGEService.getInitialCount().toStringFormatted()
            suffix.value = productInfo.value?.purchaseOrderUnits?.name

            if (isBoxNotIncludedInNetworkLenta.value == true) { //https://trello.com/c/6NyHp2jB
                qualityInfo.value = dataBase.getSurplusInfoForPGE()
                onClickUnitChange() //вызываем, чтобы Отображать пиктограмму «БЕИ».
                visibilityImgUnit.value = false //Кнопка недоступна для нажатия.
                count.value = processExciseAlcoBoxAccPGEService.getInitialCount().toStringFormatted() //при вызове onClickUnitChange() подставится 0, пожтому обновляем эту переменную здессь
            } else {
                qualityInfo.value = dataBase.getQualityInfoPGENotSurplusNotUnderload()
            }
            spinQuality.value = qualityInfo.value?.map {
                it.name
            }

            val positionQuality = qualityInfo.value?.indexOfLast {it.code == selectQualityCode.value} ?: 0
            spinQualitySelectedPosition.value = if (positionQuality == -1) {
                0
            } else {
                positionQuality
            }

            if (exciseStampInfo.value != null) { //значит была отсканирована марка
                //https://trello.com/c/iOmIb6N7 ситуация 2 по карточке, но в WM работает иначе, отображается ЕИЗ, а не БЕИ, здесь реализовано ка на WM
                boxInfo.value = taskManager.getReceivingTask()?.taskRepository?.getBoxes()?.getBoxes()?.findLast {
                    it.boxNumber == exciseStampInfo.value!!.boxNumber
                }
            }
        }
    }

    fun getDescription() : String {
        return if (massProcessingBoxesNumber.value != null) {
            context.getString(R.string.bulk_box_processing)
        } else {
            val boxNumber = if (exciseStampInfo.value != null) { //значит была отсканирована марка
                taskManager.getReceivingTask()?.taskRepository?.getBoxes()?.getBoxes()?.findLast {box ->
                    box.boxNumber == exciseStampInfo.value!!.boxNumber
                }?.boxNumber
            } else {
                boxInfo.value?.boxNumber
            }
            "${boxNumber?.substring(0,4)}...${boxNumber?.substring(boxNumber.length - 10)}"
        }
    }

    fun onClickRollback() {
        processExciseAlcoBoxAccPGEService.rollbackScannedExciseStamp()
        //уменьшаем кол-во отсканированных марок на единицу в текущей сессии
        countExciseStampsScanned.value = countExciseStampsScanned.value?.minus(1)
        //возвращаем данные предыдущей остканированной марки, если таковая есть
        val lastExciseStampInfo = processExciseAlcoBoxAccPGEService.getLastAddExciseStamp()
        val manufacturerCode = taskManager.getReceivingTask()?.taskRepository?.getBatches()?.getBatches()?.findLast {
            it.batchNumber == lastExciseStampInfo?.batchNumber
        }?.egais ?: ""
        val manufacturerName = repoInMemoryHolder.manufacturers.value?.findLast {
            it.code == manufacturerCode
        }?.name ?: ""
        spinManufacturers.value = listOf(manufacturerName)

        val dateOfPour = taskManager.getReceivingTask()?.taskRepository?.getBatches()?.getBatches()?.findLast {
            it.batchNumber == lastExciseStampInfo?.batchNumber
        }?.bottlingDate
        if (!dateOfPour.isNullOrEmpty()) {
            spinBottlingDate.value = listOf(formatterRU.format(formatterEN.parse(dateOfPour)))
        } else {
            spinBottlingDate.value = listOf("")
        }
    }

    fun onClickDetails() {
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickAdd() {
        visibilityImgUnit.value = false //https://trello.com/c/iOmIb6N7  кнопка изменения ЕИ доступна, пока данная коробка не была сохранена в обработанные по кнопке "Добавить"/"Применить"

        //массовая обработка коробов, по постановке задачи может быть только для брака, можем сюда попасть только с экрана Список коробов ExciseAlcoBoxListFragment
        if (massProcessingBoxesNumber.value != null) {
            massProcessingBoxesNumber.value?.map {boxNumber ->
                processExciseAlcoBoxAccPGEService.searchBox(boxNumber)?.let {
                    processExciseAlcoBoxAccPGEService.applyBoxCard(it, qualityInfo.value!![spinQualitySelectedPosition.value!!].code, isScan.value!!)
                }
            }
            return
        }

        //обработка одной коробки
        boxInfo.value?.let {
            processExciseAlcoBoxAccPGEService.applyBoxCard(it, qualityInfo.value!![spinQualitySelectedPosition.value!!].code, isScan.value!!)
            //обнуляем кол-во отсканированных марок в текущей сессии
            countExciseStampsScanned.value = 0
        }
    }

    fun onClickApply() {
        onClickAdd()

        //массовая обработка коробов, по постановке задачи может быть только для брака, можем сюда попасть только с экрана Список коробов ExciseAlcoBoxListFragment
        if (massProcessingBoxesNumber.value != null) {
            screenNavigator.goBack()
            return
        }

        if (checkStampControl.value == true && isBoxNotIncludedInNetworkLenta.value == false) { //isBoxNotIncludedInNetworkLenta.value == false по карточке https://trello.com/c/6NyHp2jB, если false, то переходим на else
            screenNavigator.openExciseAlcoBoxAccInfoPGEScreen(productInfo.value!!)
        } else {
            screenNavigator.goBack()
            screenNavigator.openExciseAlcoBoxListPGEScreen(
                    productInfo = productInfo.value!!,
                    selectQualityCode = qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code ?: "1"
            )
            if (isBoxNotIncludedInNetworkLenta.value == true) {
                processExciseAlcoBoxAccPGEService.decreaseByOneInitialCount()
                screenNavigator.openAlertAmountNormWillBeReduced()
            }
        }
    }

    fun onScanResult(data: String) {
        when (data.length) {
            68, 150 -> {
                exciseStampInfo.value = processExciseAlcoBoxAccPGEService.searchExciseStamp(data)
                if (isBoxNotIncludedInNetworkLenta.value == true) { //https://trello.com/c/6NyHp2jB Карточка короба-излишка
                    /**проводить проверки:
                    • Наличие марки в ET_TASK_MARK
                    1. Марка присутствует в задании – выводить экран с сообщением «Марка присутствует в задании, добавление излишка недоступно».
                    2. Марка отсутствует в задании – добавлять марку в список.
                     */
                    if (exciseStampInfo.value == null) {
                        screenNavigator.openAlertExciseStampPresentInTask()
                    } else {
                        addExciseStampDiscrepancy()
                    }
                } else { //https://trello.com/c/iOmIb6N7 Карточка короба
                    if (exciseStampInfo.value == null) {
                        screenNavigator.openScannedStampBoxPGENotFoundDialog( //Отсканированная марка не найдена. Пометить ее как излишек? В случае согласия необходимо будет отсканировать все марки в текущей коробке.
                                nextCallbackFunc = {
                                    //https://trello.com/c/lWYJ43Pe
                                    visibilityImgUnit.value = false //Кнопка недоступна для нажатия.
                                    if (isEizUnit.value == true) { //если установлена ЕИЗ
                                        onClickUnitChange() //вызываем, чтобы Отображать пиктограмму «БЕИ».
                                    }
                                    isExciseStampSurplus.value = true //чтобы сохранить данную марку как излишек
                                    addExciseStampDiscrepancy()
                                }
                        )
                    } else {
                        if (processExciseAlcoBoxAccPGEService.exciseStampIsAlreadyProcessed(data)) {
                            screenNavigator.openAlertScannedStampIsAlreadyProcessedScreen() //АМ уже обработана
                        } else {
                            if (exciseStampInfo.value!!.materialNumber != productInfo.value!!.materialNumber) {
                                //Отсканированная марка принадлежит товару <SAP-код> <Название>"
                                screenNavigator.openAlertScannedStampBelongsAnotherProductScreen(exciseStampInfo.value!!.materialNumber, zfmpUtz48V001.getProductInfoByMaterial(exciseStampInfo.value!!.materialNumber)?.name ?: "")
                            } else {
                                if (exciseStampInfo.value!!.boxNumber == (boxInfo.value?.boxNumber ?: "")) {
                                    addExciseStampDiscrepancy()
                                } else {//https://trello.com/c/E4b0z0q5
                                    val realBoxNumber = processExciseAlcoBoxAccPGEService.searchBox(boxNumber = exciseStampInfo.value!!.boxNumber)?.boxNumber ?: ""
                                    screenNavigator.openDiscrepancyScannedMarkCurrentBoxPGEDialog( //Отсканированная(ый) марка/блок числится в другой коробке. Необходимо отсканировать все марки/блоки в текущей коробке и коробке № XXXXXXX
                                            nextCallbackFunc = {
                                                processExciseAlcoBoxAccPGEService.addDiscrepancyScannedMarkCurrentBox(
                                                        currentBoxNumber = boxInfo.value!!.boxNumber,
                                                        realBoxNumber = realBoxNumber,
                                                        scannedExciseStampInfo = exciseStampInfo.value!!,
                                                        typeDiscrepancies = "1"
                                                )
                                            },
                                            realBoxNumber = "${realBoxNumber?.substring(0,4)}...${realBoxNumber?.substring(realBoxNumber.length - 10)}"
                                    )
                                    /**2.4.1. Конвертировать ЕИЗ в БЕИ (т.е. 1 кор перевести в шт, используя параметр - QNTINCL) и отображать полученное кол-во в БЕИ в поле ввода, значение доступно для редактирования
                                    2.4.2. Уменьшать количество нормы на 1 шт в БЕИ в поле ввода кол-ва.*/
                                    if (isEizUnit.value == true) { //если установлена ЕИЗ
                                        onClickUnitChange() //вызываем, чтобы Отображать пиктограмму «БЕИ».
                                    }
                                    count.value = (convertEizToBei() - 1).toStringFormatted()
                                }
                            }
                        }
                    }
                }
            }
            26 -> {
                if (enabledApplyBtn.value == true) { //Функция доступна только при условии, что доступна кнопка "Применить". (ПГЕ https://trello.com/c/TzUSGIH7
                    if (processExciseAlcoBoxAccPGEService.getCountBoxOfProductOfDiscrepancies(data) >= processExciseAlcoBoxAccPGEService.getCountAcceptRefusal() ) {
                        screenNavigator.openAlertRequiredQuantityBoxesAlreadyProcessedScreen() //Необходимое количество коробок уже обработано
                    } else {
                        val boxInfo = processExciseAlcoBoxAccPGEService.searchBox(boxNumber = data)
                        if (boxInfo == null) {
                            scannedBoxNumber.value = data
                            scannedBoxNotFound(data)
                        } else {
                            if (boxInfo.materialNumber != productInfo.value!!.materialNumber) {
                                //Отсканированная коробка принадлежит товару <SAP-код> <Название>
                                screenNavigator.openAlertScannedBoxBelongsAnotherProductScreen(materialNumber = boxInfo.materialNumber, materialName = zfmpUtz48V001.getProductInfoByMaterial(boxInfo.materialNumber)?.name
                                        ?: "")
                            } else {
                                isScan.value = true
                                onClickApply()
                                screenNavigator.openExciseAlcoBoxCardPGEScreen(
                                        productInfo = productInfo.value!!,
                                        boxInfo = boxInfo,
                                        massProcessingBoxesNumber = null,
                                        exciseStampInfo = null,
                                        selectQualityCode = selectQualityCode.value ?: "",
                                        isScan = true,
                                        isBoxNotIncludedInNetworkLenta = false
                                )
                            }
                        }
                    }
                }
            }
            else -> screenNavigator.openAlertInvalidBarcodeFormatScannedScreen()
        }
    }

    private fun addExciseStampDiscrepancy() {
        processExciseAlcoBoxAccPGEService.addExciseStampDiscrepancy(
                exciseStamp = exciseStampInfo.value!!,
                typeDiscrepancies = if (isExciseStampSurplus.value == true) "2" else qualityInfo.value!![spinQualitySelectedPosition.value!!].code, //https://trello.com/c/lWYJ43Pe (Марка-излишек внутри коробки) карточка об этом условии if (isExciseStampSurplus.value == true) "2"
                isScan = true
        )
        isExciseStampSurplus.value = false //это из карточки https://trello.com/c/lWYJ43Pe (Марка-излишек внутри коробки), когда отсканированная марка была сохранена как излишек, сбрасываем эту переменную, чтобы остальные марки при скане не сохранялись как излишек
        //увеличиваем кол-во отсканированных марок на единицу для отображения на экране
        countExciseStampsScanned.value = countExciseStampsScanned.value?.plus(1)
        val manufacturerCode = taskManager.getReceivingTask()?.taskRepository?.getBatches()?.getBatches()?.findLast {
            it.batchNumber == exciseStampInfo.value!!.batchNumber
        }?.egais ?: ""
        val manufacturerName = repoInMemoryHolder.manufacturers.value?.findLast {
            it.code == manufacturerCode
        }?.name ?: ""
        spinManufacturers.value = listOf(manufacturerName)

        val dateOfPour = taskManager.getReceivingTask()?.taskRepository?.getBatches()?.getBatches()?.findLast {
            it.batchNumber == exciseStampInfo.value!!.batchNumber
        }?.bottlingDate
        if (!dateOfPour.isNullOrEmpty()) {
            spinBottlingDate.value = listOf(formatterRU.format(formatterEN.parse(dateOfPour)))
        }
    }

    private fun scannedBoxNotFound(boxNumber: String) {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            taskManager.getReceivingTask()?.let { task ->
                val params = ZmpUtzGrz31V001Params(
                        taskNumber = task.taskHeader.taskNumber,
                        materialNumber = productInfo.value!!.materialNumber,
                        boxNumber = boxNumber,
                        stampCode = ""
                )
                zmpUtzGrz31V001NetRequest(params).either(::handleFailure, ::handleSuccessZmpUtzGrz31)
            }
            screenNavigator.hideProgress()

        }
    }

    private fun handleSuccessZmpUtzGrz31(result: ZmpUtzGrz31V001Result) {
        when (result.indicatorOnePosition) {
            "1" -> {
                screenNavigator.openScannedBoxListedInCargoUnitDialog(
                        cargoUnitNumber = result.cargoUnitNumber,
                        nextCallbackFunc = {
                            processExciseAlcoBoxAccPGEService.addAllAsSurplusForBox(count = convertEizToBei().toString(), boxNumber = scannedBoxNumber.value ?: "", typeDiscrepancies = "2", isScan = true)
                            screenNavigator.goBack()
                            screenNavigator.openExciseAlcoBoxListPGEScreen(
                                    productInfo = productInfo.value!!,
                                    selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code
                            )
                        }
                )
            }
            "2" -> {
                screenNavigator.openScannedBoxNotIncludedInDeliveryDialog(
                        nextCallbackFunc = {
                            processExciseAlcoBoxAccPGEService.addAllAsSurplusForBox(count = convertEizToBei().toString(), boxNumber = scannedBoxNumber.value ?: "", typeDiscrepancies = "2", isScan = true)
                            screenNavigator.goBack()
                            screenNavigator.openExciseAlcoBoxListPGEScreen(
                                    productInfo = productInfo.value!!,
                                    selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code
                            )
                        }
                )
            }
            "3" -> {
                screenNavigator.openScannedBoxNotIncludedInNetworkLentaDialog(
                        nextCallbackFunc = { //https://trello.com/c/6NyHp2jB 11. ПГЕ. Излишки. Карточка короба-излишка (не числится в ленте)
                            val boxInfo = processExciseAlcoBoxAccPGEService.searchBox(boxNumber = scannedBoxNumber.value ?: "")
                            screenNavigator.goBack()
                            screenNavigator.openExciseAlcoBoxCardPGEScreen(
                                    productInfo = productInfo.value!!,
                                    boxInfo = boxInfo,
                                    massProcessingBoxesNumber = null,
                                    exciseStampInfo = null,
                                    selectQualityCode = qualityInfo.value!![spinQualitySelectedPosition.value!!].code,
                                    isScan = true,
                                    isBoxNotIncludedInNetworkLenta = true
                            )
                        }
                )
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure, "97")
    }

    override fun onClickPosition(position: Int) {
        spinQualitySelectedPosition.value = position
    }

    fun onClickUnitChange() {
        if (!processExciseAlcoBoxAccPGEService.boxProcessed(boxInfo.value?.boxNumber ?: "")) { //https://trello.com/c/iOmIb6N7 кнопка доступна, если данная коробка не была сохранена в обработанные
            if ((countExciseStampsScanned.value ?: 0) > 0) {
                screenNavigator.openBoxCardUnsavedDataConfirmationDialog(
                        nextCallbackFunc = {
                            modifications.value = true
                            isEizUnit.value = !isEizUnit.value!!
                            if (isEizUnit.value!!) {
                                count.value = "1"
                                suffix.value = productInfo.value?.purchaseOrderUnits?.name
                            } else {
                                count.value = "0"
                                suffix.value = productInfo.value?.uom?.name
                            }
                            val countStamps = countExciseStampsScanned.value!!
                            for (i in 1..countStamps) {
                                onClickRollback()
                            }
                        }
                )
            } else {
                modifications.value = true
                isEizUnit.value = !isEizUnit.value!!
                if (isEizUnit.value!!) {
                    count.value = "1"
                    suffix.value = productInfo.value?.purchaseOrderUnits?.name
                } else {
                    count.value = "0"
                    suffix.value = productInfo.value?.uom?.name
                }
            }
        }
    }

    private fun convertEizToBei(): Double {
        var addNewCount = countValue.value!!.toDouble()
        if (isEizUnit.value!!) {
            addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: 1.0
        }
        return addNewCount
    }

    fun onBackPressed() {
        if (processExciseAlcoBoxAccPGEService.modifications() || modifications.value!!) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        processExciseAlcoBoxAccPGEService.clearModifications()
                        screenNavigator.goBack()
                        screenNavigator.openExciseAlcoBoxListPGEScreen(
                                productInfo = productInfo.value!!,
                                selectQualityCode = selectQualityCode.value!!
                        )
                    }
            )
            return
        }

        screenNavigator.goBack()
    }
}
