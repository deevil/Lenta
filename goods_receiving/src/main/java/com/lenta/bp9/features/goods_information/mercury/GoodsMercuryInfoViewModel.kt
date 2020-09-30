package com.lenta.bp9.features.goods_information.mercury

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.goods_information.base.BaseGoodsInfo
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.data.BarcodeParser
import com.lenta.bp9.features.goods_information.z_batches.task_ppp.ZBatchesInfoPPPViewModel
import com.lenta.bp9.model.processing.*
import com.lenta.bp9.model.task.TaskType
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.shared.models.core.BarcodeData
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import com.lenta.shared.view.OnPositionClickListener
import org.joda.time.DateTime
import org.joda.time.Days
import java.util.*
import javax.inject.Inject

class GoodsMercuryInfoViewModel : BaseGoodsInfo(), OnPositionClickListener {

    @Inject
    lateinit var processMercuryProductService: ProcessMercuryProductService

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var timeMonitor: ITimeMonitor

    val requestFocusToCount: MutableLiveData<Boolean> = MutableLiveData(false)
    val barcodeData: MutableLiveData<BarcodeData> = MutableLiveData()
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

    private val currentDate: MutableLiveData<Date> = MutableLiveData()
    private val expirationDate: MutableLiveData<Calendar> = MutableLiveData()
    val spinQuality: MutableLiveData<List<String>> = MutableLiveData()
    val spinReasonRejection: MutableLiveData<List<String>> = MutableLiveData()


    override val spinProductionDate: MutableLiveData<List<String>> =
            spinManufacturersSelectedPosition
                    .map { pos ->
                    val position = pos ?: 0
                    productInfo.value
                            ?.let { product ->
                                val receivingTask = taskManager.getReceivingTask()
                                receivingTask?.run {
                                    taskRepository
                                            .getMercuryDiscrepancies()
                                            .findMercuryDiscrepanciesOfProduct(product)
                                            .filter { it.manufacturer == spinManufacturers.value?.get(position) }
                                            .groupBy { it.productionDate }
                                            .map { formatterRU.format(formatterEN.parse(it.key)) }
                                }
                            }
                            .orEmpty()
                    }


    private val currentProductionDateFormatterEN: String
        get() {
            return currentProductionDate
                    .takeIf { it.isNotEmpty() }
                    ?.run { formatterEN.format(formatterRU.parse(this)) }
                    .orEmpty()
        }



    override val currentTypeDiscrepanciesCode: String
        get() {
            return if (isTaskPGE.value == true) {
                currentQualityInfoCode
                        .takeIf {
                            it == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                                    || it == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
                        }
                        ?: currentReasonRejectionInfoCode
            } else {
                currentQualityInfoCode
                        .takeIf { it == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM }
                        ?: currentReasonRejectionInfoCode
            }
    }

    val suffix: MutableLiveData<String> = MutableLiveData()
    val generalShelfLife: MutableLiveData<String> = MutableLiveData()
    val remainingShelfLife: MutableLiveData<String> = MutableLiveData()
    private val paramGrzRoundLackRatio: MutableLiveData<String> = MutableLiveData()
    private val paramGrzRoundLackUnit: MutableLiveData<String> = MutableLiveData()
    private val paramGrzRoundHeapRatio: MutableLiveData<String> = MutableLiveData()
    private val isClickApply: MutableLiveData<Boolean> = MutableLiveData(false)

    private val mercuryVolume: MutableLiveData<String> =
            spinManufacturersSelectedPosition
                    .combineLatest(spinProductionDateSelectedPosition)
                    .map {
                        val countMercuryVolume = processMercuryProductService.getVolumeAllMercury(currentManufactureName, currentProductionDateFormatterEN)
                        val mercuryUomName = processMercuryProductService.getUomNameOfMercury(currentManufactureName, currentProductionDateFormatterEN)
                        buildString {
                            append(countMercuryVolume.toStringFormatted())
                            append(" ")
                            append(mercuryUomName)
                        }
                    }

    val tvProductionDate = mercuryVolume.map {
        if (isTaskPGE.value == true && isGoodsAddedAsSurplus.value == true) {
            context.getString(R.string.vet_with_production_date_txt)
        } else {
            context.getString(R.string.vet_with_production_date, it.orEmpty())
        }
    }

    val isDiscrepancy: MutableLiveData<Boolean> = MutableLiveData(false)
    override val isDefect: MutableLiveData<Boolean> =
            spinQualitySelectedPosition
                    .combineLatest(isDiscrepancy)
                    .map {
                        val taskType =
                                taskManager
                                        .getReceivingTask()
                                        ?.taskHeader
                                        ?.taskType

                        isDiscrepancy.value
                                ?.takeIf { !it }
                                ?.run {
                                    if (taskType != TaskType.RecalculationCargoUnit) {
                                        currentQualityInfoCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                                    } else {
                                        currentQualityInfoCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                                                && currentQualityInfoCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
                                    }
                                }
                                ?: true
                    }
    val isEizUnit: MutableLiveData<Boolean> by lazy {
        MutableLiveData(isDiscrepancy.value == false && isGoodsAddedAsSurplus.value == false)
    }

    val isPerishable: MutableLiveData<Boolean> = MutableLiveData()
    val count: MutableLiveData<String> = MutableLiveData("0")
    private val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull() ?: 0.0 }
    private val addGoods: MutableLiveData<Boolean> = MutableLiveData(false)

    val acceptTotalCount: MutableLiveData<Double> by lazy {
        countValue.combineLatest(spinQualitySelectedPosition).map {
            val countAccept =
                    isTaskPGE.value
                            ?.takeIf { isTaskPGEVal ->  isTaskPGEVal }
                            ?.run { processMercuryProductService.getCountAcceptPGE() }
                            ?: processMercuryProductService.getCountAccept()

            if (isTaskPGE.value == true) {
                if (currentQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                        || currentQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS) {
                    convertEizToBei() + countAccept
                } else {
                    countAccept
                }
            } else {
                if (currentQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                    (it?.first ?: 0.0) + countAccept
                } else {
                    countAccept
                }
            }
        }
    }

    val acceptTotalCountWithUom: MutableLiveData<String> = acceptTotalCount.map {
        val countAccept =
                isTaskPGE.value
                        ?.takeIf { isTaskPGEVal ->  isTaskPGEVal }
                        ?.run { processMercuryProductService.getCountAcceptPGE() }
                        ?: processMercuryProductService.getCountAccept()

        if ((it ?: 0.0) > 0.0) {
            "+ ${it.toStringFormatted()} ${uom.value?.name}"
        } else {
            "${if (countAccept > 0.0) "+ " + countAccept.toStringFormatted() else countAccept.toStringFormatted()} ${uom.value?.name}"
        }
    }

    val refusalTotalCount: MutableLiveData<Double> by lazy {
        countValue.combineLatest(spinQualitySelectedPosition).map {
            val countRefusal =
                    isTaskPGE.value
                            ?.takeIf { isTaskPGEVal ->  isTaskPGEVal }
                            ?.run { processMercuryProductService.getCountRefusalPGE() }
                            ?: processMercuryProductService.getCountRefusal()

            if (isTaskPGE.value!!) {
                if (currentQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_UNDERLOAD
                        || currentQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_MARRIAGE_SHIPMENT
                        || currentQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_WAREHOUSE_MARRIAGE) {
                    convertEizToBei() + countRefusal
                } else {
                    countRefusal
                }
            } else {
                if (currentQualityInfoCode != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                    val totalCount = it?.first ?: 0.0
                    totalCount + countRefusal
                } else {
                    countRefusal
                }
            }
        }
    }

    val refusalTotalCountWithUom: MutableLiveData<String> = refusalTotalCount.map {
        val countRefusal =
                isTaskPGE.value
                        ?.takeIf { isTaskPGEVal ->  isTaskPGEVal }
                        ?.run { processMercuryProductService.getCountRefusalPGE() }
                        ?: processMercuryProductService.getCountRefusal()

        if ((it ?: 0.0) > 0.0) {
            "- ${it.toStringFormatted()} ${uom.value?.name.orEmpty()}"
        } else {
            "${if (countRefusal > 0.0) "- " + countRefusal.toStringFormatted() else countRefusal.toStringFormatted()} ${uom.value?.name}"
        }
    }

    private val isNotRecountCargoUnit: MutableLiveData<Boolean> by lazy { //https://trello.com/c/PRTAVnUP только без признака ВЗЛОМ (обсудили с Колей 17.06.2020)
        MutableLiveData(isTaskPGE.value == true && productInfo.value!!.isWithoutRecount)
    }

    val enabledApplyButton: MutableLiveData<Boolean> = countValue.map {
        if (isGoodsAddedAsSurplus.value == true) { //карточка трелло https://trello.com/c/eo1nRdKC) (ТП (меркурий по ПГЕ) -> 3.2.2.16 Обработка расхождений при пересчете ГЕ (Меркурий) -> 2.1.Излишек по товару
            (it ?: 0.0) > 0.0 && currentManufactureName.isNotEmpty()
        } else {
            (it ?: 0.0) > 0.0 && currentManufactureName.isNotEmpty() && currentProductionDate.isNotEmpty()
        }
    }

    init {
        launchUITryCatch {
            productInfo.value
                    ?.let {
                        if (processMercuryProductService.newProcessMercuryProductService(it) == null) {
                            screenNavigator.goBackAndShowAlertWrongProductType()
                            return@launchUITryCatch
                        }
                    }.orIfNull {
                        screenNavigator.goBackAndShowAlertWrongProductType()
                        return@launchUITryCatch
                    }

            searchProductDelegate.init(scanResultHandler = this@GoodsMercuryInfoViewModel::handleProductSearchResult)

            currentDate.value = timeMonitor.getServerDate()
            expirationDate.value = Calendar.getInstance()
            if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
                when {
                    isGoodsAddedAsSurplus.value == true -> {
                        suffix.value = uom.value?.name
                        qualityInfo.value = dataBase.getSurplusInfoForPGE()
                    }
                    isDiscrepancy.value == true -> {
                        suffix.value = uom.value?.name
                        count.value =
                                taskManager
                                        .getReceivingTask()
                                        ?.taskRepository
                                        ?.getProductsDiscrepancies()
                                        ?.getCountProductNotProcessedOfProductPGE(productInfo.value!!)
                                        .toStringFormatted()

                        if (isNotRecountCargoUnit.value == true) {
                            qualityInfo.value = dataBase.getQualityInfoPGENotRecountBreaking().orEmpty()
                        } else {
                            qualityInfo.value = dataBase.getQualityInfoPGEForDiscrepancy().orEmpty()
                        }
                    }
                    else -> {
                        suffix.value = productInfo.value?.purchaseOrderUnits?.name.orEmpty()
                        if (isNotRecountCargoUnit.value == true) {
                            qualityInfo.value = dataBase.getQualityInfoPGENotRecountBreaking().orEmpty()
                        } else {
                            qualityInfo.value = dataBase.getQualityInfoPGE().orEmpty()
                        }
                    }
                }
            } else {
                suffix.value = uom.value?.name.orEmpty()
                if (isDiscrepancy.value == true) {
                    count.value =
                            taskManager
                                    .getReceivingTask()
                                    ?.run {
                                        taskRepository
                                                .getProductsDiscrepancies()
                                                .getCountProductNotProcessedOfProduct(productInfo.value!!)
                                                .toStringFormatted()
                                    }

                    qualityInfo.value = dataBase.getQualityInfoForDiscrepancy().orEmpty()
                    spinQualitySelectedPosition.value =
                            qualityInfo.value
                                    ?.indexOfLast { it.code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_DELIVERY_ERRORS }
                                    ?: -1
                } else {
                    //https://trello.com/c/3AnfqLKo про barcodeData
                    barcodeData.value?.let {
                        if (it.barcodeInfo.isWeight) {
                            val weightInGrams = it.barcodeInfo.weight.toDoubleOrNull() ?: 0.0
                            if (uom.value?.code?.toUpperCase(Locale.getDefault()) == UNIT_KG) {
                                count.value = (weightInGrams / 1000).toStringFormatted()
                            } else {
                                count.value = weightInGrams.toStringFormatted()
                            }
                        }
                    }
                    qualityInfo.value = dataBase.getQualityMercuryInfo().orEmpty()
                }
            }

            /** определяем, что товар скоропорт, это общий для всех алгоритм https://trello.com/c/8sOTWtB7 */
            val paramGrzUffMhdhb = dataBase.getParamGrzUffMhdhb()?.toInt() ?: 60
            val productGeneralShelfLife = productInfo.value?.generalShelfLife?.toInt() ?: 0
            val productRemainingShelfLife = productInfo.value?.remainingShelfLife?.toInt() ?: 0
            val productMhdhbDays = productInfo.value?.mhdhbDays ?: 0
            val productMhdrzDays = productInfo.value?.mhdrzDays ?: 0

            isPerishable.value = productGeneralShelfLife > 0
                    || productRemainingShelfLife > 0
                    || (productMhdhbDays in 1 until paramGrzUffMhdhb)

            isPerishable.value
                    ?.takeIf { it }
                    ?.run {
                        if ( productGeneralShelfLife > 0 || productRemainingShelfLife > 0 ) { //https://trello.com/c/XSAxdgjt
                            generalShelfLife.value = productGeneralShelfLife.toString()
                            remainingShelfLife.value = productRemainingShelfLife.toString()
                        } else {
                            generalShelfLife.value = productMhdhbDays.toString()
                            remainingShelfLife.value = productMhdrzDays.toString()
                        }
                    }

            spinQuality.value = qualityInfo.value?.map { it.name }

            spinManufacturers.value =
                if (isTaskPGE.value == true && isGoodsAddedAsSurplus.value == true) {
                    repoInMemoryHolder.manufacturers.value?.map {
                        it.name
                    }
                } else {
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getMercuryDiscrepancies()
                            ?.getManufacturesOfProduct(productInfo.value!!)
                }

            paramGrzRoundLackRatio.value = dataBase.getParamGrzRoundLackRatio()
            paramGrzRoundLackUnit.value = dataBase.getParamGrzRoundLackUnit()
            paramGrzRoundHeapRatio.value = dataBase.getParamGrzRoundHeapRatio()
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
        launchUITryCatch {
            spinQualitySelectedPosition.value = position
            updateDataSpinReasonRejection(currentQualityInfoCode)
        }
    }

    fun onClickPositionSpinManufacturers(position: Int){
        spinManufacturersSelectedPosition.value = position
    }

    fun onClickPositionSpinProductionDate(position: Int){
        spinProductionDateSelectedPosition.value = position
    }

    private suspend fun updateDataSpinReasonRejection(selectedQuality: String) {
        launchUITryCatch {
            if (isTaskPGE.value == true) {
                spinReasonRejectionSelectedPosition.value = 0
                spinReasonRejection.value = listOf("ЕО - " + productInfo.value!!.processingUnit)
            } else {
                screenNavigator.showProgressLoadingData(::handleFailure)
                reasonRejectionInfo.value = dataBase.getReasonRejectionMercuryInfoOfQuality(selectedQuality)
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

    fun onClickDetails() {
        productInfo.value?.let { screenNavigator.openGoodsDetailsScreen(it) }
    }

    @SuppressLint("SimpleDateFormat")
    fun onClickAdd() {
        if (isTaskPGE.value == true) {
            //меркурий для ПГЕ
            if (isPerishable.value == true) { //https://trello.com/c/fqOMeUob
                expirationDate.value!!.time =
                        currentProductionDate
                                .takeIf { it.isNotEmpty() }
                                ?.run { formatterRU.parse(this) }
                expirationDate.value!!.add(Calendar.DATE, generalShelfLife.value?.toInt() ?: 0)

                if (expirationDate.value!!.time <= currentDate.value
                        && (currentQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                                || currentQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS)) {
                    screenNavigator.openShelfLifeExpiredDialog(
                            yesCallbackFunc = {
                                //устанавливаем брак складской (как и в обычном товаре, Маша Стоян)
                                spinQualitySelectedPosition.value =
                                        qualityInfo.value
                                                ?.indexOfLast { it.code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_WAREHOUSE_MARRIAGE }
                                                ?: -1
                            }
                    )
                } else {
                    if ( Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days > remainingShelfLife.value?.toLong() ?: 0 ) {
                        addProductDiscrepanciesPGE()
                    }  else {
                        screenNavigator.openShelfLifeExpiresDialog(
                                noCallbackFunc = {
                                    //устанавливаем брак складской (как и в обычном товаре, Маша Стоян)
                                    spinQualitySelectedPosition.value =
                                            qualityInfo.value
                                                    ?.indexOfLast { it.code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_WAREHOUSE_MARRIAGE }
                                                    ?: -1
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
                expirationDate.value!!.time =
                        currentProductionDate
                                .takeIf { it.isNotEmpty() }
                                ?.run { formatterRU.parse(this) }
                expirationDate.value!!.add(Calendar.DATE, generalShelfLife.value?.toInt() ?: 0)

                if (expirationDate.value!!.time <= currentDate.value
                        && currentQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                    screenNavigator.openShelfLifeExpiredDialog(
                            yesCallbackFunc = {
                                spinQualitySelectedPosition.value =
                                        qualityInfo.value
                                                ?.indexOfLast { it.code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PRODUCT_QUALITY }
                                                ?: -1
                            }
                    )
                } else {
                    if ( Days.daysBetween(DateTime(currentDate.value), DateTime(expirationDate.value!!.time)).days > remainingShelfLife.value?.toLong() ?: 0 ) {
                        addProductDiscrepancies()
                    }  else {
                        screenNavigator.openShelfLifeExpiresDialog(
                                noCallbackFunc = {
                                    spinQualitySelectedPosition.value =
                                            qualityInfo.value
                                                    ?.indexOfLast { it.code == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PRODUCT_QUALITY }
                                                    ?: -1
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
        processingAddProductDiscrepancies(
                processing = processMercuryProductService.checkConditionsOfPreservationOfProduct(
                                count = count.value ?: "0",
                                typeDiscrepancies = currentTypeDiscrepanciesCode,
                                manufacturer = currentManufactureName,
                                productionDate = currentProductionDateFormatterEN,
                                paramGrzRoundLackRatio = paramGrzRoundLackRatio.value?.replace(",", ".")?.toDouble() ?: 0.0,
                                paramGrzRoundLackUnit = paramGrzRoundLackUnit.value?.replace(",", ".")?.toDouble() ?: 0.0,
                                paramGrzRoundHeapRatio = paramGrzRoundHeapRatio.value?.replace(",", ".")?.toDouble() ?: 0.0),
                addCount = count.value ?: "0",
                typeDiscrepancies = currentTypeDiscrepanciesCode
        )
    }

    private fun processingAddProductDiscrepancies(processing: Int, addCount: String, typeDiscrepancies: String) {
        //меркурий для ППП
        when (processing) {
            PROCESSING_MERCURY_SAVED -> {
                if (currentQualityInfoCode == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM) {
                    processMercuryProductService.add(
                            count = addCount,
                            isConvertUnit = false,
                            typeDiscrepancies = TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM,
                            manufacturer = currentManufactureName,
                            productionDate = currentProductionDateFormatterEN
                    )
                } else {
                    processMercuryProductService.add(
                            count = addCount,
                            isConvertUnit = false,
                            typeDiscrepancies = currentReasonRejectionInfoCode,
                            manufacturer = currentManufactureName,
                            productionDate = currentProductionDateFormatterEN
                    )
                }
                count.value = "0"
                addGoods.value = true
                checkClickApply()
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
                            processingAddProductDiscrepancies(
                                    processing = processMercuryProductService.checkConditionsOfPreservationOfVSD(
                                                    count = addCount,
                                                    typeDiscrepancies = typeDiscrepancies,
                                                    manufacturer = currentManufactureName,
                                                    productionDate = currentProductionDateFormatterEN
                                    ),
                                    addCount = addCount,
                                    typeDiscrepancies = typeDiscrepancies
                            )

                        },
                        yesCallbackFunc = {
                            //- В случае, если пользователь согласился округлить, то фактическое значение приравнивается к плановому
                            val enteredCount = processMercuryProductService.getRoundingQuantityPPP(count = addCount, reasonRejectionCode = typeDiscrepancies)
                            //и переходим к п.2 (проверка по ВСД)
                            processingAddProductDiscrepancies(
                                    processing = processMercuryProductService.checkConditionsOfPreservationOfVSD(
                                                        count = enteredCount.toString(),
                                                        typeDiscrepancies = typeDiscrepancies,
                                                        manufacturer = currentManufactureName,
                                                        productionDate = currentProductionDateFormatterEN
                                    ),
                                    addCount = enteredCount.toString(),
                                    typeDiscrepancies = typeDiscrepancies)

                        }
                )
            }
            PROCESSING_MERCURY_CANT_SAVE_NEGATIVE_NUMBER -> {
                //отображать ошибку «Невозможно сохранить отрицательное количество»
                screenNavigator.openAlertUnableSaveNegativeQuantity()
            }
            PROCESSING_MERCURY_OVERDELIVERY_MORE_EQUAL_NOT_ORDER -> {
                processMercuryProductService
                        .overDeliveryMoreEqualNotOrder(
                                count = addCount,
                                isConvertUnit = false,
                                typeDiscrepancies = currentReasonRejectionInfoCode,
                                manufacturer = currentManufactureName,
                                productionDate = currentProductionDateFormatterEN
                        )
            }
            PROCESSING_MERCURY_OVERDELIVERY_LESS_NOT_ORDER -> {
                processMercuryProductService
                        .overDeliveryLessNotOrder(
                                count = addCount,
                                isConvertUnit = false,
                                typeDiscrepancies = currentReasonRejectionInfoCode,
                                manufacturer = currentManufactureName,
                                productionDate = currentProductionDateFormatterEN
                        )
            }
            PROCESSING_MERCURY_UNKNOWN -> {
                //на Windows Mobile нет действия
            }
        }
        count.value = "0"
    }

    @SuppressLint("SimpleDateFormat")
    private fun addProductDiscrepanciesPGE() {
        val mercuryUom =
                taskManager
                        .getReceivingTask()
                        ?.run {
                            taskRepository
                                    .getMercuryDiscrepancies()
                                    .findMercuryDiscrepanciesOfProduct(productInfo.value!!)
                                    .last { mercuryDiscrepancies ->
                                        mercuryDiscrepancies.manufacturer == currentManufactureName
                                                && mercuryDiscrepancies.productionDate == currentProductionDateFormatterEN
                                    }
                                    .uom
                        }


        //https://trello.com/c/yALoQg2b
        val isConvertUnit = uom.value != mercuryUom

        // обработка ПГЕ Меркурия для товаров, которые добавляются как излишек и отсутствуют в поставке (карточка трелло https://trello.com/c/eo1nRdKC) (ТП (меркурий по ПГЕ) -> 3.2.2.16 Обработка расхождений при пересчете ГЕ (Меркурий) -> 2.1.Излишек по товару)
        // добавляем все кол-во введенное пользователем как и для обычного товара (GoodsInfoViewModel - onClickAdd)
        if (isGoodsAddedAsSurplus.value == true) {
            if (!isCorrectDate(productionDate.value)) {
                screenNavigator.openAlertNotCorrectDate()
                return
            } else {
                val productionDateSave = formatterEN.format(formatterRU.parse(productionDate.value))
                processMercuryProductService.add(
                        count = convertEizToBei().toString(),
                        isConvertUnit = isConvertUnit,
                        typeDiscrepancies = currentQualityInfoCode,
                        manufacturer = currentManufactureName,
                        productionDate = productionDateSave
                )
                count.value = "0"
                addGoods.value = true
                checkClickApply()
                return
            }
        }

        //обработка ПГЕ Меркурия для товаров, которые есть в поставке (карточка трелло https://trello.com/c/eo1nRdKC) (ТП (меркурий по ПГЕ) -> 3.2.2.16 Обработка расхождений при пересчете ГЕ (Меркурий) )
        val checkConditionsOfPreservationPGE =
                processMercuryProductService
                        .checkConditionsOfPreservationPGE(
                                count = convertEizToBei(),
                                isConvertUnit = isConvertUnit,
                                reasonRejectionCode = currentQualityInfoCode,
                                manufacturer = currentManufactureName,
                                productionDate = currentProductionDateFormatterEN
                        )
        when (checkConditionsOfPreservationPGE) {
            PROCESSING_MERCURY_SAVED -> {
                processMercuryProductService.add(
                        count = convertEizToBei().toString(),
                        isConvertUnit = isConvertUnit,
                        typeDiscrepancies = currentQualityInfoCode,
                        manufacturer = currentManufactureName,
                        productionDate = currentProductionDateFormatterEN
                )
                count.value = "0"
                addGoods.value = true
                checkClickApply()
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
                            processMercuryProductService
                                    .addSurplusInQuantityPGE(
                                            count = convertEizToBei(),
                                            isConvertUnit = isConvertUnit,
                                            manufacturer = currentManufactureName,
                                            productionDate = currentProductionDateFormatterEN
                                    )
                            count.value = "0"
                            addGoods.value = true
                            checkClickApply()
                        }
                )
            }
            PROCESSING_MERCURY_NORM_AND_UNDERLOAD_EXCEEDED_INVOICE -> {//4.Особые случаи, 4.2.1.1 кол-во по поставке превышено
                processMercuryProductService
                        .addNormAndUnderloadExceededInvoicePGE(
                                count = convertEizToBei(),
                                isConvertUnit = isConvertUnit,
                                manufacturer = currentManufactureName,
                                productionDate = currentProductionDateFormatterEN
                        )
                count.value = "0"
                addGoods.value = true
                checkClickApply()
            }
            PROCESSING_MERCURY_NORM_AND_UNDERLOAD_EXCEEDED_VET_DOC -> { //4.Особые случаи, 4.2.1.2 кол-во превышает кол-во по ВСД
                processMercuryProductService
                        .addNormAndUnderloadExceededVetDocPGE(
                                count = convertEizToBei(),
                                isConvertUnit = isConvertUnit,
                                manufacturer = currentManufactureName,
                                productionDate = currentProductionDateFormatterEN
                        )
                count.value = "0"
                addGoods.value = true
                checkClickApply()
            }
        }
    }

    fun onClickApply() {
        isClickApply.value = true
        onClickAdd()
    }

    private fun checkClickApply() {
        isClickApply.value
                ?.takeIf { it }
                ?.run {
                    processMercuryProductService.save()
                    screenNavigator.goBack()
                }
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
            val date = formatterRU.parse(checkDate)
            !(checkDate != formatterRU.format(date) || date!! > currentDate.value)
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

    companion object {
        private const val UNIT_KG = "KG"
    }
}

