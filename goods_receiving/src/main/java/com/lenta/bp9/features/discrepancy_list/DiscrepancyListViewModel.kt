package com.lenta.bp9.features.discrepancy_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.TypeDiscrepanciesConstants
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.EndRecountDDParameters
import com.lenta.bp9.requests.network.EndRecountDDResult
import com.lenta.bp9.requests.network.EndRecountDirectDeliveriesNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import javax.inject.Inject

private const val SELECTED_PAGE_NOT_PROCESSED = 0
private const val SELECTED_PAGE_CONTROL = 1
private const val PAGE_PROCESSED_WITHOUT_CONTROL = 1
private const val PAGE_PROCESSED_WITH_CONTROL = 2

class DiscrepancyListViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: IReceivingTaskManager

    @Inject
    lateinit var endRecountDirectDeliveries: EndRecountDirectDeliveriesNetRequest

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var searchProductDelegate: SearchProductDelegate

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @Inject
    lateinit var dataBase: IDataBaseRepo

    val selectedPage = MutableLiveData(0)
    val processedSelectionsHelper = SelectionItemsHelper()
    val countNotProcessed: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()
    val countProcessed: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()
    val countControl: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()
    private val isBatches: MutableLiveData<Boolean> = MutableLiveData(false)
    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()

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
        MutableLiveData(taskManager.getReceivingTask()?.taskDescription?.isAlco == true && !(taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ShipmentPP || taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ShipmentRC)) //для заданий ОПП и ОРЦ не показываем кнопку Партия, уточнил у Артема
    }

    private val paramGrzGrundMarkCode: MutableLiveData<String> = MutableLiveData("")

    init {
        launchUITryCatch {
            searchProductDelegate.init(viewModelScope = this@DiscrepancyListViewModel::viewModelScope,
                    scanResultHandler = this@DiscrepancyListViewModel::handleProductSearchResult)

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
        taskManager.getReceivingTask()?.let { task ->
            task.getProcessedProducts()
                    .filter {
                        if (task.taskHeader.taskType == TaskType.RecalculationCargoUnit) {
                            task.taskRepository.getProductsDiscrepancies().getCountProductNotProcessedOfProductPGE(it) > 0.0
                        } else {
                            task.taskRepository.getProductsDiscrepancies().getCountProductNotProcessedOfProduct(it) > 0.0
                        }
                    }.sortedByDescending { sorted ->
                        sorted.materialNumber
                    }
                    .map { productInfo ->
                        val uom = if (task.taskHeader.taskType == TaskType.DirectSupplier) {
                            productInfo.purchaseOrderUnits
                        } else {
                            productInfo.uom
                        }
                        if (isBatches.value == true && productInfo.type == ProductType.NonExciseAlcohol && !productInfo.isBoxFl && !productInfo.isMarkFl) {
                            val batchesInfoOfProduct = task.taskRepository.getBatches().findBatchOfProduct(productInfo)
                            batchesInfoOfProduct?.map { batch ->
                                val batchInfo = task.taskRepository.getBatches().findBatch(
                                        batchNumber = batch.batchNumber,
                                        materialNumber = batch.materialNumber,
                                        processingUnitNumber = batch.processingUnitNumber
                                )
                                val quantityNotProcessedProductBatch = task.taskRepository.getBatchesDiscrepancies().getCountBatchNotProcessedOfBatch(batch).toStringFormatted()
                                arrayNotCounted.add(
                                        GoodsDiscrepancyItem(
                                                number = index + 1,
                                                name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                                nameMaxLines = 1,
                                                nameBatch = "ДР-${batchInfo?.bottlingDate} // ${getManufacturerName(batchInfo)}",
                                                visibilityNameBatch = true,
                                                countRefusalWithUom = "",
                                                quantityNotProcessedWithUom = "? $quantityNotProcessedProductBatch ${uom.name}",
                                                discrepanciesName = "",
                                                productInfo = productInfo,
                                                productDiscrepancies = null,
                                                batchInfo = null,
                                                visibilityCheckBoxControl = true,
                                                checkBoxControl = false,
                                                checkStampControl = false,
                                                even = index % 2 == 0
                                        )
                                )
                                index += 1
                            }
                        } else {
                            val quantityNotProcessedProduct = if (task.taskHeader.taskType == TaskType.RecalculationCargoUnit) {
                                val processingUnitsOfProduct = task.taskRepository.getProducts().getProcessingUnitsOfProduct(productInfo.materialNumber)
                                if (processingUnitsOfProduct.size > 1) { //если у товара две ЕО
                                    val countOrderQuantity = processingUnitsOfProduct.map { unitInfo ->
                                        unitInfo.orderQuantity.toDouble()
                                    }.sumByDouble {
                                        it
                                    }
                                    task.taskRepository.getProductsDiscrepancies().getCountProductNotProcessedOfProductPGEOfProcessingUnits(productInfo, countOrderQuantity).toStringFormatted()
                                } else {
                                    task.taskRepository.getProductsDiscrepancies().getCountProductNotProcessedOfProductPGE(productInfo).toStringFormatted()
                                }
                            } else {
                                task.taskRepository.getProductsDiscrepancies().getCountProductNotProcessedOfProduct(productInfo).toStringFormatted()

                            }
                            arrayNotCounted.add(
                                    GoodsDiscrepancyItem(
                                            number = index + 1,
                                            name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                            nameMaxLines = 2,
                                            nameBatch = "",
                                            visibilityNameBatch = false,
                                            countRefusalWithUom = "",
                                            quantityNotProcessedWithUom = "? $quantityNotProcessedProduct ${uom.name}",
                                            discrepanciesName = "",
                                            productInfo = productInfo,
                                            productDiscrepancies = null,
                                            batchInfo = null,
                                            visibilityCheckBoxControl = true,
                                            checkBoxControl = false,
                                            checkStampControl = false,
                                            even = index % 2 == 0
                                    )
                            )
                            index += 1
                        }
                    }
        }

        countNotProcessed.postValue(
                arrayNotCounted.reversed()
        )

        launchUITryCatch {
            moveToProcessedPageIfNeeded()
        }
    }

    private fun updateCountProcessed() {
        val arrayCounted: ArrayList<GoodsDiscrepancyItem> = ArrayList()
        var index = 0
        var addBatchProduct = ""
        taskManager.getReceivingTask()?.let { task ->
            task.taskRepository.getProductsDiscrepancies().getProductsDiscrepancies()
                    .filter {
                        val isComponent = repoInMemoryHolder.sets.value?.any { set ->
                            set.componentNumber == it.materialNumber
                        }
                        if (repoInMemoryHolder.taskList.value?.taskListLoadingMode == TaskListLoadingMode.PGE) {
                            !(it.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM
                                    || it.typeDiscrepancies == TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_SURPLUS)
                                    && isComponent == false
                        } else {
                            it.typeDiscrepancies != TypeDiscrepanciesConstants.TYPE_DISCREPANCIES_QUALITY_NORM && isComponent == false
                        }
                    }
                    .sortedByDescending {
                        it.materialNumber
                    }
                    .map { productDiscrepancies ->
                        val productInfo = task.taskRepository.getProducts().findProduct(productDiscrepancies.materialNumber)
                        val uom = if (task.taskHeader.taskType == TaskType.DirectSupplier) {
                            productInfo?.purchaseOrderUnits
                        } else {
                            productInfo?.uom
                        }
                        val discrepanciesName = qualityInfo.value?.findLast {
                            it.code == productDiscrepancies.typeDiscrepancies
                        }?.name
                        if (isBatches.value == true && productInfo?.type == ProductType.NonExciseAlcohol && !productInfo.isBoxFl && !productInfo.isMarkFl) {
                            if (addBatchProduct != productInfo.materialNumber) { //показываем партии без разбивки по расхождениям
                                addBatchProduct = productInfo.materialNumber
                                val batchesInfoOfProduct = task.taskRepository.getBatches().findBatchOfProduct(productInfo)?.filter { findBatch ->
                                    if (repoInMemoryHolder.taskList.value?.taskListLoadingMode == TaskListLoadingMode.PGE) {
                                        task.taskRepository.getBatchesDiscrepancies().findBatchDiscrepanciesOfBatch(findBatch).filter { findBatchDiscrPGE ->
                                            !(findBatchDiscrPGE.typeDiscrepancies == "1" || findBatchDiscrPGE.typeDiscrepancies == "2")
                                        }.any()
                                    } else {
                                        task.taskRepository.getBatchesDiscrepancies().findBatchDiscrepanciesOfBatch(findBatch).filter { findBatchDiscr ->
                                            findBatchDiscr.typeDiscrepancies != "1"
                                        }.any()
                                    }
                                }
                                batchesInfoOfProduct?.map { batch ->
                                    val batchInfo = task.taskRepository.getBatches().findBatch(
                                            batchNumber = batch.batchNumber,
                                            materialNumber = batch.materialNumber,
                                            processingUnitNumber = batch.processingUnitNumber
                                    )
                                    arrayCounted.add(
                                            GoodsDiscrepancyItem(
                                                    number = index + 1,
                                                    name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                                    nameMaxLines = 1,
                                                    nameBatch = "",
                                                    visibilityNameBatch = true,
                                                    countRefusalWithUom = getRefusalTotalCountWithUomBatch(batchInfo, uom),
                                                    quantityNotProcessedWithUom = "",
                                                    discrepanciesName = "ДР-${batchInfo?.bottlingDate} // ${getManufacturerName(batchInfo)}",
                                                    productInfo = productInfo,
                                                    productDiscrepancies = productDiscrepancies,
                                                    batchInfo = null,
                                                    visibilityCheckBoxControl = true,
                                                    checkBoxControl = false,
                                                    checkStampControl = false,
                                                    even = index % 2 == 0
                                            )
                                    )
                                    index += 1
                                }
                            }
                        } else {
                            arrayCounted.add(
                                    GoodsDiscrepancyItem(
                                            number = index + 1,
                                            name = "${productInfo?.getMaterialLastSix()} ${productInfo?.description}",
                                            nameMaxLines = 2,
                                            nameBatch = "",
                                            visibilityNameBatch = false,
                                            countRefusalWithUom = "- ${productDiscrepancies.numberDiscrepancies.toDouble().toStringFormatted()} ${uom?.name}",
                                            quantityNotProcessedWithUom = "",
                                            discrepanciesName = discrepanciesName ?: "",
                                            productInfo = productInfo,
                                            productDiscrepancies = productDiscrepancies,
                                            batchInfo = null,
                                            visibilityCheckBoxControl = true,
                                            checkBoxControl = false,
                                            checkStampControl = false,
                                            even = index % 2 == 0
                                    )
                            )
                            index += 1
                        }
                    }
        }

        countProcessed.postValue(
                arrayCounted.reversed()
        )

        processedSelectionsHelper.clearPositions()
    }

    private fun updateCountControl() {
        taskManager.getReceivingTask()?.let { task ->
            //https://trello.com/c/9K1FZnUU, отображать перечень товаров (марочного алкоголя), по которым не пройден хотя бы один из видов контроля
            //https://trello.com/c/vcymT9Kp отображать перечень товаров (маркированный товар, сигареты, обувь), по которым не пройден хотя бы один из видов контроля (пока сделано только для одного режима)
            countControl.postValue(
                    task.getProcessedProducts()
                            .filter { goodsInfo ->
                                if (goodsInfo.markType != MarkType.None) { //маркированный товар (сигареты, обувь)
                                    markingProductControlNotPassed(goodsInfo)
                                } else { //марочный алкоголь
                                    normEnteredButControlNotPassed(goodsInfo)
                                }
                            }
                            .sortedByDescending {
                                it.materialNumber
                            }
                            .mapIndexed { index, productInfo ->
                                val isControlBoxesOfProduct = if (productInfo.markType == MarkType.None) {
                                    taskManager.getReceivingTask()?.controlBoxesOfProduct(productInfo) ?: false
                                } else {
                                    false
                                }

                                val isControlExciseStampsOfProduct = if (productInfo.markType == MarkType.None) {
                                    taskManager.getReceivingTask()?.controlExciseStampsOfProduct(productInfo) ?: false
                                } else {
                                    false
                                }
                                GoodsDiscrepancyItem(
                                        number = index + 1,
                                        name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                        nameMaxLines = 2,
                                        nameBatch = "",
                                        visibilityNameBatch = false,
                                        countRefusalWithUom = "",
                                        quantityNotProcessedWithUom = "",
                                        discrepanciesName = "",
                                        productInfo = productInfo,
                                        productDiscrepancies = null,
                                        batchInfo = null,
                                        visibilityCheckBoxControl = productInfo.markType == MarkType.None, //скрываем для маркированного товара, это пока реализовано для режима TASK_MARK не пустая, BSTME=ST и признак IS_USE_ALTERN_MEINS не установлен (режимы https://trello.com/c/NGsFfWgB), остальные режимы еще будут дорабатываться
                                        checkBoxControl = isControlBoxesOfProduct,
                                        checkStampControl = isControlExciseStampsOfProduct,
                                        even = index % 2 == 0)
                            }
                            .reversed())
        }
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption.orEmpty()
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickItemPosition(position: Int) {
        val selectedNotProcessedProduct = countNotProcessed.value?.get(position)?.productInfo
        val selectedMaterialNumber: String? =
                if (selectedPage.value == SELECTED_PAGE_NOT_PROCESSED) {
                    selectedNotProcessedProduct?.materialNumber
                } else {
                    if (((isAlco.value == true || isMark.value == true) && selectedPage.value == PAGE_PROCESSED_WITH_CONTROL)
                            || ((isAlco.value == false && isMark.value == false) && selectedPage.value == PAGE_PROCESSED_WITHOUT_CONTROL)) {
                        countProcessed.value
                                ?.get(position)
                                ?.productInfo
                                ?.materialNumber
                    } else {
                        countControl.value
                                ?.get(position)
                                ?.productInfo
                                ?.materialNumber
                    }
                }

        val mode = repoInMemoryHolder.taskList.value?.taskListLoadingMode

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
                code = selectedMaterialNumber.orEmpty(),
                fromScan = false,
                isDiscrepancy = true
        )
    }

    fun onClickClean() {
        processedSelectionsHelper
                .selectedPositions
                .value
                ?.map { position ->
                    countProcessed
                            .value
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
                }
    }

    fun onClickBatches() {
        isBatches.value = !isBatches.value!!
        updateData()
    }

    fun onClickSave() {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            //очищаем таблицу ET_TASK_DIFF от не акцизного (партионного) алкоголя, т.к. для этих товаров необходимо передавать только данные из таблицы ET_PARTS_DIFF
            taskManager.getReceivingTask()
                    ?.taskRepository
                    ?.getProductsDiscrepancies()
                    ?.getProductsDiscrepancies()
                    ?.asSequence()
                    ?.map { productDiscr ->
                        taskManager.getReceivingTask()
                                ?.taskRepository
                                ?.getProducts()
                                ?.findProduct(productDiscr.materialNumber)
                    }?.filter { filterProduct ->
                        //партионный - это помеченный IS_ALCO и не помеченный IS_BOX_FL, IS_MARK_FL (Артем)
                        filterProduct?.type == ProductType.NonExciseAlcohol
                                && !filterProduct.isBoxFl
                                && !filterProduct.isMarkFl
                    }?.map { mapProduct ->
                        mapProduct?.let { productForDel ->
                            taskManager.getReceivingTask()
                                    ?.taskRepository
                                    ?.getProductsDiscrepancies()
                                    ?.deleteProductsDiscrepanciesForProduct(productForDel.materialNumber)
                        }
                    }

            endRecountDirectDeliveries(EndRecountDDParameters(
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber.orEmpty(),
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber.orEmpty(),
                    discrepanciesProduct = taskManager.getReceivingTask()
                            ?.taskRepository
                            ?.getProductsDiscrepancies()
                            ?.getProductsDiscrepancies()
                            ?.map {
                                TaskProductDiscrepanciesRestData.from(it)
                            }
                            ?: emptyList(),
                    discrepanciesBatches = taskManager.getReceivingTask()
                            ?.taskRepository
                            ?.getBatchesDiscrepancies()
                            ?.getBatchesDiscrepancies()
                            ?.map {
                                TaskBatchesDiscrepanciesRestData.from(it)
                            }
                            ?: emptyList(),
                    discrepanciesBoxes = taskManager.getReceivingTask()
                            ?.taskRepository
                            ?.getBoxesDiscrepancies()
                            ?.getBoxesDiscrepancies()
                            ?.map {
                                TaskBoxDiscrepanciesRestData.from(it)
                            }
                            ?: emptyList(),
                    discrepanciesExciseStamp = taskManager.getReceivingTask()
                            ?.taskRepository
                            ?.getExciseStampsDiscrepancies()
                            ?.getExciseStampDiscrepancies()
                            ?.map {
                                TaskExciseStampDiscrepanciesRestData.from(it)
                            }
                            ?: emptyList(),
                    exciseStampBad = taskManager.getReceivingTask()
                            ?.taskRepository
                            ?.getExciseStampsBad()
                            ?.getExciseStampsBad()
                            ?.map {
                                TaskExciseStampBadRestData.from(it)
                            }
                            ?: emptyList(),
                    discrepanciesMercury = taskManager.getReceivingTask()
                            ?.taskRepository
                            ?.getMercuryDiscrepancies()
                            ?.getMercuryDiscrepancies()
                            ?.map {
                                TaskMercuryDiscrepanciesRestData.from(it)
                            }
                            ?: emptyList(),
                    discrepanciesBlocks = taskManager.getReceivingTask()
                            ?.taskRepository
                            ?.getBlocksDiscrepancies()
                            ?.getBlocksDiscrepancies()
                            ?.map {
                                TaskBlockDiscrepanciesRestData.from(it)
                            }
                            ?: emptyList()
            )).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(result: EndRecountDDResult) {
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType
                ?: TaskType.None)
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure, pageNumber = "97")
    }

    private fun normEnteredButControlNotPassed(productInfo: TaskProductInfo): Boolean {
        /** Артем:
        товар на вкладке Контроль появляется если по товару была введеная норма, но не был пройден контроль
        Причем если по товару
        10 шт
        контроль 2 шт,
        а по факту мы ввели 1 норму (и подтвердили сканированием) и 9 брака то контроль считается пройден*/
        val countAcceptOfProduct =
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getProductsDiscrepancies()
                        ?.getCountAcceptOfProduct(productInfo)
                        ?: 0.0
        val countRefusalOfProduct =
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getProductsDiscrepancies()
                        ?.getCountRefusalOfProduct(productInfo)
                        ?: 0.0
        val isControlBoxesOfProduct =
                taskManager.getReceivingTask()
                        ?.controlBoxesOfProduct(productInfo)
                        ?: false
        val isControlExciseStampsOfProduct =
                taskManager.getReceivingTask()
                        ?.controlExciseStampsOfProduct(productInfo)
                        ?: false
        val countBoxesPassedControlOfProduct =
                taskManager.getReceivingTask()
                        ?.countBoxesPassedControlOfProduct(productInfo)
                        ?: 0

        return if (countAcceptOfProduct <= 0 || (isControlExciseStampsOfProduct && isControlBoxesOfProduct)) {
            false
        } else {
            (countBoxesPassedControlOfProduct + countRefusalOfProduct) < productInfo.origQuantity.toDouble()
        }
    }

    private fun markingProductControlNotPassed(productInfo: TaskProductInfo): Boolean {
        val isProcessedProduct =
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getProductsDiscrepancies()
                        ?.findProductDiscrepanciesOfProduct(productInfo)
                        ?.any {
                            it.typeDiscrepancies != paramGrzGrundMarkCode.value
                        }
                        ?: false
        val countStampsScanned =
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getBlocksDiscrepancies()
                        ?.processedNumberOfStampsByProduct(productInfo)
                        ?.toDouble()
                        ?: 0.0
        val numberStampsControl = productInfo.numberStampsControl.toDouble()
        return isProcessedProduct && countStampsScanned != numberStampsControl
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
                    if (isAlco.value == true || isMark.value == true) 2 else 1
                } else {
                    0
                }
    }

}
