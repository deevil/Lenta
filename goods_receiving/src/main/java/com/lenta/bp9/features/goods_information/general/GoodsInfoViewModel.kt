package com.lenta.bp9.features.goods_information.general

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessGeneralProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.platform.toolbar.bottom_toolbar.ButtonDecorationInfo.Companion.add
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.Logg
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
    @Inject
    lateinit var context: Context
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
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinShelfLife: MutableLiveData<List<String>> = MutableLiveData()
    val spinShelfLifeSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    private val roundingQuantity: MutableLiveData<Double> = MutableLiveData()
    private val countWithoutParamGrsGrundNeg: MutableLiveData<Double> = MutableLiveData()
    private val countOverdelivery: MutableLiveData<Double> = MutableLiveData()
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

    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
    val enteredProcessingUnitNumber: MutableLiveData<String> = MutableLiveData()
    val generalShelfLife: MutableLiveData<String> = MutableLiveData()
    val remainingShelfLife: MutableLiveData<String> = MutableLiveData()
    val shelfLifeDate: MutableLiveData<String> = MutableLiveData("")
    val isPerishable: MutableLiveData<Boolean> = MutableLiveData(false)
    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("dd.MM.yyyy")
    private val expirationDate: MutableLiveData<Calendar> = MutableLiveData()
    private val currentDate: MutableLiveData<Date> = MutableLiveData()

    val isGoodsAddedAsSurplus: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.isGoodsAddedAsSurplus == true )
    }
    val isTaskPGE: MutableLiveData<Boolean> by lazy {
        if (taskManager.getReceivingTask()!!.taskHeader.taskType == TaskType.RecalculationCargoUnit) MutableLiveData(true) else MutableLiveData(false)
    }
    val isEizUnit: MutableLiveData<Boolean> by lazy {
        MutableLiveData(isDiscrepancy.value == false && isGoodsAddedAsSurplus.value == false)
    }
    val tvAccept: MutableLiveData<String> by lazy {
        if (isTaskPGE.value == true && isGoodsAddedAsSurplus.value == true) {
            MutableLiveData(context.getString(R.string.accept_txt))
        } else {
            MutableLiveData(context.getString(R.string.accept, "${productInfo.value?.purchaseOrderUnits?.name}=${productInfo.value?.quantityInvest?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}"))
        }

    }

    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }

    val acceptTotalCount: MutableLiveData<Double> by lazy {
        countValue.combineLatest(spinQualitySelectedPosition).map {
            val countAccept = if (isTaskPGE.value!!) {
                taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)
            } else {
                taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
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
                taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(productInfo.value!!)
            } else {
                taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo.value!!)
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

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.combineLatest(enteredProcessingUnitNumber).map {
        if (isTaskPGE.value!! && isGoodsAddedAsSurplus.value!!) {
            it?.first != 0.0 && it?.second?.length == 18
        } else {
            it?.first != 0.0
        }
    }

    init {
        viewModelScope.launch {
            if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
                when {
                    isGoodsAddedAsSurplus.value == true -> { //товар, который не числится в задании https://trello.com/c/im9rJqrU
                        enteredProcessingUnitNumber.value = productInfo.value?.processingUnit ?: ""
                        suffix.value = uom.value?.name
                        qualityInfo.value = dataBase.getSurplusInfoForPGE()
                    }
                    isDiscrepancy.value!! -> {
                        suffix.value = uom.value?.name
                        count.value = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountProductNotProcessedOfProductPGE(productInfo.value!!).toStringFormatted()
                        qualityInfo.value = dataBase.getQualityInfoPGEForDiscrepancy()
                    }
                    else -> {// обычный товар https://trello.com/c/OMjrZPhg
                        suffix.value = productInfo.value?.purchaseOrderUnits?.name
                        qualityInfo.value = dataBase.getQualityInfoPGE()
                    }
                }
            } else {
                suffix.value = uom.value?.name
                if (isDiscrepancy.value!!) {
                    count.value = taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountProductNotProcessedOfProduct(productInfo.value!!).toStringFormatted()
                    qualityInfo.value = dataBase.getQualityInfoForDiscrepancy()
                    spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "4"}
                } else {
                    qualityInfo.value = dataBase.getQualityInfo()
                }
            }

            spinQuality.value = qualityInfo.value?.map {
                it.name
            }

            /** определяем, что товар скоропорт https://trello.com/c/8sOTWtB7 */
            val paramGrzUffMhdhb = dataBase.getParamGrzUffMhdhb()?.toInt() ?: 60
            isPerishable.value = (productInfo.value?.generalShelfLife?.toInt() ?: 0) > 0 ||
                    (productInfo.value?.remainingShelfLife?.toInt() ?: 0) > 0 ||
                    ((productInfo.value?.mhdhbDays ?: 0) > 0 && (productInfo.value?.mhdhbDays ?: 0) < paramGrzUffMhdhb )
            if (isPerishable.value == true) {
                spinShelfLife.value = dataBase.getTermControlInfo()
                currentDate.value = timeMonitor.getServerDate()
                expirationDate.value = Calendar.getInstance()
                generalShelfLife.value = productInfo.value?.generalShelfLife
                remainingShelfLife.value = productInfo.value?.remainingShelfLife
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

    fun onClickPositionSpinShelfLife(position: Int){
        spinShelfLifeSelectedPosition.value = position
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
                    spinReasonRejectionSelectedPosition.value = reasonRejectionInfo.value!!.indexOfLast {it.code == "43"}
                } else {
                    spinReasonRejectionSelectedPosition.value = 0
                }
                count.value = count.value
                screenNavigator.hideProgress()
            }
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

    fun onClickApply() {
        isClickApply.value = true
        onClickAdd()
    }

    fun onClickAdd() {
        //ПГЕ
        if (isTaskPGE.value == true) {
            var addNewCount = count.value!!.toDouble()
            if (isEizUnit.value!!) {
                addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: 1.0
            }
            if (isGoodsAddedAsSurplus.value == true) { //GRZ. ПГЕ. Добавление товара, который не числится в задании https://trello.com/c/im9rJqrU
                processGeneralProductService.setProcessingUnitNumber(enteredProcessingUnitNumber.value!!)
                processGeneralProductService.add(addNewCount.toString(), qualityInfo.value!![spinQualitySelectedPosition.value!!].code)
                clickBtnApply()
            } else { //Пересчёт обычного товара (шт.) https://trello.com/c/OMjrZPhg
                if (processGeneralProductService.countLessEqualOrderQuantityPGE(addNewCount)) {
                    processGeneralProductService.add(addNewCount.toString(), qualityInfo.value!![spinQualitySelectedPosition.value!!].code)
                    clickBtnApply()
                } else {
                    screenNavigator.openAlertQuantGreatInInvoiceScreen()
                }
            }
            return
        }

        //ППП-скоропорт. в блок-схеме лист 6 "Карточка товара ППП" блок - 6.3
        if (isPerishable.value == true) {
            addPerishable()
            return
        }

        //ППП-обычный товар. в блок-схеме лист 6 "Карточка товара ППП" блок - 6.8
        addOrdinaryGoods()
    }

    private fun addOrdinaryGoods() {
        //блок 6.16
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

    //ППП-скоропорт. в блок-схеме лист 6 "Карточка товара ППП" блок - 6.3
    private fun addPerishable() {
        //блок 6.101
        if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code != "1") {
            addOrdinaryGoods()
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
                        spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "7"}
                    }
            )
            return
        }

        //блоки 6.157 и 6.182
        if ( Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days > productInfo.value!!.remainingShelfLife.toLong() ) {
            //блок 6.192
            addOrdinaryGoods()
            return
        }

        //блок 6.184
        screenNavigator.openExpiredDialog(
                //блок 6.189
                noCallbackFunc = {
                    //блок 6.191
                    spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "7"}
                },
                //блок 6.188
                yesCallbackFunc = {
                    //блок 6.192
                    addOrdinaryGoods()
                }
        )
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
                        //блок 6.176
                        clickBtnApply()
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
            processGeneralProductService.add(count.value!!, reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code)
        }

        //блок 6.176
        clickBtnApply()
    }

    //ППП блок 6.176
    private fun clickBtnApply() {
        if (isClickApply.value!!) {
            screenNavigator.goBack()
        } else {
            count.value = "0"
        }
    }

    //блок 6.187
    private fun calculationOverdelivery() {
        //блок 6.187
        countOverdelivery.value = productInfo.value!!.orderQuantity.toDouble() /**- processGeneralProductService.getQuantityCapitalized()*/ + (productInfo.value!!.overdToleranceLimit.toDouble() / 100) * productInfo.value!!.orderQuantity.toDouble()
        //блок 6.190
        if (processGeneralProductService.getQuantityAllCategory(countValue.value!!) > countOverdelivery.value!!) {//блок 6.190 (да)
            //блок 6.193
            screenNavigator.openAlertCountLargerOverdelivery()
        } else {//блок 6.190 (нет)
            if (productInfo.value!!.origQuantity.toDouble() > productInfo.value!!.orderQuantity.toDouble()) {
                val calculationOne = productInfo.value!!.origQuantity.toDouble() - productInfo.value!!.orderQuantity.toDouble()
                val calculationTwo = productInfo.value!!.origQuantity.toDouble() - processGeneralProductService.getQuantityAllCategory(countValue.value!!)
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
