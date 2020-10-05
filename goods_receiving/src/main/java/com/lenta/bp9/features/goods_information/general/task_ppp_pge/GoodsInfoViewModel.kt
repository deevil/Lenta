package com.lenta.bp9.features.goods_information.general.task_ppp_pge

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.model.processing.ProcessGeneralProductService
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
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
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
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
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    private val processingUnitsOfProduct: MutableLiveData<List<TaskProductInfo>> = MutableLiveData()
    val uom: MutableLiveData<Uom?> by lazy {
        if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.DirectSupplier) {
            MutableLiveData(productInfo.value?.purchaseOrderUnits)
        } else {
            MutableLiveData(productInfo.value?.uom)
        }
    }
    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
    private val shelfLifeInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinQualitySelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val spinShelfLife: MutableLiveData<List<String>> = MutableLiveData()
    val spinShelfLifeSelectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    val suffix: MutableLiveData<String> = MutableLiveData()
    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)
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

    val enteredProcessingUnitNumber: MutableLiveData<String> = MutableLiveData("")
    val generalShelfLife: MutableLiveData<String> = MutableLiveData()
    val remainingShelfLife: MutableLiveData<String> = MutableLiveData()
    val shelfLifeDate: MutableLiveData<String> = MutableLiveData("")
    val isPerishable: MutableLiveData<Boolean> = MutableLiveData(false)
    @SuppressLint("SimpleDateFormat")
    private val formatter = SimpleDateFormat("dd.MM.yyyy")
    private val expirationDate: MutableLiveData<Calendar> = MutableLiveData()
    private val currentDate: MutableLiveData<Date> = MutableLiveData()

    private val paramGrwOlGrundcat: MutableLiveData<String> = MutableLiveData("")
    private val paramGrwUlGrundcat: MutableLiveData<String> = MutableLiveData("")

    private val paramGrsGrundNeg: MutableLiveData<String> = MutableLiveData("")
    private val addGoods: MutableLiveData<Boolean> = MutableLiveData(false)

    val isGoodsAddedAsSurplus: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.isGoodsAddedAsSurplus == true )
    }
    private val isNotRecountCargoUnit: MutableLiveData<Boolean> by lazy { //https://trello.com/c/PRTAVnUP только без признака ВЗЛОМ (обсудили с Колей 17.06.2020)
        MutableLiveData(isTaskPGE.value == true && productInfo.value!!.isWithoutRecount)
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
                if (qualityInfo.value?.get(it!!.second)?.code == "1" || qualityInfo.value?.get(it!!.second)?.code == "2") {
                    convertEizToBei() + countAccept
                } else {
                    if (isNotRecountCargoUnit.value == true && convertEizToBei() >= 0.0) {//Не пересчётная ГЕ, convertEizToBei() >= 0.0 это условие, чтобы не счиатать, если пользователь ввел отрицательное значение
                        if ((countAccept - convertEizToBei()) >= 0.0 ) countAccept - convertEizToBei() else 0.0
                    } else {
                        countAccept
                    }
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
            taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)
        } else {
            taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
        }
        when {
            (it ?: 0.0) > 0.0 -> {
                "+ ${it.toStringFormatted()} ${uom.value?.name}"
            }
            it == 0.0 -> {
                "0 ${uom.value?.name}"
            }
            else -> { //если было введено отрицательное значение
                "${if (countAccept > 0.0) "+ " + countAccept.toStringFormatted() else countAccept.toStringFormatted()} ${uom.value?.name}"
            }
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
            taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(productInfo.value!!)
        } else {
            taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo.value!!)
        }
        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${uom.value?.name}"
        } else { //если было введено отрицательное значение
            "${if (countRefusal > 0.0) "- " + countRefusal.toStringFormatted() else countRefusal.toStringFormatted()} ${uom.value?.name}"
        }
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.combineLatest(enteredProcessingUnitNumber).map {
        val countAccept = if (isTaskPGE.value!!) {
            taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(productInfo.value!!)
        } else {
            taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo.value!!)
        }

        if (isTaskPGE.value!! && isGoodsAddedAsSurplus.value!!) {
            (countAccept + (it?.first ?: 0.0)) >= 0.0 && it?.first != 0.0 && it?.second?.length == 18
        } else if (isNotRecountCargoUnit.value == true) {//Не пересчётная ГЕ
            val quantityAdded = convertEizToBei() + acceptTotalCount.value!! + taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(productInfo.value!!)
            convertEizToBei() > 0.0 && quantityAdded <= productInfo.value!!.orderQuantity.toDouble()
        } else {
            if (qualityInfo.value?.get(spinQualitySelectedPosition.value!!)?.code == "1" || (isTaskPGE.value == true && qualityInfo.value?.get(spinQualitySelectedPosition.value!!)?.code == "2")) {
                (countAccept + (it?.first ?: 0.0)) >= 0.0 && it?.first != 0.0
            } else {
                (it?.first ?: 0.0) > 0.0
            }
        }
    }

    val visibilityLabelBtn: MutableLiveData<Boolean> by lazy {
        //todo https://trello.com/c/LhzZRxzi
        //MutableLiveData(repoInMemoryHolder.manufacturers.value != null)
        MutableLiveData(false)
    }

    val enabledLabelBtn: MutableLiveData<Boolean> by lazy {
        //todo https://trello.com/c/LhzZRxzi
        //MutableLiveData(repoInMemoryHolder.processOrderData.value != null)
        MutableLiveData(false)
    }

    init {
        launchUITryCatch {
            productInfo.value
                    ?.let {
                        if (processGeneralProductService.newProcessGeneralProductService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }.orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }

            searchProductDelegate.init(scanResultHandler = this@GoodsInfoViewModel::handleProductSearchResult)

            if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
                paramGrwOlGrundcat.value = dataBase.getParamGrwOlGrundcat() ?: ""
                paramGrwUlGrundcat.value = dataBase.getParamGrwUlGrundcat() ?: ""
                when {
                    isGoodsAddedAsSurplus.value == true -> { //товар, который не числится в задании https://trello.com/c/im9rJqrU
                        enteredProcessingUnitNumber.value = productInfo.value?.processingUnit ?: ""
                        suffix.value = uom.value?.name
                        qualityInfo.value = dataBase.getSurplusInfoForPGE()
                    }
                    isDiscrepancy.value!! -> {
                        suffix.value = uom.value?.name
                        val processingUnitsOfProduct = taskManager.getReceivingTask()?.taskRepository?.getProducts()?.getProcessingUnitsOfProduct(productInfo.value!!.materialNumber)
                        count.value = if ( (processingUnitsOfProduct?.size ?: 0) > 1) { //если у товара две ЕО
                            val countOrderQuantity = processingUnitsOfProduct!!.map {unitInfo ->
                                unitInfo.orderQuantity.toDouble()
                            }.sumByDouble {
                                it
                            }
                            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountProductNotProcessedOfProductPGEOfProcessingUnits(productInfo.value!!, countOrderQuantity).toStringFormatted()
                        } else {
                            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountProductNotProcessedOfProductPGE(productInfo.value!!).toStringFormatted()
                        }
                        if (isNotRecountCargoUnit.value == true) {
                            qualityInfo.value = dataBase.getQualityInfoPGENotRecountBreaking()
                        } else {
                            qualityInfo.value = dataBase.getQualityInfoPGEForDiscrepancy()
                        }
                    }
                    else -> {// обычный товар https://trello.com/c/OMjrZPhg
                        suffix.value = productInfo.value?.purchaseOrderUnits?.name
                        if (isNotRecountCargoUnit.value == true) {
                            qualityInfo.value = dataBase.getQualityInfoPGENotRecountBreaking()
                        } else {
                            qualityInfo.value = dataBase.getQualityInfoPGE()
                        }
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

            spinQuality.value =
                    qualityInfo.value
                            ?.map {
                                it.name
                            }
                            ?: emptyList()
            paramGrsGrundNeg.value = dataBase.getParamGrsGrundNeg().orEmpty()

            /** определяем, что товар скоропорт https://trello.com/c/8sOTWtB7 */
            val paramGrzUffMhdhb = dataBase.getParamGrzUffMhdhb()?.toInt() ?: 60
            val productGeneralShelfLifeInt = productInfo.value?.generalShelfLife?.toInt() ?: 0
            val productRemainingShelfLifeInt = productInfo.value?.remainingShelfLife?.toInt() ?: 0
            val productMhdhbDays = productInfo.value?.mhdhbDays ?: 0
            isPerishable.value =
                    productGeneralShelfLifeInt > 0
                            || productRemainingShelfLifeInt > 0
                            || (productMhdhbDays in 1 until paramGrzUffMhdhb)
            if (isPerishable.value == true) {
                shelfLifeInfo.value = dataBase.getTermControlInfo()
                spinShelfLife.value = shelfLifeInfo.value?.map {it.name}.orEmpty()
                currentDate.value = timeMonitor.getServerDate()
                expirationDate.value = Calendar.getInstance()
                shelfLifeDate.value =
                        currentDate.value
                                ?.let {
                                    DateTimeUtil.formatDate(it, Constants.DATE_FORMAT_dd_mm_yyyy)
                                }
                                .orEmpty()
                if ( productGeneralShelfLifeInt > 0 || productRemainingShelfLifeInt > 0 ) { //https://trello.com/c/XSAxdgjt
                    generalShelfLife.value = productInfo.value?.generalShelfLife.orEmpty()
                    remainingShelfLife.value = productInfo.value?.remainingShelfLife.orEmpty()
                } else {
                    generalShelfLife.value = productInfo.value?.mhdhbDays.toString()
                    remainingShelfLife.value = productInfo.value?.mhdrzDays.toString()
                }
            }
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        screenNavigator.goBack()
        return false
    }

    fun onClickDetails() {
        screenNavigator.openGoodsDetailsScreen(productInfo.value!!)
    }

    fun onScanResult(data: String) {
        addGoods.value = false
        onClickAdd()
        if (addGoods.value == true) {
            searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
        }
    }

    override fun onClickPosition(position: Int) {
        spinReasonRejectionSelectedPosition.value = position
        if (isTaskPGE.value == true) {
            productInfo.value = processingUnitsOfProduct.value?.get(position)
            tvAccept.value = context.getString(R.string.accept, "${productInfo.value?.purchaseOrderUnits?.name}=${productInfo.value?.quantityInvest?.toDouble().toStringFormatted()} ${productInfo.value?.uom?.name}")

            productInfo.value
                    ?.let {
                        if (processGeneralProductService.newProcessGeneralProductService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                        }
                    }
                    ?.orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                    }
        }
    }

    fun onClickPositionSpinQuality(position: Int) {
        launchUITryCatch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(qualityInfo.value!![position].code)
        }
    }

    fun onClickPositionSpinShelfLife(position: Int){
        spinShelfLifeSelectedPosition.value = position
    }

    private suspend fun updateDataSpinReasonRejection(selectedQuality: String) {
        launchUITryCatch {
            if (isTaskPGE.value == true) {
                spinReasonRejectionSelectedPosition.value = 0
                processingUnitsOfProduct.value = taskManager.getReceivingTask()?.taskRepository?.getProducts()?.getProcessingUnitsOfProduct(productInfo.value!!.materialNumber)
                spinReasonRejection.value = processingUnitsOfProduct.value?.map {
                    "ЕО - " + it.processingUnit
                }
            } else {
                screenNavigator.showProgressLoadingData(::handleFailure)
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

    private fun convertEizToBei() : Double {
        var addNewCount = countValue.value!!.toDouble()
        if (isEizUnit.value!!) {
            addNewCount *= productInfo.value?.quantityInvest?.toDouble() ?: 1.0
        }
        return addNewCount
    }

    fun onClickApply() {
        isClickApply.value = true
        onClickAdd()
    }

    fun onClickAdd() {
        //ПГЕ - Расчет количеств по товару
        if (isTaskPGE.value == true) {
            if (isGoodsAddedAsSurplus.value == true) { //GRZ. ПГЕ. Добавление товара, который не числится в задании https://trello.com/c/im9rJqrU
                processGeneralProductService.setProcessingUnitNumber(enteredProcessingUnitNumber.value!!)
                processGeneralProductService.add(convertEizToBei().toString(), qualityInfo.value!![spinQualitySelectedPosition.value!!].code, enteredProcessingUnitNumber.value!!)
                clickBtnApply()
            } else if (isNotRecountCargoUnit.value == true) { //не пересчетная ГЕ
                if ((convertEizToBei() +
                                acceptTotalCount.value!! +
                                taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(productInfo.value!!)) <= productInfo.value!!.orderQuantity.toDouble()) {
                    processGeneralProductService.addNotRecountPGE(acceptTotalCount.value.toString(), convertEizToBei().toString(), qualityInfo.value!![spinQualitySelectedPosition.value!!].code, spinReasonRejection.value!![spinReasonRejectionSelectedPosition.value!!].substring(5))
                    clickBtnApply()
                } else {
                    screenNavigator.openAlertUnableSaveNegativeQuantity()
                }

            } else {
                //карточка https://trello.com/c/8sOTWtB7
                if (isPerishable.value == true) {
                    //ПГЕ-скоропорт. в блок-схеме лист 7 "Карточка товара ППП" блок - 7.2
                    addPerishablePGE()
                } else {
                    //ПГЕ-обычный товар. в блок-схеме лист 7 "Карточка товара ПГЕ" блок - 7.6
                    addOrdinaryGoodsPGE()
                }
            }
            return
        }

        //ППП - Расчет количеств по товару
        if (isPerishable.value == true) {
            //ППП-скоропорт. в блок-схеме лист 6 "Карточка товара ППП" блок - 6.3
            addPerishable()
        } else {
            //ППП-обычный товар. в блок-схеме лист 6 "Карточка товара ППП" блок - 6.8
            addOrdinaryGoods()
        }
    }

    //ППП-обычный товар. в блок-схеме лист 6 "Карточка товара ППП" блок - 6.8
    private fun addOrdinaryGoods() {
        //блок 6.16
        if (processGeneralProductService.countEqualOrigQuantityPPP(countValue.value!!)) {//блок 6.16 (да)
            //блок 6.172
            saveCategory()
        } else {//блок 6.16 (нет)
            //блок 6.22
            if (processGeneralProductService.countMoreOrigQuantityPPP(countValue.value!!)) {//блок 6.22 (да)
                //блок 6.58
                checkParamGrsGrundNeg()
            } else {//блок 6.22 (нет)
                //блок 6.26
                if (productInfo.value!!.uom.code == "G") {//блок 6.26 (да)
                    //блок 6.49
                    val roundingQuantity = processGeneralProductService.getRoundingQuantityPPP() - countValue.value!! // "- countValue.value!!" -> этого в блок-схеме нету, но без этого не правильно расчитывается необходимость округления, добавлено при отработке карточки https://trello.com/c/hElr3cn3
                    //блок 6.90
                    if (roundingQuantity <= productInfo.value!!.roundingShortages.toDouble()) {//блок 6.90 (да)
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
                                    count.value = (countValue.value!! + roundingQuantity).toString()
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
        if (spinShelfLifeSelectedPosition.value == shelfLifeInfo.value!!.indexOfLast {it.code == "001"}) {
            //блок 6.146
            expirationDate.value!!.time = formatter.parse(shelfLifeDate.value)
        } else {
            //блок 6.144
            expirationDate.value!!.time = formatter.parse(shelfLifeDate.value)
            expirationDate.value!!.add(Calendar.DATE, generalShelfLife.value?.toInt() ?: 0)
        }

        //блок 6.152
        val currentTypeDiscrepancies =
                qualityInfo.value
                        ?.get(spinQualitySelectedPosition.value ?: 0)
                        ?.code
                        .orEmpty()
        if (expirationDate.value!!.time <= currentDate.value
                && currentTypeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
            //блок 6.158
            screenNavigator.openShelfLifeExpiredDialog(
                    //блок 6.170
                    yesCallbackFunc = {
                        //блок 6.174
                        spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "7"}
                    }
            )
            return
        }

        //блоки 6.157 и 6.182
        if ( Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days > remainingShelfLife.value?.toLong() ?: 0 ) {
            //блок 6.192
            addOrdinaryGoods()
            return
        }

        //блок 6.184
        screenNavigator.openShelfLifeExpiresDialog(
                //блок 6.189
                noCallbackFunc = {
                    //блок 6.191
                    spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "7"}
                },
                //блок 6.188
                yesCallbackFunc = {
                    //блок 6.192
                    addOrdinaryGoods()
                },
                expiresThrough = Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days.toString()
        )
    }

    //ППП блок 6.58
    private fun checkParamGrsGrundNeg() {
        if (processGeneralProductService.checkParam(paramGrsGrundNeg.value!!)) {//блок 6.58 (да)
            //блок 6.93
            val countWithoutParamGrsGrundNeg = processGeneralProductService.countWithoutParamGrsGrundNegPPP(paramGrsGrundNeg.value.orEmpty())
            //блок 6.130
            if (countWithoutParamGrsGrundNeg == 0.0) {//блок 6.130 (да)
                //блок 6.121
                processGeneralProductService.removeDiscrepancyFromProduct(paramGrsGrundNeg.value.orEmpty(), productInfo.value?.processingUnit.orEmpty())
                //блок 6.172
                saveCategory()
            } else {//блок 6.130 (нет)
                //блок 6.147
                if (countWithoutParamGrsGrundNeg > 0.0) {//блок 6.147 (да)
                    //блок 6.145
                    processGeneralProductService.addWithoutUnderload(paramGrsGrundNeg.value.orEmpty(), countWithoutParamGrsGrundNeg.toString(), productInfo.value?.processingUnit.orEmpty())
                    //блок 6.172
                    saveCategory()
                } else {//блок 6.147 (нет)
                    //блок 6.155
                    processGeneralProductService.removeDiscrepancyFromProduct(paramGrsGrundNeg.value.orEmpty(), productInfo.value?.processingUnit.orEmpty())
                    noParamGrsGrundNeg()
                }
            }
        } else {//блок 6.58 (нет)
            noParamGrsGrundNeg()
        }
    }

    //ППП блок 6.163
    private fun noParamGrsGrundNeg() {
        if (productInfo.value!!.uom.code == "G") {//блок 6.163 (да)
            //блок 6.167
            val roundingQuantity = processGeneralProductService.getRoundingQuantityPPP() - countValue.value!! // "- countValue.value!!" -> этого в блок-схеме нету, но без этого не правильно расчитывается необходимость округления, добавлено при доработке карточки https://trello.com/c/hElr3cn3
            //блок 6.173
            if (roundingQuantity <= productInfo.value!!.roundingSurplus.toDouble()) {//блок 6.173 (да)
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
                            count.value = (countValue.value!! + roundingQuantity).toString()
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

    //ППП блок 6.172
    private fun saveCategory() {
        if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
            processGeneralProductService.add(acceptTotalCount.value!!.toString(), "1", productInfo.value!!.processingUnit)
        } else {
            processGeneralProductService.add(count.value!!, reasonRejectionInfo.value!![spinReasonRejectionSelectedPosition.value!!].code, productInfo.value!!.processingUnit)
        }

        //ППП блок 6.176
        clickBtnApply()
    }

    //ППП блок 6.176 и ПГЕ блок 7.188
    private fun clickBtnApply() {
        addGoods.value = true
        if (isClickApply.value!!) {
            screenNavigator.goBack()
        } else {
            count.value = "0"
        }
    }

    //ППП блок 6.187
    private fun calculationOverdelivery() {
        //блок 6.187
        countOverdelivery.value = productInfo.value!!.orderQuantity.toDouble() /**- processGeneralProductService.getQuantityCapitalized()*/ + (productInfo.value!!.overdToleranceLimit.toDouble() / 100) * productInfo.value!!.orderQuantity.toDouble()
        //блок 6.190
        if (processGeneralProductService.getQuantityAllCategoryPPP(countValue.value!!) > countOverdelivery.value!!) {//блок 6.190 (да)
            //блок 6.193
            screenNavigator.openAlertCountMoreOverdelivery()
        } else {//блок 6.190 (нет)
            if (productInfo.value!!.origQuantity.toDouble() > productInfo.value!!.orderQuantity.toDouble()) {
                val calculationOne = productInfo.value!!.origQuantity.toDouble() - productInfo.value!!.orderQuantity.toDouble()
                val calculationTwo = productInfo.value!!.origQuantity.toDouble() - processGeneralProductService.getQuantityAllCategoryPPP(countValue.value!!)
                val calculation = if (calculationOne < calculationTwo) calculationOne else calculationTwo
                if (calculation > 0.0) {
                    processGeneralProductService.add(calculation.toString(), "41", productInfo.value!!.processingUnit)
                }
            }
            //блок 6.196
            if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code == "1") {
                saveCategory()
            } else {
                if (processGeneralProductService.categNormNotOrderMoreOrigQuantity()) {
                    screenNavigator.openAlertCountMoreOverdelivery()
                } else {
                    saveCategory()
                }
            }

        }
    }



    //ПГЕ-обычный товар. в блок-схеме лист 7 "Карточка товара ПГЕ" блок - 7.6
    private fun addOrdinaryGoodsPGE() {
        //блок 7.15
        if (processGeneralProductService.countEqualOrigQuantityPGE(convertEizToBei())) {//блок 7.15 (да)
            //блок 7.177
            saveCategoryPGE(true)
        } else {//блок 7.15 (нет)
            //блок 7.21 (processGeneralProductService.getOpenQuantityPGE - блок 7.11)
            //Logg.d { "processGeneralProductService.getQuantityAllCategoryPGE ${processGeneralProductService.getQuantityAllCategoryPGE(convertEizToBei())} " }
            //Logg.d { "processGeneralProductService.getOpenQuantityPGE ${processGeneralProductService.getOpenQuantityPGE(paramGrwOlGrundcat.value!!, paramGrwUlGrundcat.value!!)} " }
            if (processGeneralProductService.getQuantityAllCategoryPGE(convertEizToBei()) > processGeneralProductService.getOpenQuantityPGE(paramGrwOlGrundcat.value!!, paramGrwUlGrundcat.value!!)) {
                //блок 7.55
                checkParamGrwUlGrundcat()
            } else {
                //блок 7.43
                if (productInfo.value!!.uom.code == "G") {
                    //блок 7.63
                    val roundingQuantity = processGeneralProductService.getRoundingQuantityPGE() - countValue.value!! // "- countValue.value!!" -> этого в блок-схеме нету, но без этого не правильно расчитывается необходимость округления, добавлено при доработке карточки https://trello.com/c/hElr3cn3
                    //блок 7.110
                    if (roundingQuantity <= productInfo.value!!.roundingShortages.toDouble()) {//блок 7.110 (да)
                        //блок 7.156
                        screenNavigator.openRoundingIssueDialog(
                                //блок 7.163
                                noCallbackFunc = {
                                    //блок 7.177
                                    saveCategoryPGE(true)
                                },
                                //блок 7.164
                                yesCallbackFunc = {
                                    //блок 7.169
                                    count.value = (countValue.value!! + roundingQuantity).toString()
                                    //блок 7.177
                                    saveCategoryPGE(true)
                                }
                        )
                    } else {//блок 7.110 (нет)
                        //блок 7.177
                        saveCategoryPGE(true)
                    }
                } else {
                    //блок 7.177
                    saveCategoryPGE(true)
                }
            }
        }
    }

    //ПГЕ-скоропорт. в блок-схеме лист 7 "Карточка товара ПГЕ" блок - 7.2
    private fun addPerishablePGE() {
        //блок 7.103
        if (qualityInfo.value?.get(spinQualitySelectedPosition.value ?: 0)?.code != "1") {
            addOrdinaryGoodsPGE()
            return
        }

        if (!isCorrectDate(shelfLifeDate.value)) {
            screenNavigator.openAlertNotCorrectDate()
            return
        }

        //блок 7.134
        if (spinShelfLifeSelectedPosition.value == shelfLifeInfo.value!!.indexOfLast {it.code == "001"}) {
            //блок 7.154
            expirationDate.value!!.time = formatter.parse(shelfLifeDate.value)
        } else {
            //блок 7.153
            expirationDate.value!!.time = formatter.parse(shelfLifeDate.value)
            expirationDate.value!!.add(Calendar.DATE, generalShelfLife.value?.toInt() ?: 0)
        }

        //блок 7.160
        val currentTypeDiscrepancies =
                qualityInfo.value
                        ?.get(spinQualitySelectedPosition.value ?: 0)
                        ?.code
                        .orEmpty()
        if (expirationDate.value!!.time <= currentDate.value
                && currentTypeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
            //блок 7.168
            screenNavigator.openShelfLifeExpiredDialog(
                    //блок 7.180
                    yesCallbackFunc = {
                        //блок 7.183
                        spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "5"} //устанавливаем брак складской, Маша Стоян
                        //spinShelfLifeSelectedPosition.value = shelfLifeInfo.value!!.indexOfLast {it.code == "001"} закомичено, т.к. данное поле активно только при категориях Норма и Излишек
                    }
            )
            return
        }

        //блоки 7.167 и 7.190
        if ( Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days > remainingShelfLife.value?.toLong() ?: 0 ) {
            //блок 7.203
            addOrdinaryGoodsPGE()
            return
        }

        //блок 7.194
        screenNavigator.openShelfLifeExpiresDialog(
                //блок 7.200
                noCallbackFunc = {
                    //блок 7.201
                    spinQualitySelectedPosition.value = qualityInfo.value!!.indexOfLast {it.code == "5"} //устанавливаем брак складской♂
                    //spinShelfLifeSelectedPosition.value = shelfLifeInfo.value!!.indexOfLast {it.code == "001"} закомиченно, т.к. данное поле активно только при категориях Норма и Излишек
                },
                //блок 7.199
                yesCallbackFunc = {
                    //блок 7.203
                    addOrdinaryGoodsPGE()
                },
                expiresThrough = Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days.toString()
        )
    }

    //ПГЕ блоки 7.177 и 7.185
    private fun saveCategoryPGE(checkCategoryType: Boolean) {
        //если checkCategoryType==true, значит перед сохранением (блок 7.185) делаем блок 7.177
        if (checkCategoryType && qualityInfo.value!![spinQualitySelectedPosition.value!!].code == paramGrwOlGrundcat.value) { //блок 7.177 (да)
            //блоки 7.181 и 7.185
        } else {
            //блок 7.185
            processGeneralProductService.add(convertEizToBei().toString(), qualityInfo.value!![spinQualitySelectedPosition.value!!].code, spinReasonRejection.value!![spinReasonRejectionSelectedPosition.value!!].substring(5))
        }

        //ПГЕ блок 7.188
        clickBtnApply()
    }

    //ПГЕ блок 7.55
    private fun checkParamGrwUlGrundcat() {
        if (processGeneralProductService.checkParam(paramGrwUlGrundcat.value!!)) {//блок 7.55 (да)
            //блок 7.96
            val countWithoutParamGrwUlGrundcat = processGeneralProductService.countWithoutParamGrwUlGrundcatPGE(paramGrwOlGrundcat.value.orEmpty(), paramGrwUlGrundcat.value.orEmpty())
            //блок 7.135
            if (countWithoutParamGrwUlGrundcat == 0.0) {//блок 7.135 (да)
                //блок 7.133
                processGeneralProductService.removeDiscrepancyFromProduct(paramGrwUlGrundcat.value.orEmpty(), spinReasonRejection.value!![spinReasonRejectionSelectedPosition.value!!].substring(5))
                //блок 7.177
                saveCategoryPGE(true)
            } else {//блок 7.135 (нет)
                //блок 7.157
                if (countWithoutParamGrwUlGrundcat > 0.0) {//блок 7.157 (да)
                    //блок 7.155
                    processGeneralProductService.addWithoutUnderload(paramGrwUlGrundcat.value.orEmpty(), countWithoutParamGrwUlGrundcat.toString(), spinReasonRejection.value!![spinReasonRejectionSelectedPosition.value!!].substring(5))
                    //блок 7.177
                    saveCategoryPGE(true)
                } else {//блок 7.157 (нет)
                    //блок 7.165
                    processGeneralProductService.removeDiscrepancyFromProduct(paramGrwUlGrundcat.value.orEmpty(), spinReasonRejection.value!![spinReasonRejectionSelectedPosition.value!!].substring(5))
                    noParamGrwUlGrundcat()
                }
            }
        } else {//блок 7.55 (нет)
            noParamGrwUlGrundcat()
        }
    }

    //ПГЕ блок 7.174
    private fun noParamGrwUlGrundcat() {
        if (productInfo.value!!.uom.code == "G") {//блок 7.174 (да)
            //блок 7.178
            val roundingQuantity = processGeneralProductService.getRoundingQuantityPGE() - countValue.value!! // "- countValue.value!!" -> этого в блок-схеме нету, но без этого не правильно расчитывается необходимость округления, добавлено при доработке карточки https://trello.com/c/hElr3cn3
            //блок 7.184
            if (roundingQuantity <= productInfo.value!!.roundingSurplus.toDouble()) {//блок 7.184 (да)
                //блок 7.186
                screenNavigator.openRoundingIssueDialog(
                        //блок 7.195
                        noCallbackFunc = {
                            //блок 7.198
                            checkParamGrwOlGrundcat()
                        },
                        //блок 7.193
                        yesCallbackFunc = {
                            //блок 7.196
                            count.value = (countValue.value!! + roundingQuantity).toString()
                            //блок 7.177
                            saveCategoryPGE(true)
                        }
                )
            } else {//блок 7.184 (нет)
                //блок  7.198
                checkParamGrwOlGrundcat()
            }
        } else {//блок 7.174 (нет)
            //блок 7.198
            checkParamGrwOlGrundcat()
        }
    }

    //ПГЕ блок 7.198
    private fun checkParamGrwOlGrundcat() {
        if (qualityInfo.value!![spinQualitySelectedPosition.value!!].code == "1" || qualityInfo.value!![spinQualitySelectedPosition.value!!].code == paramGrwOlGrundcat.value!!) {//блок 7.198 (да)
            //блок 7.202
            val countNormAndParamMoreOrderQuantity = processGeneralProductService.countNormAndParamMoreOrderQuantityPGE(paramGrwOlGrundcat.value!!, convertEizToBei())
            if (countNormAndParamMoreOrderQuantity) {//блок 7.202 (да)
                //блок 7.205
                screenNavigator.openAlertCountMoreCargoUnitDialog(
                        //блок 7.208
                        yesCallbackFunc = {
                            //блок 7.209
                            processGeneralProductService.addCountMoreCargoUnit(paramGrwOlGrundcat.value!!, convertEizToBei(), spinReasonRejection.value!![spinReasonRejectionSelectedPosition.value!!].substring(5))
                            //блок 7.188 (переходим минуя 7.177 и 7.185, т.к. мы уже сохранили данные в блоке 7.209)
                            clickBtnApply()
                        }
                )
            } else {//блок 7.202 (нет)
                //блок 7.185
                saveCategoryPGE(false)
            }
        } else {//блок 7.198 (нет)
            //блок 7.185
            saveCategoryPGE(false)
        }
    }

    fun onBackPressed() {
        if (enabledApplyButton.value == true) {
            screenNavigator.openUnsavedDataDialog(
                    yesCallbackFunc = {
                        screenNavigator.goBack()
                    }
            )
        } else {
            screenNavigator.goBack()
        }
    }

    fun onClickLabel() { //https://trello.com/c/LhzZRxzi

        /**launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)

            packCodeNetRequest(
                    PackCodeParams(
                            marketNumber = sessionInfo.market.orEmpty(),
                            taskType = manager.getTaskTypeCode(),
                            parent = manager.currentTask.value!!.taskInfo.number,
                            deviceIp = deviceIp.value.orEmpty(),
                            material = good.value!!.material,
                            order = raw.value!!.order,
                            quantity = total.value!!,
                            categoryCode = categories.value!![categoryPosition.value!!].code,
                            defectCode = defects.value!![defectPosition.value!!].code,
                            personnelNumber = sessionInfo.personnelNumber ?: ""
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure) { packCodeResult ->
                good.value?.let {good ->
                    good.packs.add(0,
                            Pack(
                                    material = good.material,
                                    materialOsn = raw.value!!.materialOsn,
                                    materialDef = raw.value!!.material,
                                    code = packCodeResult.packCode,
                                    order = raw.value!!.order,
                                    quantity = total.value!!,
                                    category = categories.value!![categoryPosition.value!!],
                                    defect = defects.value!![defectPosition.value!!]
                            )
                    )

                    manager.updateCurrentGood(good)
                    manager.onTaskChanged()
                }

                launchUITryCatch {
                    val productTime = Calendar.getInstance()
                    productTime.add(Calendar.MINUTE, database.getPcpExpirTimeMm())

                    val planAufFinish = Calendar.getInstance()
                    planAufFinish.add(Calendar.MINUTE, getTimeInMinutes(packCodeResult.dataLabel.planAufFinish, packCodeResult.dataLabel.planAufUnit))
                    planAufFinish.add(Calendar.MINUTE, database.getPcpContTimeMm())

                    val dateExpir = packCodeResult.dataLabel.time.toIntOrNull()?.let { time ->
                        val dateExpiration = Calendar.getInstance()
                        when (packCodeResult.dataLabel.timeType.toIntOrNull()) {
                            1 -> dateExpiration.add(Calendar.HOUR_OF_DAY, time)
                            2 -> dateExpiration.add(Calendar.DAY_OF_YEAR, time)
                        }

                        dateExpiration
                    }

                    val barCodeText = "(01)${getFormattedEan(packCodeResult.dataLabel.ean, total.value!!)}" +
                            "(91)${packCodeResult.packCode}"

                    val barcode = barCodeText.replace("(", "").replace(")", "")

                    printLabel(LabelInfo(
                            quantity = "${total.value!!}  ${good.value?.units?.name}",
                            codeCont = packCodeResult.packCode,
                            planAufFinish = SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy_hh_mm, Locale.getDefault()).format(planAufFinish.time),
                            aufnr = raw.value!!.order,
                            nameOsn = raw.value!!.name,
                            dateExpir = dateExpir?.let { SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy_hh_mm, Locale.getDefault()).format(it.time) }
                                    ?: "",
                            goodsName = "***БРАК*** ${packCodeResult.dataLabel.materialName}",
                            weigher = sessionInfo.personnelNumber ?: "",
                            productTime = SimpleDateFormat(Constants.DATE_FORMAT_dd_mm_yyyy_hh_mm, Locale.getDefault()).format(productTime.time),
                            goodsCode = packCodeResult.dataLabel.material.takeLast(6),
                            barcode = barcode,
                            barcodeText = barCodeText,
                            printTime = Date()
                    ))

                    weighted.value = 0.0
                    weightField.value = "0"
                }
            }
        }*/
    }

}
