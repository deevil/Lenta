package com.lenta.bp9.features.goods_information.general

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessGeneralProductService
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

class GoodsInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processGeneralProductService: ProcessGeneralProductService

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val titleProgressScreen: MutableLiveData<String> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    private val roundingQuantity: MutableLiveData<Double> = MutableLiveData()
    private val countWithoutParamGrsGrundNeg: MutableLiveData<Double> = MutableLiveData()
    private val countOverdelivery: MutableLiveData<Double> = MutableLiveData()
    private val isClickApply: MutableLiveData<Boolean> = MutableLiveData(false)
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> by lazy {
        countValue.combineLatest(spinQualitySelectedPosition).map {
            if (qualityInfo.value?.get(it!!.second)?.code == "1") {
                (it?.first
                        ?: 0.0) + taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
            } else {
                0.0
            }
        }
    }

    val refusalTotalCount: MutableLiveData<Double> by lazy {
        countValue.combineLatest(spinReasonRejectionSelectedPosition).combineLatest(spinQualitySelectedPosition).map {
            if (qualityInfo.value?.get(it?.second ?: 0)?.code != "1") {
                (it?.first?.first
                        ?: 0.0) + taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductOfReasonRejection(productInfo.value!!, reasonRejectionInfo.value?.get(it?.first?.second
                        ?: 0)?.code)
            } else {
                0.0
            }
        }
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
        it!! != 0.0
    }

    init {
        viewModelScope.launch {
            suffix.value = productInfo.value?.uom?.name
            qualityInfo.value = dataBase.getQualityInfo()
            spinQuality.value = qualityInfo.value?.map {
                it.name
            }
            if (processGeneralProductService.newProcessGeneralProductService(productInfo.value!!) == null) {
                screenNavigator.goBack()
                screenNavigator.openAlertWrongProductType()
            }
        }
    }

    fun onClickDetails() {
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onClickApply() {
        isClickApply.value = true
        onClickAdd()
    }

    fun onScanResult(data: String) {
        searchProductDelegate.searchCode(code = data, fromScan = true)
    }

    override fun onClickPosition(position: Int) {
        spinReasonRejectionSelectedPosition.value = position
    }

    fun onClickPositionSpinQuality(position: Int) {
        viewModelScope.launch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(qualityInfo.value!![position].code)
        }
    }

    private suspend fun updateDataSpinReasonRejection(selectedQuality: String) {
        viewModelScope.launch {
            screenNavigator.showProgress(titleProgressScreen.value!!)
            spinReasonRejectionSelectedPosition.value = 0
            reasonRejectionInfo.value = dataBase.getReasonRejectionInfoOfQuality(selectedQuality)
            spinReasonRejection.value = reasonRejectionInfo.value?.map {
                it.name
            }
            count.value = count.value
            screenNavigator.hideProgress()
        }
    }

    //блок 6.16
    fun onClickAdd() {
        if (processGeneralProductService.countEqualOrigQuantity(countValue.value!!)) {//блок 6.16 (да)
            //блок 6.172
            saveCategory()
        } else {//блок 6.16 (нет)
            //блок 6.22
            if (processGeneralProductService.countLargerOrigQuantity(countValue.value!!)) {//блок 6.22 (да)
                //блок 6.58
                paramGrsGrundNeg()
            } else {//блок 6.22 (нет)
                //блок 6.26
                if (productInfo.value!!.uom.name == "г") {//блок 6.26 (да)
                    //блок 6.49
                    roundingQuantity.value = processGeneralProductService.getRoundingQuantity(productInfo.value!!.origQuantity.toDouble())
                    //блок 6.90
                    if (roundingQuantity.value!! <= productInfo.value!!.roundingShortages.toDouble()) {//блок 6.90 (да)
                        //блок 6.109
                        screenNavigator.openRoundingIssueDialog(
                                //блок 6.148
                                noCallbackFunc = {
                                    //блок 6.172
                                    saveCategory()
                                },
                                //блок 6.149
                                yesCallbackFunc = {
                                    //блок 6.154
                                    count.value = (countValue.value!! + roundingQuantity.value!!).toString()
                                    //блок 6.172
                                    saveCategory()
                                }
                        )
                    } else {//блок 6.90 (нет)
                        //блок 6.172
                        saveCategory()
                    }
                } else {//блок 6.26 (нет)
                    //блок 6.172
                    saveCategory()
                }
            }
        }
    }

    //блок 6.58
    private fun paramGrsGrundNeg() {
        viewModelScope.launch {
            val paramGrsGrundNeg = dataBase.getParamGrsGrundNeg()
            if (processGeneralProductService.paramGrsGrundNeg(paramGrsGrundNeg!!)) {//блок 6.58 (да)
                //блок 6.93
                countWithoutParamGrsGrundNeg.value = processGeneralProductService.countWithoutParamGrsGrundNeg(paramGrsGrundNeg)
                //блок 6.130
                if (countWithoutParamGrsGrundNeg.value == 0.0) {//блок 6.130 (да)
                    //блок 6.121
                    processGeneralProductService.delCategoryParamGrsGrundNeg(paramGrsGrundNeg)
                    saveCategory()
                } else {//блок 6.130 (нет)
                    //блок 6.147
                    if (countWithoutParamGrsGrundNeg.value!! > 0.0) {//блок 6.147 (да)
                        //блок 6.145
                        processGeneralProductService.addWithoutUnderload(paramGrsGrundNeg, countWithoutParamGrsGrundNeg.value.toString())
                    } else {//блок 6.147 (нет)
                        //блок 6.155
                        processGeneralProductService.delCategoryParamGrsGrundNeg(paramGrsGrundNeg)
                        noParamGrsGrundNeg()
                    }
                }
            } else {//блок 6.58 (нет)
                noParamGrsGrundNeg()
            }
        }
    }

    //блок 6.163
    private fun noParamGrsGrundNeg() {
        if (productInfo.value!!.uom.name == "г") {//блок 6.163 (да)
            //блок 6.167
            roundingQuantity.value = processGeneralProductService.getRoundingQuantity(productInfo.value!!.origQuantity.toDouble())
            //блок 6.173
            if (roundingQuantity.value!! <= productInfo.value!!.roundingSurplus.toDouble()) {//блок 6.173 (да)
                //блок 6.175
                screenNavigator.openRoundingIssueDialog(
                        //блок 6.178
                        noCallbackFunc = {
                            //блок 6.187
                            calculationOverdelivery()
                        },
                        //блок 6.179
                        yesCallbackFunc = {
                            //блок 6.185
                            count.value = (countValue.value!! + roundingQuantity.value!!).toString()
                            //блок 6.172
                            saveCategory()
                        }
                )
            } else {//блок 6.173 (нет)
                //блок 6.187
                calculationOverdelivery()
            }
        } else {//блок 6.163 (нет)
            //блок 6.187
            calculationOverdelivery()
        }
    }

    //блок 6.172
    private fun saveCategory() {
        if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
            processGeneralProductService.add(acceptTotalCount.value!!.toString(), "1")
        } else {
            processGeneralProductService.add(refusalTotalCount.value!!.toString(), reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code)
        }

        //блок 6.176
        clickBtnApply()
    }

    //блок 6.176
    private fun clickBtnApply() {
        if (isClickApply.value!!) {//блок 6.176 (да)
            //блок 6.181
            screenNavigator.goBack()
        } else {//блок 6.176 (нет)
            //блок 6.180
            count.value = "0"
        }
    }

    //блок 6.187
    private fun calculationOverdelivery() {
        //блок 6.187
        countOverdelivery.value = productInfo.value!!.orderQuantity.toDouble() - processGeneralProductService.getQuantityCapitalized() + (productInfo.value!!.overdToleranceLimit.toDouble() / 100) * productInfo.value!!.orderQuantity.toDouble()
        //блок 6.190
        if (processGeneralProductService.getQuantityAllCategory() > countOverdelivery.value!!) {//блок 6.190 (да)
            //блок 6.193
            screenNavigator.openAlertCountLargerOverdelivery()
        } else {//блок 6.190 (нет)
            if (productInfo.value!!.origQuantity.toDouble() > productInfo.value!!.orderQuantity.toDouble()) {
                val calculationOne = productInfo.value!!.origQuantity.toDouble() - productInfo.value!!.orderQuantity.toDouble()
                val calculationTwo = productInfo.value!!.origQuantity.toDouble() - processGeneralProductService.getQuantityAllCategory() - countValue.value!!
                val calculation = if (calculationOne < calculationTwo) calculationOne else calculationTwo
                if (calculation > 0.0) {
                    processGeneralProductService.add(calculation.toString(), "41")
                }
            }
            //блок 6.196
            if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
                saveCategory()
            } else {
                if (processGeneralProductService.categNormNotOrderLargerOrigQuantity()) {
                    screenNavigator.openAlertCountLargerOverdelivery()
                } else {
                    saveCategory()
                }
            }

        }
    }

}
