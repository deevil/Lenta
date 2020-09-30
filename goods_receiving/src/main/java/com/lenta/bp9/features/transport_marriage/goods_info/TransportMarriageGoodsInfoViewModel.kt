package com.lenta.bp9.features.transport_marriage.goods_info

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskTransportMarriageInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.ZmpUtzGrz26V001NetRequest
import com.lenta.bp9.requests.network.ZmpUtzGrz26V001Params
import com.lenta.bp9.requests.network.ZmpUtzGrz26V001Result
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequest
import com.lenta.shared.requests.combined.scan_info.ScanInfoRequestParams
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import com.mobrun.plugin.api.HyperHive
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
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var scanInfoRequest: ScanInfoRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo

    val transportMarriageInfoCurrent: MutableLiveData<TaskTransportMarriageInfo> = MutableLiveData()
    val cargoUnitNumber by lazy {
        transportMarriageInfoCurrent.value?.cargoUnitNumber
    }
    private val processingUnitSelected by lazy {
        transportMarriageInfoCurrent.value?.processingUnitNumber
    }
    private val transportMarriageOfProduct: MutableLiveData<List<TaskTransportMarriageInfo>> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinProcessingUnit: MutableLiveData<List<String>> = MutableLiveData()
    val spinProcessingUnitSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    private val isScan: MutableLiveData<Boolean> = MutableLiveData(false)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map {
        val countAdd = (it?.toDoubleOrNull() ?: 0.0) + (transportMarriageInfoCurrent.value?.quantity ?: 0.0)
        if (countAdd < 0.0) {
            0.0
        } else {
            countAdd
        }
    }
    val acceptTotalCountWithUom by lazy {
        "0 ${transportMarriageInfoCurrent.value?.uom?.name}"
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = countValue.map {
        if (it != 0.0) {
            "- " + it.toStringFormatted() + " " + transportMarriageInfoCurrent.value?.uom?.name
        } else {
            "0 " + transportMarriageInfoCurrent.value?.uom?.name
        }
    }
    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
        it!! > 0.0 && it != (transportMarriageInfoCurrent.value?.quantity ?: 0.0)
    }

    fun getTitle() : String {
        return "${transportMarriageInfoCurrent.value?.getMaterialLastSix()} ${transportMarriageInfoCurrent.value?.materialName}"
    }

    init {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            suffix.value = transportMarriageInfoCurrent.value?.uom?.name
            spinQuality.value = dataBase.getQualityInfoTransportMarriage()?.map {
                it.name
            }

            searchProduct(transportMarriageInfoCurrent.value?.materialNumber ?: "")
        }
    }

    private fun searchProduct(materialNumber: String) {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            val foundTransportMarriageInfo = taskManager.getReceivingTask()?.taskRepository?.getTransportMarriage()?.findTransportMarriage(cargoUnitNumber ?: "", materialNumber)
            if (!foundTransportMarriageInfo.isNullOrEmpty()) {
                transportMarriageOfProduct.value = foundTransportMarriageInfo
                spinProcessingUnit.postValue(
                        transportMarriageOfProduct.value?.map {
                            "ЕО-${it.processingUnitNumber}"
                        }
                )
                if (processingUnitSelected != null) {
                    transportMarriageOfProduct.value?.mapIndexed { index, taskTransportMarriageInfo ->
                        if (taskTransportMarriageInfo.processingUnitNumber == processingUnitSelected) {
                            spinProcessingUnitSelectedPosition.value = index
                            return@mapIndexed
                        }
                    }
                }
                transportMarriageOfProduct.value?.get(spinProcessingUnitSelectedPosition.value!!)?.let {
                    transportMarriageInfoCurrent.value = it
                }

                if (isScan.value == true) {
                    transportMarriageInfoCurrent.value?.let {
                        screenNavigator.goBack()
                        screenNavigator.openTransportMarriageGoodsInfoScreen(transportMarriageInfo = it)
                    }
                }
            } else {
                taskManager.getReceivingTask()?.let { task ->
                    val params = ZmpUtzGrz26V001Params(
                            taskNumber = task.taskHeader.taskNumber,
                            cargoUnitNumber = cargoUnitNumber ?: "",
                            materialNumber = materialNumber
                    )
                    zmpUtzGrz26V001NetRequest(params).either(::handleFailure, ::handleSuccess)
                }
            }
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(result: ZmpUtzGrz26V001Result) {
        launchUITryCatch {
            repoInMemoryHolder.manufacturers.value = result.manufacturers
            transportMarriageOfProduct.value = result.processingUnits.map {
                val batchNumber = result.taskBatches.findLast {batchesInfo ->
                    batchesInfo.materialNumber == it.materialNumber && batchesInfo.processingUnitNumber == it.processingUnitNumber
                }?.batchNumber
                TaskTransportMarriageInfo.from(hyperHive, it, cargoUnitNumber ?: "", batchNumber ?: "")
            }
            spinProcessingUnit.postValue(
                    transportMarriageOfProduct.value?.map {
                        "ЕО-${it.processingUnitNumber}"
                    }
            )
            if (processingUnitSelected != null) {
                transportMarriageOfProduct.value?.mapIndexed { index, taskTransportMarriageInfo ->
                    if (taskTransportMarriageInfo.processingUnitNumber == processingUnitSelected) {
                        spinProcessingUnitSelectedPosition.value = index
                        return@mapIndexed
                    }
                }
            }
            transportMarriageOfProduct.value?.get(spinProcessingUnitSelectedPosition.value!!)?.let {
                transportMarriageInfoCurrent.value = it
            }

            if (isScan.value == true) {
                transportMarriageInfoCurrent.value?.let {
                    screenNavigator.goBack()
                    screenNavigator.openTransportMarriageGoodsInfoScreen(transportMarriageInfo = it)
                }
            }
        }
    }

    fun onClickDetails() {
        transportMarriageOfProduct.value?.get(spinProcessingUnitSelectedPosition.value!!)?.let {
            screenNavigator.openTransportMarriageGoodsDetailsScreen(
                    cargoUnitNumber = it.cargoUnitNumber,
                    materialNumber = it.materialNumber,
                    materialName = it.materialName
            )
        }
    }

    fun onClickAdd() {
        transportMarriageInfoCurrent.value?.let {
            if ((countValue.value ?: 0.0) > it.quantityInvestments) {
                screenNavigator.openAlertAmountEnteredGreaterPUScreen()
            } else {
                taskManager.getReceivingTask()?.
                        taskRepository?.
                        getTransportMarriage()?.
                        changeTransportMarriage(it.copy(quantity = countValue.value!!))
                transportMarriageInfoCurrent.value = it.copy(quantity = countValue.value!!)
                searchProduct(transportMarriageInfoCurrent.value?.materialNumber ?: "") //чтобы обновились локальные данные для transportMarriageOfProduct
            }
        }
        count.value = "0"
    }

    fun onClickApply() {
        onClickAdd()
        screenNavigator.goBack()
    }

    override fun onClickPosition(position: Int) {
        spinProcessingUnitSelectedPosition.value = position
        transportMarriageOfProduct.value?.get(spinProcessingUnitSelectedPosition.value!!)?.let {
            transportMarriageInfoCurrent.value = it
        }
    }

    fun onScanResult(data: String) {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)

            scanInfoRequest(
                    ScanInfoRequestParams(
                            number = data,
                            tkNumber = sessionInfo.market.orEmpty(),
                            fromScan = true,
                            isBarCode = true
                    )
            ).also {
                screenNavigator.hideProgress()
            }.either(::handleFailure) { scanInfoResult ->
                isScan.value = true
                searchProduct(scanInfoResult.productInfo.materialNumber)
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

}
