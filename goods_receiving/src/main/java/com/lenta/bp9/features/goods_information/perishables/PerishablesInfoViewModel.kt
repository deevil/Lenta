package com.lenta.bp9.features.goods_information.perishables

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessGeneralProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.joda.time.Days
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class PerishablesInfoViewModel : CoreViewModel(), OnPositionClickListener {

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

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinShelfLife: MutableLiveData<List<String>> = MutableLiveData()
    val spinShelfLifeSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val remainingShelfLife: MutableLiveData<String> = MutableLiveData()
    val generalShelfLife: MutableLiveData<String> = MutableLiveData()
    val shelfLifeDate: MutableLiveData<String> = MutableLiveData("")
    val isDefect: MutableLiveData<Boolean> = spinQualitySelectedPosition.map {
        it != 0
    }

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
    private val isClickApply: MutableLiveData<Boolean> = MutableLiveData(false)
    private val roundingQuantity: MutableLiveData<Double> = MutableLiveData()
    private val countWithoutParamGrsGrundNeg: MutableLiveData<Double> = MutableLiveData()
    private val countOverdelivery: MutableLiveData<Double> = MutableLiveData()

    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("dd.MM.yyyy")
    private val expirationDate: MutableLiveData<Calendar> = MutableLiveData()
    private val currentDate: MutableLiveData<Date> = MutableLiveData()

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> by lazy  {
        countValue.combineLatest(spinQualitySelectedPosition).map{
            if (qualityInfo.value?.get(it!!.second)?.code == "1") {
                (it?.first ?: 0.0) + taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
            } else {
                0.0
            }
        }
    }

    val refusalTotalCount: MutableLiveData<Double> by lazy  {
        countValue.
                combineLatest(spinReasonRejectionSelectedPosition).
                combineLatest(spinQualitySelectedPosition).
                map{
                    if (qualityInfo.value?.get(it?.second ?: 0)?.code != "1") {
                        (it?.first?.first ?: 0.0) + taskManager.
                                getReceivingTask()!!.
                                taskRepository.
                                getProductsDiscrepancies().
                                getCountRefusalOfProductOfReasonRejection(productInfo.value!!, reasonRejectionInfo.value?.get(it?.first?.second ?: 0) ?.code)
                    } else {
                        0.0
                    }
                }
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.
            map {
                it!! != 0.0
            }

    init {
        viewModelScope.launch {
            currentDate.value = timeMonitor.getServerDate()
            expirationDate.value = Calendar.getInstance()
            suffix.value = productInfo.value?.uom?.name
            generalShelfLife.value = productInfo.value?.generalShelfLife
            remainingShelfLife.value = productInfo.value?.remainingShelfLife
            qualityInfo.value = dataBase.getQualityInfo()
            spinQuality.value = qualityInfo.value?.map {
                it.name
            }
            spinShelfLife.value = dataBase.getTermControlInfo()
            if (processGeneralProductService.newProcessGeneralProductService(productInfo.value!!) == null){
                screenNavigator.goBack()
                screenNavigator.openAlertWrongProductType()
            }
        }
    }

    fun onClickDetails(){
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    //блок 6.3
    fun onClickAdd() {
        //блок 6.101
        if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
            add()
            return
        }

        if (!isCorrectDate(shelfLifeDate.value)) {
            screenNavigator.openAlertNotCorrectDate()
            return
        }

        //блок 6.131
        if (spinShelfLifeSelectedPosition.value == 1) {
            //блок 6.146
            expirationDate.value!!.time = formatter.parse(shelfLifeDate.value)
            return
        } else {
            //блок 6.144
            expirationDate.value!!.time = formatter.parse(shelfLifeDate.value)
            expirationDate.value!!.add(Calendar.DATE, productInfo.value!!.generalShelfLife.toInt())
        }

        //блок 6.152
        if (expirationDate.value!!.time <= currentDate.value) {
            //блок 6.158
            screenNavigator.openExpiredDialog(
                    //блок 6.169
                    noCallbackFunc = {
                        //блок 6.172
                    },
                    //блок 6.170
                    yesCallbackFunc = {
                        //блок 6.174
                        spinQualitySelectedPosition.value = 6
                    }
            )
            return
        }

        //блоки 6.157 и 6.182
        if ( Days.daysBetween(DateTime(currentDate.value),DateTime(expirationDate.value!!.time)).days > productInfo.value!!.remainingShelfLife.toLong() ) {
            //блок 6.192
            add()
            return
        }

        //блок 6.184
        screenNavigator.openExpiredDialog(
                //блок 6.189
                noCallbackFunc = {
                    //блок 6.191
                    spinQualitySelectedPosition.value = 6
                },
                //блок 6.188
                yesCallbackFunc = {
                    //блок 6.192
                    add()
                }
        )

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

    fun onClickPositionSpinQuality(position: Int){
        viewModelScope.launch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(qualityInfo.value!![position].code)
        }
    }

    fun onClickPositionSpinShelfLife(position: Int){
        spinShelfLifeSelectedPosition.value = position
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

    private fun isCorrectDate(checkDate: String?): Boolean {
        return try {
            val date = formatter.parse(checkDate)
            !(checkDate != formatter.format(date) || date!! > currentDate.value)
        } catch (e: Exception) {
            false
        }
    }

    //блок 6.16
    private fun add() {
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
