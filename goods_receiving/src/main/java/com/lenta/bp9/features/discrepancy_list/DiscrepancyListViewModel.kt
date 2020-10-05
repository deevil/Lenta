package com.lenta.bp9.features.discrepancy_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.delegates.ISaveProductDelegate
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.model.processing.ProcessMarkingBoxProductService
import com.lenta.bp9.model.processing.ProcessMarkingProductService
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
import com.lenta.bp9.platform.TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.EndRecountDirectDeliveriesNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import javax.inject.Inject

private const val SELECTED_PAGE_NOT_PROCESSED = 0
private const val PAGE_PROCESSED_WITHOUT_CONTROL = 1
private const val PAGE_PROCESSED_WITH_CONTROL = 2

class DiscrepancyListViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var processMarkingProductService: ProcessMarkingProductService

    @Inject
    lateinit var processMarkingBoxProductService: ProcessMarkingBoxProductService

    @Inject
    lateinit var endRecountDirectDeliveries: EndRecountDirectDeliveriesNetRequest

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var saveProductDelegate: ISaveProductDelegate

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var dataBase: IDataBaseRepo

    val processedSelectionsHelper = SelectionItemsHelper()
    val countNotProcessed: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()
    val countProcessed: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()
    val countControl: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()

    private val taskType by lazy {
        taskManager
                .getReceivingTask()
                ?.taskHeader
                ?.taskType
                ?: TaskType.None
    }

    private val isBatches: MutableLiveData<Boolean> = MutableLiveData(false)
    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    private val paramGrzGrundMarkCode: MutableLiveData<String> = MutableLiveData("")

    val isAlco: MutableLiveData<Boolean> by lazy {
        //проверяем, есть ли алкогольные акцизные товары в задании и стоит ли признак isAlco в задании
        MutableLiveData(!taskManager.getReceivingTask()?.taskRepository?.getProducts()?.getProducts()?.filter { product ->
            product.isBoxFl || product.isMarkFl
        }.isNullOrEmpty() && taskManager.getReceivingTask()?.taskDescription?.isAlco == true)
    }

    //https://trello.com/c/74l1kXcn
    val isMark: MutableLiveData<Boolean> by lazy {
        //проверяем, стоит ли признак isMark в задании (значит есть маркированные товары)
        MutableLiveData(taskManager.getReceivingTask()?.taskDescription?.isMark == true)
    }

    val visibilityCleanButton: MutableLiveData<Boolean> = selectedPage.map {
        if (isAlco.value == true || isMark.value == true) {
            it == PAGE_PROCESSED_WITH_CONTROL
        } else {
            it == PAGE_PROCESSED_WITHOUT_CONTROL
        }
    }

    val enabledCleanButton: MutableLiveData<Boolean> = processedSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty()
    }

    val enabledSaveButton: MutableLiveData<Boolean> = countNotProcessed.map {
        it.isNullOrEmpty()
    }

    val visibilityBatchesButton: MutableLiveData<Boolean> by lazy {
        val taskDescription = taskManager.getReceivingTask()?.taskDescription
        MutableLiveData(taskDescription?.isAlco == true //Z-партии на этом экране не учитываются
                && !(taskType == TaskType.ShipmentPP || taskType == TaskType.ShipmentRC)) //для заданий ОПП и ОРЦ не показываем кнопку Партия, уточнил у Артема
    }

    init {
        launchUITryCatch {
            searchProductDelegate.init(scanResultHandler = this@DiscrepancyListViewModel::handleProductSearchResult)
            paramGrzGrundMarkCode.value = dataBase.getGrzGrundMark().orEmpty()
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        return false
    }

    fun onResume() {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
                qualityInfo.value = dataBase.getQualityInfoPGE()
            } else {
                val qualityInfoForDiscrepancy = dataBase.getQualityInfoForDiscrepancy()?.map {
                    it
                }.orEmpty()
                val allReasonRejectionInfo = dataBase.getAllReasonRejectionInfo()?.map {
                    it.convertToQualityInfo()
                }.orEmpty()
                qualityInfo.value = qualityInfoForDiscrepancy + allReasonRejectionInfo
            }
            updateData()
            screenNavigator.hideProgress()
        }
    }

    private fun updateData() {
        updateCountNotProcessed()
        updateCountProcessed()
        updateCountControl()
    }

    private fun updateCountNotProcessed() {
        val arrayNotCounted: ArrayList<GoodsDiscrepancyItem> = ArrayList()
        var index = 0
        taskManager
                .getReceivingTask()
                ?.let { task ->
                    val taskProductDiscrepancies = task.taskRepository.getProductsDiscrepancies()
                    task.getProcessedProducts()
                            .asSequence()
                            .filter { product ->
                                if (task.taskHeader.taskType == TaskType.RecalculationCargoUnit) {
                                    val countOrderQuantity =
                                            task.taskRepository
                                                    .getProducts()
                                                    .getProcessingUnitsOfProduct(product.materialNumber)
                                                    .map { unitInfo -> unitInfo.orderQuantity.toDouble() }
                                                    .sumByDouble { it }
                                    task.taskRepository.getProductsDiscrepancies().getCountProductNotProcessedOfProductPGEOfProcessingUnits(product, countOrderQuantity) > 0.0
                                } else {
                                    taskProductDiscrepancies.getCountProductNotProcessedOfProduct(product) > 0.0
                                }
                            }.sortedByDescending { sorted ->
                                sorted.materialNumber
                            }
                            .map { productInfo ->
                                if (isBatches.value == true
                                        && productInfo.type == ProductType.NonExciseAlcohol
                                        && !productInfo.isBoxFl
                                        && !productInfo.isMarkFl) {
                                    val batchesInfoOfProduct =
                                            task.taskRepository
                                                    .getBatches()
                                                    .findBatchOfProduct(productInfo)

                                    @Suppress("IMPLICIT_CAST_TO_ANY")
                                    batchesInfoOfProduct
                                            ?.map { batch ->
                                                index += 1
                                                arrayNotCounted.add(
                                                        getItemNotProcessedBatch(
                                                                task = task,
                                                                batch = batch,
                                                                product = productInfo,
                                                                index = index
                                                        )
                                                )
                                            }
                                } else {
                                    index += 1
                                    @Suppress("IMPLICIT_CAST_TO_ANY")
                                    arrayNotCounted.add(
                                            getItemNotProcessedProduct(
                                                    task = task,
                                                    product = productInfo,
                                                    index = index
                                            )
                                    )
                                }
                            }
                            .toList()
                }

        countNotProcessed.value = arrayNotCounted.reversed()

        launchUITryCatch {
            moveToProcessedPageIfNeeded()
        }
    }

    private fun getUomForTaskType(product: TaskProductInfo): Uom {
        val taskType =
                taskManager
                        .getReceivingTask()
                        ?.taskHeader
                        ?.taskType
        return if (taskType == TaskType.DirectSupplier) {
            product.purchaseOrderUnits
        } else {
            product.uom
        }
    }

    private fun getItemNotProcessedBatch(task: ReceivingTask, batch: TaskBatchInfo, product: TaskProductInfo, index: Int): GoodsDiscrepancyItem {
        val uom = getUomForTaskType(product)
        val taskRepository = task.taskRepository
        val batchInfo =
                taskRepository
                        .getBatches()
                        .findBatch(
                                batchNumber = batch.batchNumber,
                                materialNumber = batch.materialNumber,
                                processingUnitNumber = batch.processingUnitNumber
                        )

        val quantityNotProcessedProductBatch =
                taskRepository
                        .getBatchesDiscrepancies()
                        .getCountBatchNotProcessedOfBatch(batch)
                        .toStringFormatted()

        return GoodsDiscrepancyItem(
                number = index,
                name = "${product.getMaterialLastSix()} ${product.description}",
                nameMaxLines = 1,
                nameBatch = "ДР-${batchInfo?.bottlingDate.orEmpty()} // ${getManufacturerName(batchInfo)}",
                visibilityNameBatch = true,
                countRefusalWithUom = "",
                quantityNotProcessedWithUom = "? $quantityNotProcessedProductBatch ${uom.name}",
                discrepanciesName = "",
                productInfo = product,
                productDiscrepancies = null,
                batchInfo = null,
                visibilityCheckBoxControl = true,
                checkBoxControl = false,
                checkStampControl = false,
                even = index % 2 == 0
        )
    }

    private fun getItemNotProcessedProduct(task: ReceivingTask, product: TaskProductInfo, index: Int): GoodsDiscrepancyItem {
        val uom = getUomForTaskType(product)
        val quantityNotProcessedProduct = getQuantityNotProcessedProduct(task, product)
        return GoodsDiscrepancyItem(
                number = index,
                name = "${product.getMaterialLastSix()} ${product.description}",
                nameMaxLines = 2,
                nameBatch = "",
                visibilityNameBatch = false,
                countRefusalWithUom = "",
                quantityNotProcessedWithUom = "? $quantityNotProcessedProduct ${uom.name}",
                discrepanciesName = "",
                productInfo = product,
                productDiscrepancies = null,
                batchInfo = null,
                visibilityCheckBoxControl = true,
                checkBoxControl = false,
                checkStampControl = false,
                even = index % 2 == 0
        )
    }

    private fun getQuantityNotProcessedProduct(task: ReceivingTask, product: TaskProductInfo): String {
        val taskProductDiscrepancies = task.taskRepository.getProductsDiscrepancies()

        return if (task.taskHeader.taskType == TaskType.RecalculationCargoUnit) {
            getCountProductNotProcessedOfProductPGE(task, product)
        } else {
            taskProductDiscrepancies
                    .getCountProductNotProcessedOfProduct(product)
                    .toStringFormatted()
        }
    }

    private fun getCountProductNotProcessedOfProductPGE(task: ReceivingTask, product: TaskProductInfo): String {
        val taskRepository = task.taskRepository
        val taskProductDiscrepancies = taskRepository.getProductsDiscrepancies()

        val processingUnitsOfProduct =
                taskRepository
                        .getProducts()
                        .getProcessingUnitsOfProduct(product.materialNumber)

        val countOrderQuantity =
                processingUnitsOfProduct
                        .map { unitInfo ->
                            unitInfo.orderQuantity.toDouble()
                        }
                        .sumByDouble {
                            it
                        }

        return if (processingUnitsOfProduct.size > 1) {
            taskProductDiscrepancies
                    .getCountProductNotProcessedOfProductPGEOfProcessingUnits(
                            product = product,
                            orderQuantity = countOrderQuantity
                    )
                    .toStringFormatted()
        } else {
            taskProductDiscrepancies
                    .getCountProductNotProcessedOfProductPGE(product)
                    .toStringFormatted()
        }
    }

    private fun filterCountProcessedProduct(productDiscrepancies: TaskProductDiscrepancies): Boolean {
        val isComponent = repoInMemoryHolder.sets.value?.any { set ->
            set.componentNumber == productDiscrepancies.materialNumber
        }
        return if (repoInMemoryHolder.taskList.value?.taskListLoadingMode == TaskListLoadingMode.PGE) {
            !(productDiscrepancies.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM
                    || productDiscrepancies.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS)
                    && isComponent == false
        } else {
            productDiscrepancies.typeDiscrepancies != TYPE_DISCREPANCIES_QUALITY_NORM && isComponent == false
        }
    }

    private fun filterBatchOfProduct(batchInfo: TaskBatchInfo): Boolean {
        val batchDiscrepanciesOfBatch =
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getBatchesDiscrepancies()
                        ?.findBatchDiscrepanciesOfBatch(batchInfo)

        val loadingMode =
                repoInMemoryHolder
                        .taskList.value
                        ?.taskListLoadingMode
                        ?: TaskListLoadingMode.None

        return if (loadingMode == TaskListLoadingMode.PGE) {
            batchDiscrepanciesOfBatch
                    ?.filter { findBatchDiscrPGE ->
                        !(findBatchDiscrPGE.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM
                                || findBatchDiscrPGE.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_PGE_SURPLUS)
                    }
                    ?.any()
                    ?: false
        } else {
            batchDiscrepanciesOfBatch
                    ?.filter { findBatchDiscr ->
                        findBatchDiscr.typeDiscrepancies != TYPE_DISCREPANCIES_QUALITY_NORM
                    }
                    ?.any()
                    ?: false
        }
    }

    private fun updateCountProcessed() {
        val arrayCounted: ArrayList<GoodsDiscrepancyItem> = ArrayList()
        var index = 0
        var addBatchProduct = ""
        taskManager
                .getReceivingTask()
                ?.let { task ->
                    task.getProcessedProductsDiscrepancies()
                            .filter { filterCountProcessedProduct(it) }
                            .sortedByDescending { it.materialNumber }
                            .map { productDiscrepancies ->
                                val productInfo = getProductInfoForProcessedProduct(task, productDiscrepancies.materialNumber)
                                if (isBatches.value == true
                                        && productInfo?.type == ProductType.NonExciseAlcohol
                                        && !productInfo.isBoxFl
                                        && !productInfo.isMarkFl) {
                                    //показываем партии без разбивки по расхождениям
                                    @Suppress("IMPLICIT_CAST_TO_ANY")
                                    addBatchProduct
                                            .takeIf {
                                                it != productInfo.materialNumber
                                            }
                                            ?.run {
                                                addBatchProduct = productInfo.materialNumber
                                                getBatchesInfoOfProcessedBatches(task, productInfo)
                                                        ?.map { batch ->
                                                            index += 1
                                                            val itemProcessedBatch = getItemProcessedBatch(
                                                                    task = task,
                                                                    batch = batch,
                                                                    product = productInfo,
                                                                    productDiscrepancies = productDiscrepancies,
                                                                    index = index
                                                            )
                                                            arrayCounted.add(itemProcessedBatch)
                                                        }
                                            }
                                } else {
                                    @Suppress("IMPLICIT_CAST_TO_ANY")
                                    productInfo?.let { product ->
                                        index += 1
                                        val itemProcessedProduct = getItemProcessedProduct(
                                                product = product,
                                                productDiscrepancies = productDiscrepancies,
                                                index = index
                                        )
                                        arrayCounted.add(itemProcessedProduct)
                                    }
                                }
                            }
                }

        countProcessed.value = arrayCounted.reversed()

        processedSelectionsHelper.clearPositions()
    }

    private fun getProductInfoForProcessedProduct(task: ReceivingTask, materialNumber: String): TaskProductInfo? {
        return task.taskRepository
                .getProducts()
                .findProduct(materialNumber)
    }

    private fun getBatchesInfoOfProcessedBatches(task: ReceivingTask, product: TaskProductInfo): List<TaskBatchInfo>? {
        return task.taskRepository
                .getBatches()
                .findBatchOfProduct(product)
                ?.filter { findBatch ->
                    filterBatchOfProduct(findBatch)
                }
    }

    private fun getItemProcessedBatch(task: ReceivingTask, batch: TaskBatchInfo, product: TaskProductInfo, productDiscrepancies: TaskProductDiscrepancies, index: Int): GoodsDiscrepancyItem {
        val uom = getUomForTaskType(product)
        val batchInfo =
                task.taskRepository
                        .getBatches()
                        .findBatch(
                                batchNumber = batch.batchNumber,
                                materialNumber = batch.materialNumber,
                                processingUnitNumber = batch.processingUnitNumber
                        )

        return GoodsDiscrepancyItem(
                number = index,
                name = "${product.getMaterialLastSix()} ${product.description}",
                nameMaxLines = 1,
                nameBatch = "",
                visibilityNameBatch = true,
                countRefusalWithUom = getRefusalTotalCountWithUomBatch(batchInfo, uom),
                quantityNotProcessedWithUom = "",
                discrepanciesName = "ДР-${batchInfo?.bottlingDate} // ${getManufacturerName(batchInfo)}",
                productInfo = product,
                productDiscrepancies = productDiscrepancies,
                batchInfo = null,
                visibilityCheckBoxControl = true,
                checkBoxControl = false,
                checkStampControl = false,
                even = index % 2 == 0
        )
    }

    private fun getItemProcessedProduct(product: TaskProductInfo, productDiscrepancies: TaskProductDiscrepancies, index: Int): GoodsDiscrepancyItem {
        val uom = getUomForTaskType(product)
        val discrepanciesName =
                qualityInfo.value
                        ?.findLast { it.code == productDiscrepancies.typeDiscrepancies }
                        ?.name
                        .orEmpty()

        return GoodsDiscrepancyItem(
                number = index,
                name = "${product.getMaterialLastSix()} ${product.description}",
                nameMaxLines = 2,
                nameBatch = "",
                visibilityNameBatch = false,
                countRefusalWithUom = "- ${productDiscrepancies.numberDiscrepancies.toDouble().toStringFormatted()} ${uom.name}",
                quantityNotProcessedWithUom = "",
                discrepanciesName = discrepanciesName,
                productInfo = product,
                productDiscrepancies = productDiscrepancies,
                batchInfo = null,
                visibilityCheckBoxControl = true,
                checkBoxControl = false,
                checkStampControl = false,
                even = index % 2 == 0
        )
    }

    private fun updateCountControl() {
        //https://trello.com/c/9K1FZnUU, отображать перечень товаров (марочного алкоголя), по которым не пройден хотя бы один из видов контроля
        //https://trello.com/c/vcymT9Kp отображать перечень товаров (маркированный товар, сигареты, обувь), по которым не пройден хотя бы один из видов контроля (пока сделано только для одного режима)
        countControl.value =
                taskManager
                        .getReceivingTask()
                        ?.getProcessedProducts()
                        ?.asSequence()
                        ?.filter { goodsInfo ->
                            if (goodsInfo.markType != MarkType.None) { //маркированный товар (сигареты, обувь)
                                markingProductControlNotPassed(goodsInfo)
                            } else { //марочный алкоголь
                                normEnteredButControlNotPassed(goodsInfo)
                            }
                        }
                        ?.sortedByDescending { it.materialNumber }
                        ?.mapIndexed { index, productInfo ->
                            getItemControlProduct(productInfo, index)
                        }
                        ?.toList()
                        ?.reversed()
    }

    private fun getItemControlProduct(product: TaskProductInfo, index: Int): GoodsDiscrepancyItem {
        val isControlBoxesOfProduct =
                if (product.markType == MarkType.None) {
                    taskManager
                            .getReceivingTask()
                            ?.controlBoxesOfProduct(product)
                            ?: false
                } else {
                    false
                }

        val isControlExciseStampsOfProduct =
                if (product.markType == MarkType.None) {
                    taskManager
                            .getReceivingTask()
                            ?.controlExciseStampsOfProduct(product)
                            ?: false
                } else {
                    false
                }

        return GoodsDiscrepancyItem(
                number = index + 1,
                name = "${product.getMaterialLastSix()} ${product.description}",
                nameMaxLines = 2,
                nameBatch = "",
                visibilityNameBatch = false,
                countRefusalWithUom = "",
                quantityNotProcessedWithUom = "",
                discrepanciesName = "",
                productInfo = product,
                productDiscrepancies = null,
                batchInfo = null,
                visibilityCheckBoxControl = product.markType == MarkType.None, //скрываем для маркированного товара, это пока реализовано для режима TASK_MARK не пустая, BSTME=ST и признак IS_USE_ALTERN_MEINS не установлен (режимы https://trello.com/c/NGsFfWgB), остальные режимы еще будут дорабатываться
                checkBoxControl = isControlBoxesOfProduct,
                checkStampControl = isControlExciseStampsOfProduct,
                even = index % 2 == 0
        )
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption.orEmpty()
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    private fun getMaterialNumberFromPrediction(position: Int): String {
        return if (((isAlco.value == true || isMark.value == true) && selectedPage.value == PAGE_PROCESSED_WITH_CONTROL)
                || ((isAlco.value == false && isMark.value == false) && selectedPage.value == PAGE_PROCESSED_WITHOUT_CONTROL)) {
            countProcessed.value
                    ?.get(position)
                    ?.productInfo
                    ?.materialNumber
                    .orEmpty()
        } else {
            countControl.value
                    ?.get(position)
                    ?.productInfo
                    ?.materialNumber
                    .orEmpty()
        }
    }

    fun onClickItemPosition(position: Int) {
        val selectedNotProcessedProduct = countNotProcessed.value?.getOrNull(position)?.productInfo
        val selectedMaterialNumber =
                if (selectedPage.value == SELECTED_PAGE_NOT_PROCESSED) {
                    selectedNotProcessedProduct?.materialNumber.orEmpty()
                } else {
                    getMaterialNumberFromPrediction(position)
                }

        val mode = repoInMemoryHolder.taskList.value?.taskListLoadingMode ?: TaskListLoadingMode.None

        //коробочный учет для ПРИЕМКИ https://trello.com/c/WeGFSdAW
        if (mode == TaskListLoadingMode.Receiving
                && selectedNotProcessedProduct?.isBoxFl == true
                && selectedPage.value == SELECTED_PAGE_NOT_PROCESSED) {
            screenNavigator.openExciseAlcoBoxProductFailureScreen(selectedNotProcessedProduct)
            return
        }

        //https://trello.com/c/vcymT9Kp маркированый товар ППП
        if (mode == TaskListLoadingMode.Receiving
                && selectedNotProcessedProduct?.markType != MarkType.None
                && selectedPage.value == SELECTED_PAGE_NOT_PROCESSED
                && selectedNotProcessedProduct != null) {
            screenNavigator.openMarkingProductFailureScreen(selectedNotProcessedProduct)
            return
        }

        searchProductDelegate.searchCode(
                code = selectedMaterialNumber,
                fromScan = false,
                isDiscrepancy = true
        )
    }

    fun onClickClean() {
        processedSelectionsHelper
                .selectedPositions
                .value
                ?.map { position ->
                    countProcessed.value
                            ?.get(position)
                            ?.productInfo
                            ?.let { selectedProduct ->
                                if (!selectedProduct.isNotEdit) {
                                    if (isBatches.value == true && !selectedProduct.isBoxFl && !selectedProduct.isMarkFl) {
                                        //удаляем все расхождения, кроме Нормы
                                        if (selectedProduct.isSet) {
                                            repoInMemoryHolder
                                                    .sets
                                                    .value
                                                    ?.filter {
                                                        it.setNumber == selectedProduct.materialNumber
                                                    }?.map { component ->
                                                        deleteDiscrepanciesNotNormForSet(component.componentNumber)
                                                    }
                                        }
                                        deleteDiscrepanciesNotNormForProduct(selectedProduct)
                                    } else {
                                        //удаляем конкретное расхождение
                                        val selectedTypeDiscrepancies = countProcessed.value?.get(position)?.productDiscrepancies?.typeDiscrepancies.orEmpty()
                                        if (selectedProduct.isSet) {
                                            repoInMemoryHolder
                                                    .sets
                                                    .value
                                                    ?.filter {
                                                        it.setNumber == selectedProduct.materialNumber
                                                    }
                                                    ?.map { component ->
                                                        deleteDiscrepanciesForSet(component.componentNumber, selectedTypeDiscrepancies)
                                                    }
                                        }
                                        deleteDiscrepanciesForProduct(selectedProduct.materialNumber, selectedTypeDiscrepancies)
                                    }
                                }
                            }
                }

        updateData()
    }

    private fun deleteDiscrepanciesForSet(componentNumber: String, typeDiscrepancies: String) {
        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.let { taskRepository ->
                    taskRepository
                            .getProductsDiscrepancies()
                            .deleteProductDiscrepancy(componentNumber, typeDiscrepancies)

                    taskRepository
                            .getBatchesDiscrepancies()
                            .deleteBatchesDiscrepanciesForProductAndDiscrepancies(componentNumber, typeDiscrepancies)
                }
    }

    private fun deleteDiscrepanciesNotNormForSet(componentNumber: String) {
        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.let { taskRepository ->
                    taskRepository
                            .getProductsDiscrepancies()
                            .deleteProductsDiscrepanciesNotNormForProduct(componentNumber)

                    taskRepository
                            .getBatchesDiscrepancies()
                            .deleteBatchesDiscrepanciesNotNormForProduct(componentNumber)
                }
    }

    private fun deleteDiscrepanciesForProduct(materialNumber: String, typeDiscrepancies: String) {
        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.let { taskRepository ->
                    taskRepository
                            .getProductsDiscrepancies()
                            .deleteProductDiscrepancy(materialNumber, typeDiscrepancies)

                    taskRepository
                            .getMercuryDiscrepancies()
                            .deleteMercuryDiscrepancyOfProduct(materialNumber, typeDiscrepancies)

                    taskRepository
                            .getBatchesDiscrepancies()
                            .deleteBatchesDiscrepanciesForProductAndDiscrepancies(materialNumber, typeDiscrepancies)

                    taskRepository
                            .getBoxesDiscrepancies()
                            .deleteBoxesDiscrepanciesForProductAndDiscrepancies(materialNumber, typeDiscrepancies)

                    taskRepository
                            .getExciseStampsDiscrepancies()
                            .deleteExciseStampsDiscrepanciesForProductAndDiscrepancies(materialNumber, typeDiscrepancies)

                    taskRepository
                            .getExciseStampsBad()
                            .deleteExciseStampBadForProductAndDiscrepancies(materialNumber, typeDiscrepancies)

                    taskRepository
                            .getBlocksDiscrepancies()
                            .deleteBlocksDiscrepanciesForProductAndDiscrepancies(materialNumber, typeDiscrepancies)

                    taskRepository
                            .getZBatchesDiscrepancies()
                            .deleteZBatchesDiscrepanciesForProductAndDiscrepancies(materialNumber, typeDiscrepancies)
                }
    }

    private fun deleteDiscrepanciesNotNormForProduct(product: TaskProductInfo) {
        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.let { taskRepository ->
                    taskRepository
                            .getProductsDiscrepancies()
                            .deleteProductsDiscrepanciesNotNormForProduct(product)

                    taskRepository
                            .getMercuryDiscrepancies()
                            .deleteMercuryDiscrepanciesNotNormForProduct(product)

                    taskRepository
                            .getBatchesDiscrepancies()
                            .deleteBatchesDiscrepanciesNotNormForProduct(product.materialNumber)

                    taskRepository
                            .getBoxesDiscrepancies()
                            .deleteBoxesDiscrepanciesNotNormForProduct(product.materialNumber)

                    taskRepository
                            .getExciseStampsDiscrepancies()
                            .deleteExciseStampsDiscrepanciesNotNormForProduct(product.materialNumber)

                    taskRepository
                            .getExciseStampsBad()
                            .deleteExciseStampBadNotNormForProduct(product.materialNumber)

                    taskRepository
                            .getBlocksDiscrepancies()
                            .deleteBlocksDiscrepanciesNotNormForProduct(product.materialNumber)

                    taskRepository
                            .getZBatchesDiscrepancies()
                            .deleteZBatchesDiscrepanciesNotNormForProduct(product.materialNumber)
                }
    }

    fun onClickBatches() {
        isBatches.value = !isBatches.value!!
        updateData()
    }

    fun onClickSave() {
        saveProductDelegate.saveDataInERP()
    }

    private fun normEnteredButControlNotPassed(productInfo: TaskProductInfo): Boolean {
        val countAcceptOfProduct =
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getProductsDiscrepancies()
                        ?.getCountAcceptOfProduct(productInfo)
                        ?: 0.0

        val isControlBoxesOfProduct =
                taskManager.getReceivingTask()
                        ?.controlBoxesOfProduct(productInfo)
                        ?: false

        val isControlExciseStampsOfProduct =
                taskManager.getReceivingTask()
                        ?.controlExciseStampsOfProduct(productInfo)
                        ?: false

        return !(countAcceptOfProduct <= 0 || (isControlExciseStampsOfProduct && isControlBoxesOfProduct))
    }

    private fun markingProductControlNotPassed(productInfo: TaskProductInfo): Boolean {
        val processMarkingProduct =
                when(getMarkingGoodsRegime(taskManager, productInfo)) {
                    MarkingGoodsRegime.UomStWithoutBoxes -> processMarkingProductService.newProcessMarkingProductService(productInfo)
                    MarkingGoodsRegime.UomStWithBoxes -> processMarkingBoxProductService.newProcessMarkingProductService(productInfo)
                    else -> null
                }

        return processMarkingProduct
                ?.let { processMarking ->
                    val isProcessedProduct =
                            taskManager
                                    .getReceivingTask()
                                    ?.run {
                                        taskRepository
                                                .getProductsDiscrepancies()
                                                .findProductDiscrepanciesOfProduct(productInfo)
                                                .any { it.typeDiscrepancies == TYPE_DISCREPANCIES_QUALITY_NORM }
                                    }
                                    ?: false
                    val countStampsScanned =
                            taskManager
                                    .getReceivingTask()
                                    ?.run {
                                        taskRepository
                                                .getBlocksDiscrepancies()
                                                .processedNumberOfStampsByProduct(productInfo)
                                                .toDouble()
                                    }
                                    ?: 0.0
                    val countBlocksUnderload = processMarking.getCountBlocksUnderload(paramGrzGrundMarkCode.value.orEmpty())
                    val numberStampsControl =  productInfo.numberStampsControl.toDouble() - countBlocksUnderload
                    isProcessedProduct && countStampsScanned < numberStampsControl
                }
                ?: false
    }

    private fun getManufacturerName(batchInfo: TaskBatchInfo?): String {
        return repoInMemoryHolder.manufacturers.value
                ?.findLast { manufacture ->
                    manufacture.code == batchInfo?.egais
                }
                ?.name.orEmpty()
    }

    private fun getRefusalTotalCountWithUomBatch(batchInfo: TaskBatchInfo?, uom: Uom?): String {
        val refusalTotalCountBatch = batchInfo?.let {
            if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getBatchesDiscrepancies()
                        ?.getCountRefusalOfBatchPGE(batchInfo)
            } else {
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getBatchesDiscrepancies()
                        ?.getCountRefusalOfBatch(batchInfo)
            }
        }
        return "- ${refusalTotalCountBatch.toStringFormatted()} ${uom?.name.orEmpty()}"
    }

    private fun moveToProcessedPageIfNeeded() {
        selectedPage.value =
                if (countNotProcessed.value?.size == 0) {
                    if (isAlco.value == true || isMark.value == true) PAGE_PROCESSED_WITH_CONTROL else PAGE_PROCESSED_WITHOUT_CONTROL
                } else {
                    0
                }
    }

}
