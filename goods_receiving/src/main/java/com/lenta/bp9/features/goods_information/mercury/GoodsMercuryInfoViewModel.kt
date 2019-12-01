package com.lenta.bp9.features.goods_information.mercury

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.*
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.Logg
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
    lateinit var processMercuryProductService: ProcessMercuryProductService
    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinManufacturers by lazy {
        taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo.value!!)?.manufacturers ?: listOf("Manufacturer 1", "Manufacturer 2", "Manufacturer 3")
    }
    val spinManufacturersSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinProductionDate by lazy {
        taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo.value!!)?.productionDates ?: listOf("date 1", "date 2", "date 3")
    }
    val spinProductionDateSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val generalShelfLife: MutableLiveData<String> = MutableLiveData()
    val remainingShelfLife: MutableLiveData<String> = MutableLiveData()
    val tvProductionDate by lazy {
        "${taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo.value!!)?.volume.toStringFormatted()} " +
                "${taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.findMercuryInfoOfProduct(productInfo.value!!)?.uom?.name}"
    }
    val isDiscrepancy: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }
    val visibleShelfLife by lazy {
        productInfo.value?.generalShelfLife?.toDouble()!! > 0.0 && productInfo.value?.remainingShelfLife?.toDouble()!! > 0.0
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount by lazy  {
        countValue.combineLatest(spinQualitySelectedPosition).map{
            if (qualityInfo.value?.get(it!!.second)?.code == "1") {
                (it?.first ?: 0.0) + taskManager.getReceivingTask()!!.taskRepository.getMercuryDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
            } else {
                0.0
            }
        }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        if (it != 0.0) {
            "+ " + it.toStringFormatted() + " " + productInfo.value?.uom?.name
        } else {
            "0 " + productInfo.value?.uom?.name
        }
    }

    val refusalTotalCount by lazy  {
        countValue.
                combineLatest(spinReasonRejectionSelectedPosition).
                combineLatest(spinQualitySelectedPosition).
                map{
                    if (qualityInfo.value?.get(it?.second ?: 0)?.code != "1") {
                        (it?.first?.first ?: 0.0) + taskManager.
                                getReceivingTask()!!.
                                taskRepository.
                                getMercuryDiscrepancies().
                                getCountRefusalOfProductOfReasonRejection(productInfo.value!!, reasonRejectionInfo.value?.get(it?.first?.second ?: 0) ?.code)
                    } else {
                        0.0
                    }
                }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        if (it != 0.0) {
            "- " + it.toStringFormatted() + " " + productInfo.value?.uom?.name
        } else {
            "0 " + productInfo.value?.uom?.name
        }
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
                it!! != 0.0
    }

    init {
        viewModelScope.launch {
            count.value = taskManager.getReceivingTask()?.taskRepository?.getMercuryDiscrepancies()?.getCountProductNotProcessedOfProduct(productInfo.value!!).toStringFormatted()
            suffix.value = productInfo.value?.uom?.name
            generalShelfLife.value = productInfo.value?.generalShelfLife
            remainingShelfLife.value = productInfo.value?.remainingShelfLife
            if (isDiscrepancy.value!!) {
                qualityInfo.value = dataBase.getQualityInfoForDiscrepancy()
                spinQualitySelectedPosition.value = 2
            } else {
                qualityInfo.value = dataBase.getQualityInfo()
            }
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

    fun onClickDetails(){
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickAdd() : Boolean {
        val reasonRejectionCode = if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
            "1"
        } else {
            reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code
        }
        when (processMercuryProductService.checkConditionsOfPreservation(count.value ?: "0", reasonRejectionCode)) {
            PROCESSING_MERCURY_SAVED -> {
                if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
                    processMercuryProductService.add(acceptTotalCount.value!!.toString(), "1", spinManufacturers[spinManufacturersSelectedPosition.value!!], spinProductionDate[spinProductionDateSelectedPosition.value!!])
                } else {
                    processMercuryProductService.add(refusalTotalCount.value!!.toString(), reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code, spinManufacturers[spinManufacturersSelectedPosition.value!!], spinProductionDate[spinProductionDateSelectedPosition.value!!])
                }
                return true
            }
            PROCESSING_MERCURY_QUANT_GREAT_IN_INVOICE -> {
                screenNavigator.openAlertQuantGreatInInvoiceScreen()
                return false
            }
            PROCESSING_MERCURY_QUANT_GREAT_IN_ORDER -> {
                screenNavigator.openAlertQuantGreatInOrderScreen()
                return false
            }
            PROCESSING_MERCURY_QUANT_LESS_THAN_IN_VAD -> {
                var result = false
                screenNavigator.openAlertQuantLessThanInVadScreen(
                        noCallbackFunc = {
                            result = false
                        },
                        yesCallbackFunc = {
                            processMercuryProductService.add(count.value ?: "0", "41", spinManufacturers[spinManufacturersSelectedPosition.value!!], spinProductionDate[spinProductionDateSelectedPosition.value!!])
                            result = true
                        }
                )
                return result
            }
            PROCESSING_MERCURY_QUANT_MORE_THAN_IN_VAD -> {
                screenNavigator.openAlertQuantMoreThanInVadScreen()
                return false
            }
        }
        return false
    }

    fun onClickApply() {
        if (onClickAdd()) processMercuryProductService.save()
    }

    fun onScanResult(data: String) {
        searchProductDelegate.searchCode(code = data, fromScan = true)
    }
}
