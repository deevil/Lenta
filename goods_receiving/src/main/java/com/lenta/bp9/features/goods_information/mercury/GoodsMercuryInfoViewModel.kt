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
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
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

    private val mercuryVolume  = spinManufacturersSelectedPosition.combineLatest(spinProductionDateSelectedPosition).map {
        val findMercuryInfoOfProduct = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo.value!!)?.filter {taskMercuryInfo ->
            taskMercuryInfo.manufacturer == spinManufacturers!![it!!.first] && taskMercuryInfo.productionDate == spinProductionDate.value!![it.second]
        }
        "${findMercuryInfoOfProduct?.sumByDouble {mercuryInfo ->
            mercuryInfo.volume
        }.toStringFormatted()} ${uom.value?.name}"
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
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }
    val isEizUnit: MutableLiveData<Boolean> by lazy {
        MutableLiveData(isDiscrepancy.value == false && isGoodsAddedAsSurplus.value == false)
    }
    val isPerishable: MutableLiveData<Boolean> = MutableLiveData()

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> by lazy {
        countValue.combineLatest(spinQualitySelectedPosition).map {
            val countAccept = if (isTaskPGE.value!!) {
                processMercuryProductService.getNewCountAcceptPGE()
            } else {
                processMercuryProductService.getNewCountAccept()
            }

            if (isTaskPGE.value!!) {
                var addNewCount = it?.first ?: 0.0
                if (isEizUnit.value!!) {
                    addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: 1.0
                }
                if (qualityInfo.value?.get(it!!.second)?.code == "1" || qualityInfo.value?.get(it!!.second)?.code == "2") {
                    addNewCount + countAccept
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
                var addNewCount = it?.first ?: 0.0
                if (isEizUnit.value!!) {
                    addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: 1.0
                }
                if (qualityInfo.value?.get(it!!.second)?.code == "3" || qualityInfo.value?.get(it!!.second)?.code == "4" || qualityInfo.value?.get(it!!.second)?.code == "5") {
                    addNewCount + countRefusal
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
        (it ?: 0.0) > 0.0 && !spinManufacturers.isNullOrEmpty() && !spinProductionDate.value.isNullOrEmpty()
    }

    init {
        viewModelScope.launch {
            currentDate.value = timeMonitor.getServerDate()
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

            generalShelfLife.value = productInfo.value?.generalShelfLife
            remainingShelfLife.value = productInfo.value?.remainingShelfLife

            /** определяем, что товар скоропорт, это общий для всех алгоритм https://trello.com/c/8sOTWtB7 */
            val paramGrzUffMhdhb = dataBase.getParamGrzUffMhdhb()?.toInt() ?: 60
            isPerishable.value = (productInfo.value?.generalShelfLife?.toInt() ?: 0) > 0 ||
                    (productInfo.value?.remainingShelfLife?.toInt() ?: 0) > 0 ||
                    ((productInfo.value?.mhdhbDays ?: 0) > 0 && (productInfo.value?.mhdhbDays ?: 0) < paramGrzUffMhdhb )

            spinQuality.value = qualityInfo.value?.map {
                it.name
            }

            if (processMercuryProductService.newProcessMercuryProductService(productInfo.value!!) == null) {
                screenNavigator.goBack()
                screenNavigator.openAlertWrongProductType()
            }
        }
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
                    spinReasonRejectionSelectedPosition.value = reasonRejectionInfo.value!!.indexOfLast {it.code == "44"}
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

    fun onClickAdd() {
        //меркурий для ПГЕ
        if (isTaskPGE.value == true) {
            addProductDiscrepanciesPGE()
            return
        }

        //меркурий для ППП
        val reasonRejectionCode = if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
            "1"
        } else {
            reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code
        }
        when (processMercuryProductService.checkConditionsOfPreservation(count.value ?: "0", reasonRejectionCode, spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])) {
            PROCESSING_MERCURY_SAVED -> {
                if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
                    processMercuryProductService.add(acceptTotalCount.value!!.toString(), count.value ?: "0",  "1", spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
                } else {
                    processMercuryProductService.add(count.value ?: "0", count.value ?: "0", reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code, spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
                }
                count.value = "0"
                if (isClickApply.value!!) {
                    processMercuryProductService.save()
                    screenNavigator.goBack()
                }
            }
            PROCESSING_MERCURY_QUANT_GREAT_IN_VET_DOC -> {
                screenNavigator.openAlertQuantGreatInVetDocScreen()
            }
            PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE -> {
                screenNavigator.openAlertQuantGreatInInvoiceScreen()
            }
        }
        count.value = "0"
    }

    @SuppressLint("SimpleDateFormat")
    private fun addProductDiscrepanciesPGE() {
        //todo товар, который добавляется как излишек и отсутствует в поставке, должна быть доработана логика со стороны аналитиков (сказал Дима С.), пока не обрабатывается (карточка трелло https://trello.com/c/eo1nRdKC) (ТП (меркурий по ПГЕ) -> 3.2.2.16 Обработка расхождений при пересчете ГЕ (Меркурий) -> 2.1.Излишек по товару)
        if (isGoodsAddedAsSurplus.value == true) {
            if (!isCorrectDate(productionDate.value)) {
                screenNavigator.openAlertNotCorrectDate()
                return
            } else {
                val productionDateSave = SimpleDateFormat("yyyy-MM-dd").format(formatter.parse(productionDate.value))
                return //не обрабатываем, просто выходим из ф-ции
            }
        }


        //обработка ПГЕ Меркурия для товаров, которые есть в поставке
        var addNewCount = count.value!!.toDouble()
        if (isEizUnit.value!!) {
            addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: 1.0
        }
        when (processMercuryProductService.checkConditionsOfPreservationPGE(addNewCount, qualityInfo.value!![spinQualitySelectedPosition.value!!].code, spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])) {
            PROCESSING_MERCURY_SAVED -> {
                processMercuryProductService.add(addNewCount.toString(), addNewCount.toString(), qualityInfo.value!![spinQualitySelectedPosition.value!!].code, spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
                count.value = "0"
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
            PROCESSING_MERCURY_SURPLUS_PGE_ENOUGH_QUANTITY -> { //2.2.	Излишек по количеству (хватило кол-ва по текущей партии)
                screenNavigator.openExceededPlannedQuantityInProcessingUnitDialog(
                        nextCallbackFunc = {
                            processMercuryProductService.addSurplusPGEEnoughQuantity(addNewCount, spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
                            count.value = "0"
                            if (isClickApply.value!!) {
                                processMercuryProductService.save()
                                screenNavigator.goBack()
                            }
                        }
                )
            }
            PROCESSING_MERCURY_SURPLUS_PGE_NOT_HAVE_ENOUGH_QUANTITY -> { //2.3.	Излишек по количеству (не достаточно количеств по текущей партии)
                screenNavigator.openExceededPlannedQuantityBatchInProcessingUnitDialog(
                        nextCallbackFunc = {
                            //todo здесь затрагиваются партии, уточнить у аналитика, возможна реализациия, наверное, только после реализации алкоголя, карточка трелло https://trello.com/c/eo1nRdKC (ТП (меркурий по ПГЕ) -> 3.2.2.16 Обработка расхождений при пересчете ГЕ (Меркурий) -> 2.3.	Излишек по количеству (не достаточно количеств по текущей партии))
                            screenNavigator.goBack()
                        }
                )
            }
        }
    }

    fun onClickApply() {
        isClickApply.value = true
        onClickAdd()
    }

    fun onScanResult(data: String) {
        searchProductDelegate.searchCode(code = data, fromScan = true)
    }

    fun onBackPressed() {
        if (processMercuryProductService.modifications()) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        screenNavigator.goBack()
                    }
            )
        } else {
            screenNavigator.goBack()
        }
    }

    fun onClickUnitChange() {
        isEizUnit.value = !isEizUnit.value!!
        suffix.value = if (isEizUnit.value!!) {
            productInfo.value?.purchaseOrderUnits?.name
        } else {
            productInfo.value?.uom?.name
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
}
