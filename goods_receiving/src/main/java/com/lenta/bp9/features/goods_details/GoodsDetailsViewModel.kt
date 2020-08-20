package com.lenta.bp9.features.goods_details

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.model.processing.ProcessExciseAlcoBoxAccPGEService
import com.lenta.bp9.model.processing.ProcessMercuryProductService
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
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

    private val isNonExciseAlcoProduct: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value?.type == ProductType.NonExciseAlcohol)
    }

    private val isBatchProduct: MutableLiveData<Boolean> by lazy {
        MutableLiveData(productInfo.value!!.type == ProductType.NonExciseAlcohol && !productInfo.value!!.isBoxFl && !productInfo.value!!.isMarkFl)
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
            goodsDetails.postValue(
                    processMercuryProductService.getGoodsDetails()?.mapIndexed { index, discrepancy ->
                        GoodsDetailsCategoriesItem(
                                number = index + 1,
                                name = "${reasonRejectionInfo.value?.firstOrNull { it.code == discrepancy.typeDiscrepancies }?.name}",
                                nameBatch = "",
                                visibilityNameBatch = false,
                                quantityWithUom = "${discrepancy.numberDiscrepancies.toDouble().toStringFormatted()} ${uom.value?.name}",
                                isNormDiscrepancies = isNormDiscrepancies(discrepancy.typeDiscrepancies),
                                typeDiscrepancies = discrepancy.typeDiscrepancies,
                                materialNumber = productInfo.value?.materialNumber ?: "",
                                batchDiscrepancies = null,
                                even = index % 2 == 0
                        )
                    }?.reversed()
            )
        } else if (productInfo.value?.type == ProductType.ExciseAlcohol
                && productInfo.value?.isBoxFl == true
                && !boxNumberForTaskPGEBoxAlco.value.isNullOrEmpty()) { //ПГЕ алкоголь, коробочный учет https://trello.com/c/TzUSGIH7
            goodsDetails.value =
                    processExciseAlcoBoxAccPGEService
                            .getGoodsDetails(boxNumberForTaskPGEBoxAlco.value.orEmpty())
                            ?.mapIndexed { index, discrepancy ->
                                getItemAlcoBoxPGE(discrepancy, index)
                            }
                            ?.reversed()
        } else {
            goodsDetails.postValue(
                    if (isBatchProduct.value == true || productInfo.value?.isSet == true) {
                        val productNumbers = if (productInfo.value?.isSet == true) {
                            repoInMemoryHolder.sets.value?.filter {
                                it.setNumber == productInfo.value?.materialNumber
                            }?.map {
                                it.componentNumber
                            } ?: emptyList()
                        } else {
                            listOf(productInfo.value!!.materialNumber)
                        }
                        taskManager
                                .getReceivingTask()
                                ?.taskRepository
                                ?.getBatchesDiscrepancies()
                                ?.findBatchDiscrepanciesOfProducts(productNumbers)
                                ?.mapIndexed { index, discrepancy ->
                                    val nameItem = if (productInfo.value?.isSet == true) {
                                        "${discrepancy.getMaterialLastSix()} ${reasonRejectionInfo.value?.firstOrNull { it.code == discrepancy.typeDiscrepancies }?.name}"
                                    } else {
                                        "${reasonRejectionInfo.value?.firstOrNull { it.code == discrepancy.typeDiscrepancies }?.name}"
                                    }
                                    GoodsDetailsCategoriesItem(
                                            number = index + 1,
                                            name = nameItem,
                                            nameBatch = "ДР-${discrepancy.bottlingDate} // ${getManufacturerName(discrepancy.egais)}",
                                            visibilityNameBatch = true,
                                            quantityWithUom = "${discrepancy.numberDiscrepancies.toDouble().toStringFormatted()} ${uom.value?.name}",
                                            isNormDiscrepancies = isNormDiscrepancies(discrepancy.typeDiscrepancies),
                                            typeDiscrepancies = discrepancy.typeDiscrepancies,
                                            materialNumber = discrepancy.materialNumber,
                                            batchDiscrepancies = discrepancy,
                                            even = index % 2 == 0
                                    )
                                }?.reversed()
                    } else {
                        taskManager
                                .getReceivingTask()
                                ?.taskRepository
                                ?.getProductsDiscrepancies()
                                ?.findProductDiscrepanciesOfProduct(productInfo.value!!)
                                ?.mapIndexed { index, discrepancy ->
                                    GoodsDetailsCategoriesItem(
                                            number = index + 1,
                                            name = "${reasonRejectionInfo.value?.firstOrNull { it.code == discrepancy.typeDiscrepancies }?.name}",
                                            nameBatch = "",
                                            visibilityNameBatch = false,
                                            quantityWithUom = "${discrepancy.numberDiscrepancies.toDouble().toStringFormatted()} ${uom.value?.name}",
                                            isNormDiscrepancies = isNormDiscrepancies(discrepancy.typeDiscrepancies),
                                            typeDiscrepancies = discrepancy.typeDiscrepancies,
                                            materialNumber = productInfo.value?.materialNumber
                                                    ?: "",
                                            batchDiscrepancies = null,
                                            even = index % 2 == 0
                                    )
                                }?.reversed()
                    }
            )
        }
        categoriesSelectionsHelper.clearPositions()
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
                    val materialNumber = goodsDetails.value?.get(position)?.materialNumber.orEmpty()
                    val typeDiscrepancies = goodsDetails.value?.get(position)?.typeDiscrepancies.orEmpty()

                    if (isScreenPGEBoxAlcoInfo.value == true) {
                        processExciseAlcoBoxAccPGEService.delBoxesStampsDiscrepancies(typeDiscrepancies)
                    }

                    if (boxNumberForTaskPGEBoxAlco.value.isNullOrEmpty()) {
                        taskRepository
                                ?.getProductsDiscrepancies()
                                ?.deleteProductDiscrepancy(materialNumber, typeDiscrepancies)
                    } else {
                        processExciseAlcoBoxAccPGEService.delBoxStampsDiscrepancies(boxNumberForTaskPGEBoxAlco.value.orEmpty(), typeDiscrepancies)
                    }

                    taskRepository
                            ?.getBoxesDiscrepancies()
                            ?.deleteBoxesDiscrepanciesForProductAndDiscrepancies(materialNumber, typeDiscrepancies)

                    taskRepository
                            ?.getExciseStampsDiscrepancies()
                            ?.deleteExciseStampsDiscrepanciesForProductAndDiscrepancies(materialNumber, typeDiscrepancies)

                    if (isVetProduct.value == true) {
                        taskManager
                                .getReceivingTask()
                                ?.taskRepository
                                ?.getMercuryDiscrepancies()
                                ?.deleteMercuryDiscrepancyOfProduct(materialNumber, typeDiscrepancies)

                        processMercuryProductService.updateDiscrepancy()
                    }

                    if (isNonExciseAlcoProduct.value!!) {
                        goodsDetails.value?.get(position)!!.batchDiscrepancies?.let {
                            taskManager
                                    .getReceivingTask()
                                    ?.taskRepository
                                    ?.getBatchesDiscrepancies()
                                    ?.deleteBatchDiscrepancies(it)
                        }
                    }
                }
            }
        }
        updateProduct()
    }

    private fun getManufacturerName(manufactureCode: String?): String {
        return repoInMemoryHolder.manufacturers.value?.findLast { manufacture ->
            manufacture.code == manufactureCode
        }?.name ?: ""
    }

}
