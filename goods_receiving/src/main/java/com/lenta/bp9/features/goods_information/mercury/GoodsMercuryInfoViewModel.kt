package com.lenta.bp9.features.goods_information.mercury

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE
import com.lenta.bp9.model.processing.PROCESSING_MERCURY_QUANT_GREAT_IN_VET_DOC
import com.lenta.bp9.model.processing.PROCESSING_MERCURY_SAVED
import com.lenta.bp9.model.processing.ProcessMercuryProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
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

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val uom: MutableLiveData<Uom?> by lazy {
        if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.DirectSupplier) {
            MutableLiveData(productInfo.value?.purchaseOrderUnits)
        } else {
            MutableLiveData(productInfo.value?.uom)
        }
    }
    val tvAccept: MutableLiveData<String> by lazy {
        MutableLiveData(context.getString(R.string.accept, "${productInfo.value?.purchaseOrderUnits?.name}=${productInfo.value?.quantityInvest?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"))
    }

    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinManufacturers by lazy {
        taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.getManufacturesOfProduct(productInfo.value!!)
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
        context.getString(R.string.vet_with_production_date, it)
    }
    private val isClickApply: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDiscrepancy: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }
    val isGoodsAddedAsSurplus: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.isGoodsAddedAsSurplus == true )
    }
    val isTaskPGE: MutableLiveData<Boolean> by lazy {
        if (taskManager.getReceivingTask()!!.taskHeader.taskType == TaskType.RecalculationCargoUnit) MutableLiveData(true) else MutableLiveData(false)
    }
    val isEizUnit: MutableLiveData<Boolean> = isDiscrepancy.map {
        it == false
    }
    val visibleShelfLife by lazy {
        productInfo.value?.generalShelfLife?.toDouble()!! > 0.0 && productInfo.value?.remainingShelfLife?.toDouble()!! > 0.0
    }

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
        if (it != 0.0) {
            "+ " + it.toStringFormatted() + " " + uom.value?.name
        } else {
            "0 " + uom.value?.name
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
        if (it != 0.0) {
            "- " + it.toStringFormatted() + " " + uom.value?.name
        } else {
            "0 " + uom.value?.name
        }
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
                it!! != 0.0 && !spinManufacturers.isNullOrEmpty() && !spinProductionDate.value.isNullOrEmpty()
    }

    init {
        viewModelScope.launch {
            if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
                when {
                    isGoodsAddedAsSurplus.value == true -> {
                        //todo будет доработанно позже, когда аналитик допишет постановку задачи
                        /**enteredProcessingUnitNumber.value = productInfo.value?.processingUnit ?: ""
                        suffix.value = productInfo.value?.purchaseOrderUnits?.name
                        qualityInfo.value = dataBase.getSurplusInfoForPGE()*/
                    }
                    isDiscrepancy.value!! -> {
                        suffix.value = uom.value?.name
                        count.value = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountProductNotProcessedOfProductPGE(productInfo.value!!).toStringFormatted()
                        qualityInfo.value = dataBase.getQualityInfoPGEForDiscrepancy()
                    }
                    else -> {
                        suffix.value = productInfo.value?.purchaseOrderUnits?.name
                        qualityInfo.value = dataBase.getQualityInfoPGE()
                    }
                }
            } else {
                suffix.value = uom.value?.name
                if (isDiscrepancy.value!!) {
                    qualityInfo.value = dataBase.getQualityInfoForDiscrepancy()
                    spinQualitySelectedPosition.value = 2
                } else {
                    qualityInfo.value = dataBase.getQualityInfo()
                }
            }

            generalShelfLife.value = productInfo.value?.generalShelfLife
            remainingShelfLife.value = productInfo.value?.remainingShelfLife
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
                spinReasonRejectionSelectedPosition.value = 0
                reasonRejectionInfo.value = dataBase.getReasonRejectionInfoOfQuality(selectedQuality)
                spinReasonRejection.value = reasonRejectionInfo.value?.map {
                    it.name
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

    private fun addProductDiscrepanciesPGE() {
        var addNewCount = count.value!!.toDouble()
        if (isEizUnit.value!!) {
            addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: 1.0
        }
        when (processMercuryProductService.checkConditionsOfPreservationPGE(count.value ?: "0", spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])) {
            PROCESSING_MERCURY_SAVED -> {
                processMercuryProductService.add(addNewCount.toString(), count.value ?: "0", qualityInfo.value!![spinQualitySelectedPosition.value!!].code, spinManufacturers!![spinManufacturersSelectedPosition.value!!], spinProductionDate.value!![spinProductionDateSelectedPosition.value!!])
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
}
