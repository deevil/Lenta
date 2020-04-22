package com.lenta.bp9.features.goods_information.mercury

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.*
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class GoodsMercuryInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var dataBase: IDataBaseRepo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var processMercuryProductService: ProcessMercuryProductService
    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var timeMonitor: ITimeMonitor

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val uom: MutableLiveData<Uom?> by lazy {
        if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.DirectSupplier) {
            MutableLiveData(productInfo.value?.purchaseOrderUnits)
        } else {
            MutableLiveData(productInfo.value?.uom)
        }
    }
    val isGoodsAddedAsSurplus: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.isGoodsAddedAsSurplus == true )
    }
    val isTaskPGE: MutableLiveData<Boolean> by lazy {
        if (taskManager.getReceivingTask()!!.taskHeader.taskType == TaskType.RecalculationCargoUnit) MutableLiveData(true) else MutableLiveData(false)
    }
    val isVisibilityProductionDate: MutableLiveData<Boolean> by lazy {
        if (isTaskPGE.value == true && isGoodsAddedAsSurplus.value == true) MutableLiveData(true) else MutableLiveData(false)
    }
    val tvAccept: MutableLiveData<String> by lazy {
        if (isTaskPGE.value == true && isGoodsAddedAsSurplus.value == true) {
            MutableLiveData(context.getString(R.string.accept_txt))
        } else {
            MutableLiveData(context.getString(R.string.accept, "${productInfo.value?.purchaseOrderUnits?.name}=${productInfo.value?.quantityInvest?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"))
        }
    }
    val productionDate: MutableLiveData<String> = MutableLiveData("")
    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("dd.MM.yyyy")
    private val currentDate: MutableLiveData<Date> = MutableLiveData()
    private val expirationDate: MutableLiveData<Calendar> = MutableLiveData()

    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinManufacturers by lazy {
        if (isTaskPGE.value == true && isGoodsAddedAsSurplus.value == true) {
            repoInMemoryHolder.manufacturers.value?.map {
                it.name
            }
        } else {
            taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.getManufacturesOfProduct(productInfo.value!!)
        }
    }
    val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinProductionDate = spinManufacturersSelectedPosition.map {position ->
        taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo.value!!)?.filter {
            it.manufacturer == spinManufacturers!![position!!]
        }?.groupBy {
            it.productionDate
        }?.map {
            it.key
        }
    }
    val spinProductionDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val generalShelfLife: MutableLiveData<String> = MutableLiveData()
    val remainingShelfLife: MutableLiveData<String> = MutableLiveData()
    val paramGrzRoundLackRatio: MutableLiveData<String> = MutableLiveData()
    val paramGrzRoundLackUnit: MutableLiveData<String> = MutableLiveData()
    val paramGrzRoundHeapRatio: MutableLiveData<String> = MutableLiveData()

    private val mercuryVolume  = spinManufacturersSelectedPosition.combineLatest(spinProductionDateSelectedPosition).map {
        val findMercuryInfoOfProduct = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo.value!!)?.filter {taskMercuryInfo ->
            taskMercuryInfo.manufacturer == spinManufacturers!![it!!.first] && taskMercuryInfo.productionDate == spinProductionDate.value!![it.second]
        }
        "${findMercuryInfoOfProduct?.sumByDouble {mercuryInfo ->
            mercuryInfo.volume
        }.toStringFormatted()} ${findMercuryInfoOfProduct?.last()?.uom?.name}"
    }
    val tvProductionDate = mercuryVolume.map {
        if (isTaskPGE.value == true && isGoodsAddedAsSurplus.value == true) {
            context.getString(R.string.vet_with_production_date_txt)
        } else {
            context.getString(R.string.vet_with_production_date, it)
        }
    }
    private val isClickApply: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDiscrepancy: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.combineLatest(isDiscrepancy).map {
        if (taskManager.getReceivingTask()!!.taskHeader.taskType != TaskType.RecalculationCargoUnit) {
            if (!it!!.second) {
                qualityInfo.value?.get(it.first)?.code != "1"
            } else true
        } else {
            if (!it!!.second) {
                qualityInfo.value?.get(it.first)?.code != "1" && qualityInfo.value?.get(it.first)?.code != "2"
            } else true
        }
    }
    val isEizUnit: MutableLiveData<Boolean> by lazy {
        MutableLiveData(isDiscrepancy.value == false && isGoodsAddedAsSurplus.value == false)
    }
    val isPerishable: MutableLiveData<Boolean> = MutableLiveData()

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    private val addGoods: MutableLiveData<Boolean> = MutableLiveData(false)

    val acceptTotalCount: MutableLiveData<Double> by lazy {
        countValue.combineLatest(spinQualitySelectedPosition).map {
            val countAccept = if (isTaskPGE.value!!) {
                processMercuryProductService.getNewCountAcceptPGE()
            } else {
                processMercuryProductService.getNewCountAccept()
            }

            if (isTaskPGE.value!!) {
                if (qualityInfo.value?.get(it!!.second)?.code == "1" || qualityInfo.value?.get(it!!.second)?.code == "2") {
                    convertEizToBei() + countAccept
                } else {
                    countAccept
                }
            } else {
                if (qualityInfo.value?.get(it!!.second)?.code == "1") {
                    (it?.first ?: 0.0) + countAccept
                } else {
                    countAccept
                }
            }
        }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept = if (isTaskPGE.value!!) {
            processMercuryProductService.getNewCountAcceptPGE()
        } else {
            processMercuryProductService.getNewCountAccept()
        }
        if ((it ?: 0.0) > 0.0) {
            "+ ${it.toStringFormatted()} ${uom.value?.name}"
        } else {
            "${if (countAccept > 0.0) "+ " + countAccept.toStringFormatted() else countAccept.toStringFormatted()} ${uom.value?.name}"
        }
    }

    val refusalTotalCount: MutableLiveData<Double> by lazy {
        countValue.combineLatest(spinQualitySelectedPosition).map {
            val countRefusal = if (isTaskPGE.value!!) {
                processMercuryProductService.getNewCountRefusalPGE()
            } else {
                processMercuryProductService.getNewCountRefusal()
            }

            if (isTaskPGE.value!!) {
                if (qualityInfo.value?.get(it!!.second)?.code == "3" || qualityInfo.value?.get(it!!.second)?.code == "4" || qualityInfo.value?.get(it!!.second)?.code == "5") {
                    convertEizToBei() + countRefusal
                } else {
                    countRefusal
                }
            } else {
                if (qualityInfo.value?.get(it!!.second)?.code != "1") {
                    (it?.first ?: 0.0) + countRefusal
                } else {
                    countRefusal
                }
            }
        }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal = if (isTaskPGE.value!!) {
            processMercuryProductService.getNewCountRefusalPGE()
        } else {
            processMercuryProductService.getNewCountRefusal()
        }
        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${uom.value?.name}"
        } else {
            "${if (countRefusal > 0.0) "- " + countRefusal.toStringFormatted() else countRefusal.toStringFormatted()} ${uom.value?.name}"
        }
    }

    private val isNotRecountBreakingCargoUnit: MutableLiveData<Boolean> by lazy { //https://trello.com/c/PRTAVnUP
        MutableLiveData(isTaskPGE.value == true && taskManager.getReceivingTask()!!.taskHeader.isCracked && !taskManager.getReceivingTask()!!.taskDescription.isRecount)
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
        if (isGoodsAddedAsSurplus.value == true) { //карточка трелло https://trello.com/c/eo1nRdKC) (ТП (меркурий по ПГЕ) -> 3.2.2.16 Обработка расхождений при пересчете ГЕ (Меркурий) -> 2.1.Излишек по товару
            (it ?: 0.0) > 0.0 && !spinManufacturers.isNullOrEmpty()
        } else {
            (it ?: 0.0) > 0.0 && !spinManufacturers.isNullOrEmpty() && !spinProductionDate.value.isNullOrEmpty()
        }
    }

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@GoodsMercuryInfoViewModel::viewModelScope,
                    scanResultHandler = this@GoodsMercuryInfoViewModel::handleProductSearchResult)
            currentDate.value = timeMonitor.getServerDate()
            expirationDate.value = Calendar.getInstance()
            if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
                when {
                    isGoodsAddedAsSurplus.value == true -> {
                        suffix.value = uom.value?.name
                        qualityInfo.value = dataBase.getSurplusInfoForPGE()
                    }
                    isDiscrepancy.value!! -> {
                        suffix.value = uom.value?.name
                        count.value = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountProductNotProcessedOfProductPGE(productInfo.value!!).toStringFormatted()
                        if (isNotRecountBreakingCargoUnit.value == true) {
                            qualityInfo.value = dataBase.getQualityInfoPGENotRecountBreaking()
                        } else {
                            qualityInfo.value = dataBase.getQualityInfoPGEForDiscrepancy()
                        }
                    }
                    else -> {
                        suffix.value = productInfo.value?.purchaseOrderUnits?.name
                        if (isNotRecountBreakingCargoUnit.value == true) {
                            qualityInfo.value = dataBase.getQualityInfoPGENotRecountBreaking()
                        } else {
                            qualityInfo.value = dataBase.getQualityInfoPGE()
                        }
                    }
                }
            } else {
                suffix.value = uom.value?.name
                if (isDiscrepancy.value!!) {
                    qualityInfo.value = dataBase.getQualityMercuryInfoForDiscrepancy()
                    spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "7"}
                } else {
                    qualityInfo.value = dataBase.getQualityMercuryInfo()
                }
            }

            /** определяем, что товар скоропорт, это общий для всех алгоритм https://trello.com/c/8sOTWtB7 */
            val paramGrzUffMhdhb = dataBase.getParamGrzUffMhdhb()?.toInt() ?: 60
            isPerishable.value = (productInfo.value?.generalShelfLife?.toInt() ?: 0) > 0 ||
                    (productInfo.value?.remainingShelfLife?.toInt() ?: 0) > 0 ||
                    ((productInfo.value?.mhdhbDays ?: 0) > 0 && (productInfo.value?.mhdhbDays ?: 0) < paramGrzUffMhdhb )
            if (isPerishable.value == true) {
                if ( (productInfo.value?.generalShelfLife?.toInt() ?: 0) > 0 || (productInfo.value?.remainingShelfLife?.toInt() ?: 0) > 0 ) { //https://trello.com/c/XSAxdgjt
                    generalShelfLife.value = productInfo.value?.generalShelfLife
                    remainingShelfLife.value = productInfo.value?.remainingShelfLife
                } else {
                    generalShelfLife.value = productInfo.value?.mhdhbDays.toString()
                    remainingShelfLife.value = productInfo.value?.mhdrzDays.toString()
                }
            }

            spinQuality.value = qualityInfo.value?.map {
                it.name
            }

            paramGrzRoundLackRatio.value = dataBase.getParamGrzRoundLackRatio()
            paramGrzRoundLackUnit.value = dataBase.getParamGrzRoundLackUnit()
            paramGrzRoundHeapRatio.value = dataBase.getParamGrzRoundHeapRatio()

            if (processMercuryProductService.newProcessMercuryProductService(productInfo.value!!) == null) {
                screenNavigator.goBack()
                screenNavigator.openAlertWrongProductType()
            }
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    override fun onClickPosition(position: Int) {
        spinReasonRejectionSelectedPosition.value = position
    }

    fun onClickPositionSpinQuality(position: Int){
        viewModelScope.launch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(qualityInfo.value!![position].code)
        }
    }

    fun onClickPositionSpinManufacturers(position: Int){
        spinManufacturersSelectedPosition.value = position
    }

    fun onClickPositionSpinProductionDate(position: Int){
        spinProductionDateSelectedPosition.value = position
    }

    private suspend fun updateDataSpinReasonRejection(selectedQuality: String) {
        viewModelScope.launch {
            if (isTaskPGE.value == true) {
                spinReasonRejectionSelectedPosition.value = 0
                spinReasonRejection.value = listOf("ЕО - " + productInfo.value!!.processingUnit)
            } else {
                screenNavigator.showProgressLoadingData()
                reasonRejectionInfo.value = dataBase.getReasonRejectionInfoOfQuality(selectedQuality)
                spinReasonRejection.value = reasonRejectionInfo.value?.map {
                    it.name
                }
                if (isDiscrepancy.value!!) {
                    spinReasonRejectionSelectedPosition.value = reasonRejectionInfo.value!!.indexOfLast {it.code == "44"}.let {
                        if (it < 0) {
                            0
                        } else {
                            it
                        }
                    }
                } else {
                    spinReasonRejectionSelectedPosition.value = 0
                }
                count.value = count.value
                screenNavigator.hideProgress()
            }
        }
    }

    fun onClickDetails(){
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    @SuppressLint("SimpleDateFormat")
    fun onClickAdd() {
        if (isTaskPGE.value == true) {
            //меркурий для ПГЕ
            if (isPerishable.value == true) { //https://trello.com/c/fqOMeUob
                expirationDate.value!!.time = SimpleDateFormat("yyyy-MM-dd").parse(spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
                expirationDate.value!!.add(Calendar.DATE, generalShelfLife.value?.toInt() ?: 0)

                if (expirationDate.value!!.time <= currentDate.value) {
                    screenNavigator.openShelfLifeExpiredDialog(
                            yesCallbackFunc = {
                                spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "5"}//устанавливаем брак складской (как и в обычном товаре, Маша Стоян)
                            }
                    )
                } else {
                    if ( Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days > remainingShelfLife.value?.toLong() ?: 0 ) {
                        addProductDiscrepanciesPGE()
                    }  else {
                        screenNavigator.openShelfLifeExpiresDialog(
                                noCallbackFunc = {
                                    spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "5"} //устанавливаем брак складской (как и в обычном товаре, Маша Стоян)
                                },
                                yesCallbackFunc = {
                                    addProductDiscrepanciesPGE()
                                },
                                expiresThrough = Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days.toString()
                        )
                    }
                }
            } else {
                addProductDiscrepanciesPGE()
            }
        } else {
            //меркурий для ППП
            if (isPerishable.value == true) { //https://trello.com/c/fqOMeUob
                expirationDate.value!!.time = SimpleDateFormat("yyyy-MM-dd").parse(spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
                expirationDate.value!!.add(Calendar.DATE, generalShelfLife.value?.toInt() ?: 0)

                if (expirationDate.value!!.time <= currentDate.value) {
                    screenNavigator.openShelfLifeExpiredDialog(
                            yesCallbackFunc = {
                                spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "7"}
                            }
                    )
                } else {
                    if ( Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days > remainingShelfLife.value?.toLong() ?: 0 ) {
                        addProductDiscrepancies()
                    }  else {
                        screenNavigator.openShelfLifeExpiresDialog(
                                noCallbackFunc = {
                                    spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "7"}
                                },
                                yesCallbackFunc = {
                                    addProductDiscrepancies()
                                },
                                expiresThrough = Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days.toString()
                        )
                    }
                }
            } else {
                addProductDiscrepancies()
            }
        }
    }

    private fun addProductDiscrepancies() {
        //меркурий для ППП
        val reasonRejectionCode = if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
            "1"
        } else {
            reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code
        }

        when (processMercuryProductService.checkConditionsOfPreservationOfProduct(
                                            count = count.value ?: "0",
                                            typeDiscrepancies = reasonRejectionCode,
                                            manufacturer = spinManufacturers!![spinManufacturersSelectedPosition.value!!],
                                            productionDate = spinProductionDate.value!![spinProductionDateSelectedPosition.value!!],
                                            paramGrzRoundLackRatio = paramGrzRoundLackRatio.value?.replace(",", ".")?.toDouble() ?: 0.0,
                                            paramGrzRoundLackUnit = paramGrzRoundLackUnit.value?.replace(",", ".")?.toDouble() ?: 0.0,
                                            paramGrzRoundHeapRatio = paramGrzRoundHeapRatio.value?.replace(",", ".")?.toDouble() ?: 0.0)) {
            PROCESSING_MERCURY_SAVED -> {
                if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
                    processMercuryProductService.add(count.value ?: "0", false,"1", spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
                } else {
                    processMercuryProductService.add(count.value ?: "0", false, reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code, spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
                }
                count.value = "0"
                addGoods.value = true
                if (isClickApply.value!!) {
                    processMercuryProductService.save()
                    screenNavigator.goBack()
                }
            }
            PROCESSING_MERCURY_QUANT_GREAT_IN_VET_DOC -> {
                //отображает ошибку - «Введенное кол-во больше чем в ВСД, измените кол-во»
                screenNavigator.openAlertQuantGreatInVetDocScreen()
            }
            PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE -> {
                //отображать ошибку «Введенное кол-во больше чем в ВП, измените кол-во»
                screenNavigator.openAlertQuantGreatInInvoiceScreen()
            }
            PROCESSING_MERCURY_ROUND_QUANTITY_TO_PLANNED -> {
                screenNavigator.openRoundingIssueDialog(
                        noCallbackFunc = {
                            //- В случае, если пользователь отказался округлить, то переходим к п.2 (проверка по ВСД)
                            processMercuryProductService.checkConditionsOfPreservationOfVSD(
                                    count = count.value ?: "0",
                                    typeDiscrepancies = reasonRejectionCode,
                                    manufacturer = spinManufacturers!![spinManufacturersSelectedPosition.value!!],
                                    productionDate = spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
                        },
                        yesCallbackFunc = {
                            //- В случае, если пользователь согласился округлить, то фактическое значение приравнивается к плановому
                            val enteredCount = processMercuryProductService.getRoundingQuantityPPP(count = count.value ?: "0", reasonRejectionCode = reasonRejectionCode)
                            //и переходим к п.2 (проверка по ВСД)
                            processMercuryProductService.checkConditionsOfPreservationOfVSD(
                                                            count = enteredCount.toString(),
                                                            typeDiscrepancies = reasonRejectionCode,
                                                            manufacturer = spinManufacturers!![spinManufacturersSelectedPosition.value!!],
                                                            productionDate = spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
                        }
                )
            }
            PROCESSING_MERCURY_CANT_SAVE_NEGATIVE_NUMBER -> {
                //отображать ошибку «Невозможно сохранить отрицательное количество»
                screenNavigator.openAlertUnableSaveNegativeQuantity()
            }
            PROCESSING_MERCURY_OVERDELIVERY_MORE_EQUAL_NOT_ORDER -> {
                processMercuryProductService.overDeliveryMoreEqualNotOrder(count.value ?: "0", false, reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code, spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
            }
            PROCESSING_MERCURY_OVERDELIVERY_LESS_NOT_ORDER -> {
                processMercuryProductService.overDeliveryLessNotOrder(count.value ?: "0", false, reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code, spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
            }
            PROCESSING_MERCURY_UNKNOWN -> {
                //на Windows Mobile нет действия
            }
        }
        count.value = "0"
    }

    @SuppressLint("SimpleDateFormat")
    private fun addProductDiscrepanciesPGE() {
        val mercuryUom = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo.value!!)?.last { taskMercuryInfo ->
            taskMercuryInfo.manufacturer == spinManufacturers!![spinManufacturersSelectedPosition.value!!] && taskMercuryInfo.productionDate == spinProductionDate.value!![spinProductionDateSelectedPosition.value!!]
        }?.uom

        //https://trello.com/c/yALoQg2b
        val isConvertUnit = uom.value != mercuryUom

        // обработка ПГЕ Меркурия для товаров, которые добавляются как излишек и отсутствуют в поставке (карточка трелло https://trello.com/c/eo1nRdKC) (ТП (меркурий по ПГЕ) -> 3.2.2.16 Обработка расхождений при пересчете ГЕ (Меркурий) -> 2.1.Излишек по товару)
        // добавляем все кол-во введенное пользователем как и для обычного товара (GoodsInfoViewModel - onClickAdd)
        if (isGoodsAddedAsSurplus.value == true) {
            if (!isCorrectDate(productionDate.value)) {
                screenNavigator.openAlertNotCorrectDate()
                return
            } else {
                val productionDateSave = SimpleDateFormat("yyyy-MM-dd").format(formatter.parse(productionDate.value))
                processMercuryProductService.add(convertEizToBei().toString(), isConvertUnit, qualityInfo.value!![spinQualitySelectedPosition.value!!].code, spinManufacturers!![spinManufacturersSelectedPosition.value!!], productionDateSave)
                count.value = "0"
                addGoods.value = true
                if (isClickApply.value!!) {
                    processMercuryProductService.save()
                    screenNavigator.goBack()
                }
                return
            }
        }

        //обработка ПГЕ Меркурия для товаров, которые есть в поставке (карточка трелло https://trello.com/c/eo1nRdKC) (ТП (меркурий по ПГЕ) -> 3.2.2.16 Обработка расхождений при пересчете ГЕ (Меркурий) )
        when (processMercuryProductService.checkConditionsOfPreservationPGE(convertEizToBei(), isConvertUnit, qualityInfo.value!![spinQualitySelectedPosition.value!!].code, spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])) {
            PROCESSING_MERCURY_SAVED -> {
                processMercuryProductService.add(convertEizToBei().toString(), isConvertUnit, qualityInfo.value!![spinQualitySelectedPosition.value!!].code, spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
                count.value = "0"
                addGoods.value = true
                if (isClickApply.value!!) {
                    processMercuryProductService.save()
                    screenNavigator.goBack()
                }
            }
            PROCESSING_MERCURY_QUANT_GREAT_IN_VET_DOC -> { //превышено в ВСД
                screenNavigator.openAlertQuantGreatInVetDocScreen()
            }
            PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE -> { //превышено в поставке
                screenNavigator.openAlertQuantGreatInInvoiceScreen()
            }
            PROCESSING_MERCURY_UNDERLOAD_AND_SURPLUS_IN_ONE_DELIVERY -> { //противоположные категории недогруз и излишек в рамках одной поставки
                screenNavigator.openAlertBothSurplusAndUnderloadScreen()
            }
            PROCESSING_MERCURY_SURPLUS_IN_QUANTITY -> { //2.2.	Излишек по количеству
                screenNavigator.openExceededPlannedQuantityBatchInProcessingUnitDialog(
                        nextCallbackFunc = {
                            processMercuryProductService.addSurplusInQuantityPGE(convertEizToBei(), isConvertUnit, spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
                            count.value = "0"
                            addGoods.value = true
                            if (isClickApply.value!!) {
                                processMercuryProductService.save()
                                screenNavigator.goBack()
                            }
                        }
                )
            }
            PROCESSING_MERCURY_NORM_AND_UNDERLOAD_EXCEEDED_INVOICE -> {//4.Особые случаи, 4.2.1.1 кол-во по поставке превышено
                processMercuryProductService.addNormAndUnderloadExceededInvoicePGE(convertEizToBei(), isConvertUnit, spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
                count.value = "0"
                addGoods.value = true
                if (isClickApply.value!!) {
                    processMercuryProductService.save()
                    screenNavigator.goBack()
                }
            }
            PROCESSING_MERCURY_NORM_AND_UNDERLOAD_EXCEEDED_VET_DOC -> { //4.Особые случаи, 4.2.1.2 кол-во превышает кол-во по ВСД
                processMercuryProductService.addNormAndUnderloadExceededVetDocPGE(convertEizToBei(), isConvertUnit, spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
                count.value = "0"
                addGoods.value = true
                if (isClickApply.value!!) {
                    processMercuryProductService.save()
                    screenNavigator.goBack()
                }
            }
        }
    }

    fun onClickApply() {
        isClickApply.value = true
        onClickAdd()
    }

    fun onScanResult(data: String) {
        addGoods.value = false
        onClickAdd()
        if (addGoods.value == true) {
            searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
        }
    }

    fun onBackPressed() {
        if (processMercuryProductService.modifications()) {
            processMercuryProductService.save()
        }
        screenNavigator.goBack()
    }

    fun onClickUnitChange() {
        isEizUnit.value = !isEizUnit.value!!
        suffix.value = if (isEizUnit.value!!) {
            productInfo.value!!.purchaseOrderUnits.name
        } else {
            productInfo.value!!.uom.name
        }
        count.value = count.value
    }

    private fun isCorrectDate(checkDate: String?): Boolean {
        return try {
            val date = formatter.parse(checkDate)
            !(checkDate != formatter.format(date) || date!! > currentDate.value)
        } catch (e: Exception) {
            false
        }
    }

    private fun convertEizToBei() : Double {
        var addNewCount = countValue.value!!.toDouble()
        if (isEizUnit.value!!) {
            addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: 1.0
        }
        return addNewCount
    }
}
