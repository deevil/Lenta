package com.lenta.bp9.features.goods_details

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.goods_list.ListCountedItem
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccPGEService
import com.lenta.bp9.model.processing.ProcessMercuryProductService
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import java.lang.Exception
import java.text.SimpleDateFormat
import javax.inject.Inject

class GoodsDetailsViewModel : CoreViewModel() {

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var dataBase: IDataBaseRepo

    @Inject
    lateinit var processMercuryProductService: ProcessMercuryProductService

    @Inject
    lateinit var processExciseAlcoBoxAccPGEService: ProcessExciseAlcoBoxAccPGEService

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat("dd.MM.yyyy")

    @SuppressLint("SimpleDateFormat")
    private val formatterERP = SimpleDateFormat(Constants.DATE_FORMAT_yyyy_mm_dd)

    private val taskRepository by lazy { taskManager.getReceivingTask()?.taskRepository }
    private val boxNumberForTaskPGEBoxAlco: MutableLiveData<String> = MutableLiveData("")
    private val isScreenPGEBoxAlcoInfo: MutableLiveData<Boolean> = MutableLiveData(false)

    val productInfo: MutableLiveData<TaskProductInfo> = MutableLiveData()
    val uom: MutableLiveData<Uom?> by lazy {
        if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.DirectSupplier) {
            MutableLiveData(productInfo.value?.purchaseOrderUnits)
        } else {
            MutableLiveData(productInfo.value?.uom)
        }
    }

    val goodsDetails: MutableLiveData<List<GoodsDetailsCategoriesItem>> = MutableLiveData()

    private val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>> = MutableLiveData()
    private val isVetProduct: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.isVet ?: false)
    }

    private val isBatchProduct: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.type == ProductType.NonExciseAlcohol
                && productInfo.value?.isBoxFl == false
                && productInfo.value?.isMarkFl == false
        )
    }

    private val isZBatch: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.isZBatches == true
                && productInfo.value?.isVet == false) //см. SearchProductDelegate
    }

    val categoriesSelectionsHelper = SelectionItemsHelper()
    val enabledDelBtn: MutableLiveData<Boolean> = categoriesSelectionsHelper.selectedPositions.map {
        !categoriesSelectionsHelper.selectedPositions.value.isNullOrEmpty()
    }

    init {
        launchUITryCatch {
            val task = taskManager.getReceivingTask()
            val taskType = task?.taskHeader?.taskType
            reasonRejectionInfo.value = if (taskType == TaskType.RecalculationCargoUnit) {
                dataBase.getQualityInfoPGE()?.map {
                    it.convertToReasonRejectionInfo()
                }
            } else {
                val qualityInfoForDiscrepancy = dataBase.getQualityInfoForDiscrepancy()?.map {
                    it.convertToReasonRejectionInfo()
                }.orEmpty()
                val allReasonRejectionInfo = dataBase.getAllReasonRejectionInfo().orEmpty()
                val discrepancyErrorUPD = dataBase.getQualityErrorUPD()
                        ?.map {
                            it.convertToReasonRejectionInfo()
                        }.orEmpty()
                qualityInfoForDiscrepancy + allReasonRejectionInfo + discrepancyErrorUPD
            }

            updateProduct()
        }

    }

    fun getTitle(): String {
        return if (isBatchProduct.value == true) {
            taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
        } else {
            "${productInfo.value!!.getMaterialLastSix()} ${productInfo.value!!.description}"
        }
    }

    fun initProduct(initProductInfo: TaskProductInfo) {
        productInfo.value = initProductInfo
    }

    fun initBoxNumber(_initBoxNumber: String) {
        boxNumberForTaskPGEBoxAlco.value = _initBoxNumber
    }

    fun initScreenPGEBoxAlcoInfo(_initScreenPGEBoxAlcoInfo: Boolean) {
        isScreenPGEBoxAlcoInfo.value = _initScreenPGEBoxAlcoInfo
    }

    private fun isNormDiscrepancies(typeDiscrepancies: String) : Boolean {
        return when (repoInMemoryHolder.taskList.value?.taskListLoadingMode) {
            TaskListLoadingMode.PGE -> typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM || typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
            else -> typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM
        }
    }

    private fun updateProduct() {
        if (isVetProduct.value == true && productInfo.value?.isNotEdit == false) {
            updatingInfoVetProducts()
        } else if (productInfo.value?.type == ProductType.ExciseAlcohol
                && productInfo.value?.isBoxFl == true
                && !boxNumberForTaskPGEBoxAlco.value.isNullOrEmpty()) {
            updatingInfoPGEAlcoBoxProducts()
        } else if (isBatchProduct.value == true || productInfo.value?.isSet == true) {
            updatingInfoBatchesOrSetsProducts()
        } else if (productInfo.value?.isZBatches == true && productInfo.value?.isVet == false) {
            updatingInfoZBatches()
        } else {
            updatingInfoOtherProducts()
        }
        categoriesSelectionsHelper.clearPositions()
    }

    private fun updatingInfoVetProducts() {
        goodsDetails.value =
                processMercuryProductService
                        .getGoodsDetails()
                        ?.mapIndexed { index, discrepancy ->
                            GoodsDetailsCategoriesItem(
                                    number = index + 1,
                                    name = reasonRejectionInfo.value?.firstOrNull { it.code == discrepancy.typeDiscrepancies }?.name.orEmpty(),
                                    nameBatch = "",
                                    visibilityNameBatch = false,
                                    quantityWithUom = "${discrepancy.numberDiscrepancies.toDouble().toStringFormatted()} ${uom.value?.name.orEmpty()}",
                                    isNormDiscrepancies = isNormDiscrepancies(discrepancy.typeDiscrepancies),
                                    typeDiscrepancies = discrepancy.typeDiscrepancies,
                                    materialNumber = productInfo.value?.materialNumber.orEmpty(),
                                    batchDiscrepancies = null,
                                    zBatchDiscrepancies = null,
                                    even = index % 2 == 0
                            )
                        }
                        ?.reversed()
    }

    private fun updatingInfoPGEAlcoBoxProducts() {
        goodsDetails.value =
                processExciseAlcoBoxAccPGEService
                        .getGoodsDetails(boxNumberForTaskPGEBoxAlco.value.orEmpty())
                        ?.mapIndexed { index, discrepancy ->
                            getItemAlcoBoxPGE(discrepancy, index)
                        }
                        ?.reversed()
    }

    private fun updatingInfoBatchesOrSetsProducts() {
        val sets =
                repoInMemoryHolder.sets.value
                        ?.filter { it.setNumber == productInfo.value?.materialNumber }
                        ?.map { it.componentNumber }
                        .orEmpty()

        val productNumbers =
                if (productInfo.value?.isSet == true) {
                    sets
                } else {
                    listOf(productInfo.value?.materialNumber.orEmpty())
                }

        val batchDiscrepanciesOfProducts =
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getBatchesDiscrepancies()
                        ?.findBatchDiscrepanciesOfProducts(productNumbers)

        goodsDetails.value =
                batchDiscrepanciesOfProducts
                        ?.mapIndexed { index, discrepancy ->
                            getItemBatchesOrSets(discrepancy, index)
                        }
                        ?.reversed()
    }

    private fun getItemBatchesOrSets(discrepancy: TaskBatchesDiscrepancies, index: Int) : GoodsDetailsCategoriesItem {
        val reasonRejectionName =
                reasonRejectionInfo.value
                        ?.firstOrNull { it.code == discrepancy.typeDiscrepancies }
                        ?.name
                        .orEmpty()

        val nameItem =
                if (productInfo.value?.isSet == true) {
                    "${discrepancy.getMaterialLastSix()} $reasonRejectionName"
                } else {
                    reasonRejectionName
                }

        return GoodsDetailsCategoriesItem(
                number = index + 1,
                name = nameItem,
                nameBatch = "ДР-${discrepancy.bottlingDate} // ${getManufacturerName(discrepancy.egais)}",
                visibilityNameBatch = true,
                quantityWithUom = "${discrepancy.numberDiscrepancies.toDouble().toStringFormatted()} ${uom.value?.name.orEmpty()}",
                isNormDiscrepancies = isNormDiscrepancies(discrepancy.typeDiscrepancies),
                typeDiscrepancies = discrepancy.typeDiscrepancies,
                materialNumber = discrepancy.materialNumber,
                batchDiscrepancies = discrepancy,
                zBatchDiscrepancies = null,
                even = index % 2 == 0
        )
    }

    private fun updatingInfoZBatches() {
        val itemsZBatchesDiscrepancies: ArrayList<GoodsDetailsCategoriesItem> = ArrayList()
        var quantityItem = 0
        val materialNumber = productInfo.value?.materialNumber.orEmpty()
        taskRepository
                ?.getZBatchesDiscrepancies()
                ?.findZBatchDiscrepanciesOfProduct(materialNumber)
                ?.mapIndexed { index, discrepancy ->
                    itemsZBatchesDiscrepancies.add(
                            getItemsZBatchesDiscrepanciesOfBatch(index, discrepancy)
                    )
                    quantityItem = index + 1
                }

        //todo это для ПГЕ
        val processingUnitsOfProduct = taskRepository
                ?.getProducts()
                ?.getProcessingUnitsOfProduct(materialNumber)

        val processingUnits = processingUnitsOfProduct?.map { it.processingUnit }.orEmpty()
        val isShelfLifeObtainedFromEWM =
                repoInMemoryHolder.taskZBatchInfo.value
                        ?.findLast {
                            it.materialNumber == materialNumber
                                    && processingUnits.all { processingUnit -> processingUnit == it.processingUnit}
                        }
                        ?.shelfLifeDate
                        ?.isNotEmpty()
                        ?: false
        //todo end

        if (taskManager.getTaskType() == TaskType.DirectSupplier || (taskManager.getTaskType() == TaskType.RecalculationCargoUnit && !isShelfLifeObtainedFromEWM)) {
            taskManager
                    .getReceivingTask()
                    ?.taskRepository
                    ?.getProductsDiscrepancies()
                    ?.findProductDiscrepanciesOfProduct(materialNumber)
                    ?.filter { it.typeDiscrepancies != TYPE_DISCREPANCIES_QUALITY_NORM }
                    ?.groupBy { it.materialNumber}
                    ?.forEach { groupByMaterialNumberProduct ->
                        groupByMaterialNumberProduct.value
                                .groupBy { it.typeDiscrepancies }
                                .forEach { groupByProductDiscrepancies ->
                                    val countDiscrepancies =
                                            groupByProductDiscrepancies.value
                                                    .map { it.numberDiscrepancies }
                                                    .sumByDouble { it.toDoubleOrNull() ?: 0.0 }

                                    groupByProductDiscrepancies.value
                                            .first()
                                            .let { product ->
                                                itemsZBatchesDiscrepancies.add(
                                                        GoodsDetailsCategoriesItem(
                                                                number = quantityItem + 1,
                                                                name = reasonRejectionInfo.value?.firstOrNull { it.code == product.typeDiscrepancies }?.name.orEmpty(),
                                                                nameBatch = "",
                                                                visibilityNameBatch = false,
                                                                quantityWithUom = "${countDiscrepancies.toStringFormatted()} ${uom.value?.name.orEmpty()}",
                                                                isNormDiscrepancies = isNormDiscrepancies(product.typeDiscrepancies),
                                                                typeDiscrepancies = product.typeDiscrepancies,
                                                                materialNumber = materialNumber,
                                                                batchDiscrepancies = null,
                                                                zBatchDiscrepancies = null,
                                                                even = quantityItem % 2 == 0
                                                        )
                                                )
                                                quantityItem++
                                            }
                                }
                    }
        }


        goodsDetails.value = itemsZBatchesDiscrepancies.reversed()
    }

    private fun getItemsZBatchesDiscrepanciesOfBatch(index: Int, discrepancy: TaskZBatchesDiscrepancies): GoodsDetailsCategoriesItem {
        val partySignOfZBatch =
                taskRepository
                        ?.getZBatchesDiscrepancies()
                        ?.findPartySignOfZBatch(discrepancy)

        val partySign = partySignOfZBatch?.partySign ?: PartySignsTypeOfZBatches.None

        val shelfLifeOrProductionDate = getShelfLifeOrProductionDate(partySign, partySignOfZBatch?.shelfLifeDate.orEmpty(), partySignOfZBatch?.productionDate.orEmpty())

        return GoodsDetailsCategoriesItem(
                number = index + 1,
                name = reasonRejectionInfo.value?.firstOrNull { it.code == discrepancy.typeDiscrepancies }?.name.orEmpty(),
                nameBatch = "${partySign.partySignsTypeString}-$shelfLifeOrProductionDate // ${getManufacturerNameZBatch(discrepancy.manufactureCode)}",
                visibilityNameBatch = true,
                quantityWithUom = "${discrepancy.numberDiscrepancies.toDouble().toStringFormatted()} ${uom.value?.name.orEmpty()}",
                isNormDiscrepancies = isNormDiscrepancies(discrepancy.typeDiscrepancies),
                typeDiscrepancies = discrepancy.typeDiscrepancies,
                materialNumber = discrepancy.materialNumber,
                batchDiscrepancies = null,
                zBatchDiscrepancies = discrepancy,
                even = index % 2 == 0
        )
    }

    private fun getShelfLifeOrProductionDate(partySign: PartySignsTypeOfZBatches, shelfLifeDate: String, productionDate: String): String {
        return try {
            when(partySign) {
                PartySignsTypeOfZBatches.ProductionDate -> {
                    productionDate
                            .takeIf { it.isNotEmpty() }
                            ?.let { formatterRU.format(formatterERP.parse(it)) }
                            .orEmpty()
                }
                PartySignsTypeOfZBatches.ShelfLife -> {
                    shelfLifeDate
                            .takeIf { it.isNotEmpty() }
                            ?.let { formatterRU.format(formatterERP.parse(it)) }
                            .orEmpty()
                }
                else -> ""
            }
        }
        catch (e: Exception) {
            Logg.e { "e: $e" }
            ""
        }
    }

    private fun updatingInfoOtherProducts() {
        val itemsProductDiscrepancies: ArrayList<GoodsDetailsCategoriesItem> = ArrayList()
        val materialNumber = productInfo.value?.materialNumber.orEmpty()
        var index = 0

        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.getProductsDiscrepancies()
                ?.findProductDiscrepanciesOfProduct(materialNumber)
                ?.groupBy { it.materialNumber}
                ?.forEach { groupByMaterialNumberProduct ->
                    groupByMaterialNumberProduct.value
                            .groupBy { it.typeDiscrepancies }
                            .forEach { groupByProductDiscrepancies ->
                                val countDiscrepancies =
                                        groupByProductDiscrepancies.value
                                                .map { it.numberDiscrepancies }
                                                .sumByDouble { it.toDoubleOrNull() ?: 0.0 }

                                groupByProductDiscrepancies.value
                                        .first()
                                        .let { product ->
                                            itemsProductDiscrepancies.add(
                                                    GoodsDetailsCategoriesItem(
                                                            number = index + 1,
                                                            name = reasonRejectionInfo.value?.firstOrNull { it.code == product.typeDiscrepancies }?.name.orEmpty(),
                                                            nameBatch = "",
                                                            visibilityNameBatch = false,
                                                            quantityWithUom = "${countDiscrepancies.toStringFormatted()} ${uom.value?.name.orEmpty()}",
                                                            isNormDiscrepancies = isNormDiscrepancies(product.typeDiscrepancies),
                                                            typeDiscrepancies = product.typeDiscrepancies,
                                                            materialNumber = materialNumber,
                                                            batchDiscrepancies = null,
                                                            zBatchDiscrepancies = null,
                                                            even = index % 2 == 0
                                                    )
                                            )
                                            index++
                                        }
                            }
                }

        goodsDetails.value = itemsProductDiscrepancies.reversed()
    }


    private fun getItemAlcoBoxPGE(discrepancy: TaskProductDiscrepancies, index: Int) : GoodsDetailsCategoriesItem {
        val itemName = reasonRejectionInfo.value?.firstOrNull { it.code == discrepancy.typeDiscrepancies }?.name.orEmpty()
        val quantityWithUom = "${discrepancy.numberDiscrepancies.toDouble().toStringFormatted()} ${uom.value?.name.orEmpty()}"
        val materialNumber = productInfo.value?.materialNumber.orEmpty()

        return GoodsDetailsCategoriesItem(
                number = index + 1,
                name = itemName,
                nameBatch = "",
                visibilityNameBatch = false,
                quantityWithUom = quantityWithUom,
                isNormDiscrepancies = isNormDiscrepancies(discrepancy.typeDiscrepancies),
                typeDiscrepancies = discrepancy.typeDiscrepancies,
                materialNumber = materialNumber,
                batchDiscrepancies = null,
                zBatchDiscrepancies = null,
                even = index % 2 == 0
        )
    }

    fun onClickDelete() {
        if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit && productInfo.value!!.isWithoutRecount) { //если это не пересчетная ГЕ //https://trello.com/c/PRTAVnUP только без признака ВЗЛОМ (обсудили с Колей 17.06.2020)
            categoriesSelectionsHelper.selectedPositions.value?.map { position ->
                taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.deleteProductsDiscrepanciesOfProductOfDiscrepanciesNotRecountPGE(productInfo.value!!, goodsDetails.value?.get(position)!!.typeDiscrepancies)
            }
        } else {
            if (productInfo.value != null && !productInfo.value!!.isNotEdit) {
                categoriesSelectionsHelper.selectedPositions.value?.map { position ->
                    val goodsDetailsItem = goodsDetails.value?.getOrNull(position)
                    val materialNumber = goodsDetailsItem?.materialNumber.orEmpty()
                    val typeDiscrepancies = goodsDetailsItem?.typeDiscrepancies.orEmpty()

                    if (isScreenPGEBoxAlcoInfo.value == true) {
                        processExciseAlcoBoxAccPGEService.delBoxesStampsDiscrepancies(typeDiscrepancies)
                    }

                    if (!boxNumberForTaskPGEBoxAlco.value.isNullOrEmpty()) {
                        processExciseAlcoBoxAccPGEService.delBoxStampsDiscrepancies(boxNumberForTaskPGEBoxAlco.value.orEmpty(), typeDiscrepancies)
                    }

                    taskRepository
                            ?.getBoxesDiscrepancies()
                            ?.deleteBoxesDiscrepanciesForProductAndDiscrepancies(materialNumber, typeDiscrepancies)

                    taskRepository
                            ?.getExciseStampsDiscrepancies()
                            ?.deleteExciseStampsDiscrepanciesForProductAndDiscrepancies(materialNumber, typeDiscrepancies)

                    if (isVetProduct.value == true) {
                            processMercuryProductService.deleteDetails(typeDiscrepancies)
                    }

                    when {
                        isBatchProduct.value == true -> {
                            goodsDetailsItem
                                    ?.batchDiscrepancies
                                    ?.let {
                                        taskRepository
                                                ?.getBatchesDiscrepancies()
                                                ?.deleteBatchDiscrepancies(it)

                                        taskRepository
                                                ?.getProductsDiscrepancies()
                                                ?.deleteProductDiscrepancyByBatch(materialNumber, typeDiscrepancies, it.numberDiscrepancies.toDouble())
                                    }
                        }
                        isZBatch.value == true -> {
                            //todo это для ПГЕ
                            val processingUnitsOfProduct = taskRepository
                                    ?.getProducts()
                                    ?.getProcessingUnitsOfProduct(materialNumber)

                            val processingUnits = processingUnitsOfProduct?.map { it.processingUnit }.orEmpty()
                            val isShelfLifeObtainedFromEWM =
                                    repoInMemoryHolder.taskZBatchInfo.value
                                            ?.findLast {
                                                it.materialNumber == materialNumber
                                                        && processingUnits.all { processingUnit -> processingUnit == it.processingUnit}
                                            }
                                            ?.shelfLifeDate
                                            ?.isNotEmpty()
                                            ?: false
                            //todo end

                            if (taskManager.getTaskType() == TaskType.DirectSupplier) {
                                if (typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM) { //todo в ППП сохраняем норму как по z-партиям, так и по товару, поэтому чистим обе таблицы
                                    goodsDetailsItem
                                            ?.zBatchDiscrepancies
                                            ?.let {
                                                taskRepository
                                                        ?.getZBatchesDiscrepancies()
                                                        ?.deleteZBatchDiscrepancies(it)

                                                taskRepository
                                                        ?.getProductsDiscrepancies()
                                                        ?.deleteProductDiscrepancyByBatch(materialNumber, typeDiscrepancies, it.numberDiscrepancies.toDouble())
                                            }
                                } else { //todo в ППП не сохраняем брак по z-партиям, только по товару, поэтому чистим только таблицу с расхождениями по товарам
                                    taskRepository
                                            ?.getProductsDiscrepancies()
                                            ?.deleteProductDiscrepancy(materialNumber, typeDiscrepancies)
                                }
                                return@map
                            }

                            if (taskManager.getTaskType() == TaskType.RecalculationCargoUnit) { //todo в ПГЕ сохраняем норму как по z-партиям, так и по товару, а брак по z-партиям сохраняем только, когда isShelfLifeObtainedFromEWM == true (СГ получены из EWM), если не получены, то брак по z-партиям не сохраням, только по товарам
                                if ((typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM || typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS)  || isShelfLifeObtainedFromEWM) {
                                    goodsDetailsItem
                                            ?.zBatchDiscrepancies
                                            ?.let {
                                                taskRepository
                                                        ?.getZBatchesDiscrepancies()
                                                        ?.deleteZBatchDiscrepancies(it)

                                                taskRepository
                                                        ?.getProductsDiscrepancies()
                                                        ?.deleteProductDiscrepancyByBatch(materialNumber, typeDiscrepancies, it.numberDiscrepancies.toDouble())
                                            }
                                } else {
                                    taskRepository
                                            ?.getProductsDiscrepancies()
                                            ?.deleteProductDiscrepancy(materialNumber, typeDiscrepancies)
                                }
                            }
                            return@map
                        }
                        else -> {
                            taskRepository
                                    ?.getProductsDiscrepancies()
                                    ?.deleteProductDiscrepancy(materialNumber, typeDiscrepancies)
                        }
                    }
                }
            }
        }
        updateProduct()
    }

    private fun getManufacturerName(manufactureCode: String?): String {
        return repoInMemoryHolder
                .manufacturers.value
                ?.findLast { manufacture ->
                    manufacture.code == manufactureCode
                }
                ?.name
                .orEmpty()
    }

    private fun getManufacturerNameZBatch(manufactureCode: String?): String {
        return repoInMemoryHolder
                .manufacturersForZBatches.value
                ?.findLast { manufacture ->
                    manufacture.manufactureCode == manufactureCode
                }
                ?.manufactureName
                .orEmpty()
    }

}
