package com.lenta.bp9.features.transport_marriage.goods_info

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.transport_marriage.ActItem
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskTransportMarriageInfo
import com.lenta.bp9.model.task.TaskTransportMarriageInfoRestData
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.requests.network.ZmpUtzGrz26V001NetRequest
import com.lenta.bp9.requests.network.ZmpUtzGrz26V001Params
import com.lenta.bp9.requests.network.ZmpUtzGrz26V001Result
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

class TransportMarriageGoodsInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var dataBase: IDataBaseRepo
    @Inject
    lateinit var zmpUtzGrz26V001NetRequest: ZmpUtzGrz26V001NetRequest
    @Inject
    lateinit var hyperHive: HyperHive

    val transportMarriageInfo: MutableLiveData<TaskTransportMarriageInfo> = MutableLiveData()
    val cargoUnitNumber by lazy {
        transportMarriageInfo.value?.cargoUnitNumber
    }
    private val processingUnitSelected by lazy {
        transportMarriageInfo.value?.processingUnitNumber
    }
    private val transportMarriageProduct:MutableLiveData<List<TaskTransportMarriageInfo>> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinProcessingUnit: MutableLiveData<List<String>> = MutableLiveData()
    val spinProcessingUnitSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    private val processingUnitProduct: MutableLiveData<List<String>> = MutableLiveData()
    val suffix: MutableLiveData<String> = MutableLiveData()
    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    val acceptTotalCountWithUom by lazy {
        "0 ${transportMarriageInfo.value?.uom?.name}"
    }
    val refusalTotalCountWithUom: MutableLiveData<String> = countValue.map {
        if (it != 0.0) {
            "- " + it.toStringFormatted() + " " + transportMarriageInfo.value?.uom?.name
        } else {
            "0 " + transportMarriageInfo.value?.uom?.name
        }
    }
    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
        it!! != 0.0
    }

    fun getTitle() : String {
        return "${transportMarriageInfo.value!!.getMaterialLastSix()} ${transportMarriageInfo.value!!.materialName}"
    }

    init {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            suffix.value = transportMarriageInfo.value?.uom?.name
            spinQuality.value = dataBase.getQualityInfoTransportMarriage()?.map {
                it.name
            }

            searchProduct(transportMarriageInfo.value?.materialNumber ?: "")
        }
    }

    private fun searchProduct(materialNumber: String) {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            taskManager.getReceivingTask()?.let { task ->
                val params = ZmpUtzGrz26V001Params(
                        taskNumber = task.taskHeader.taskNumber,
                        cargoUnitNumber = cargoUnitNumber ?: "",
                        materialNumber = materialNumber
                )
                zmpUtzGrz26V001NetRequest(params).either(::handleFailure, ::handleSuccess)
            }
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(result: ZmpUtzGrz26V001Result) {
        viewModelScope.launch {
            processingUnitProduct.value = result.processingUnits.map {
                it.processingUnitNumber
            }

            spinProcessingUnit.postValue(
                    processingUnitProduct.value?.map {
                        "ЕО-$it"
                    }
            )
            if (processingUnitSelected != null) {
                processingUnitProduct.value?.mapIndexed { index, s ->
                    if (s == processingUnitSelected) {
                        spinProcessingUnitSelectedPosition.value = index
                        return@mapIndexed
                    }
                }
            }
            transportMarriageProduct.value = result.processingUnits.map {
                val batchNumber = result.taskBatches.findLast {batchesInfo ->
                    batchesInfo.materialNumber == it.materialNumber && batchesInfo.processingUnitNumber == it.processingUnitNumber
                }?.batchNumber
                TaskTransportMarriageInfo.from(hyperHive, it, cargoUnitNumber ?: "", batchNumber ?: "")
            }
        }
    }

    fun onClickDetails() {
        //screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickAdd() {
        //processGeneralProductService.add(count.value!!, reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code)
    }

    fun onClickApply() {

    }

    override fun onClickPosition(position: Int) {
        spinProcessingUnitSelectedPosition.value = position
    }

    fun onScanResult(data: String) {
        searchProduct(materialNumber = data)
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

}
