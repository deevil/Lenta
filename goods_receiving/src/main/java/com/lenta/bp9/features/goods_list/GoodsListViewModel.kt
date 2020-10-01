package com.lenta.bp9.features.goods_list

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.delegates.ISaveProductDelegate
import com.lenta.bp9.features.delegates.SearchProductDelegate
import com.lenta.bp9.features.goods_list.GoodsListViewPages.GOODS_LIST_VIEW_PAGE_COUNTED_OR_TO_PROCESSING
import com.lenta.bp9.features.goods_list.GoodsListViewPages.GOODS_LIST_VIEW_PAGE_WITHOUT_BARCODE_OR_PROCESSED
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.EndRecountDirectDeliveriesNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.utilities.orIfNull
import java.lang.Exception
import java.text.SimpleDateFormat
import javax.inject.Inject

class GoodsListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

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
    lateinit var saveProductDelegate: ISaveProductDelegate

    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder

    @SuppressLint("SimpleDateFormat")
    private val formatterRU = SimpleDateFormat("dd.MM.yyyy")

    @SuppressLint("SimpleDateFormat")
    private val formatterERP = SimpleDateFormat(Constants.DATE_FORMAT_yyyyMMdd)

    val countedSelectionsHelper = SelectionItemsHelper()
    val toProcessingSelectionsHelper = SelectionItemsHelper()
    val processedSelectionsHelper = SelectionItemsHelper()
    val listCounted: MutableLiveData<List<ListCountedItem>> = MutableLiveData()
    val listWithoutBarcode: MutableLiveData<List<ListWithoutBarcodeItem>> = MutableLiveData()
    val listToProcessing: MutableLiveData<List<ListShipmentPPItem>> = MutableLiveData()
    val listProcessed: MutableLiveData<List<ListShipmentPPItem>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val eanCodeCountedOrToProcessing: MutableLiveData<String> = MutableLiveData()
    val eanCodeWithoutBarcodeOrProcessed: MutableLiveData<String> = MutableLiveData()
    val requestFocusCountedOrToProcessing: MutableLiveData<Boolean> = MutableLiveData()
    val requestFocusWithoutBarcodeOrProcessed: MutableLiveData<Boolean> = MutableLiveData()
    val taskType: MutableLiveData<TaskType> = MutableLiveData()

    private val isBatches: MutableLiveData<Boolean> = MutableLiveData(false)
    private val taskRepository by lazy { taskManager.getReceivingTask()?.taskRepository }

    val visibilityCleanButton: MutableLiveData<Boolean> = selectedPage.map {
        it == 0
    }

    val enabledCleanButton: MutableLiveData<Boolean> = countedSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty()
    }

    val visibilityBatchesButton: MutableLiveData<Boolean> by lazy {
        val taskDescription = taskManager.getReceivingTask()?.taskDescription
        MutableLiveData((taskDescription?.isAlco == true || taskDescription?.isZBatches == true)
                && !(taskType.value == TaskType.ShipmentPP || taskType.value == TaskType.ShipmentRC)) //для заданий ОПП и ОРЦ не показываем кнопку Партия, уточнил у Артема
    }

    val enabledBtnSaveForShipmentPP: MutableLiveData<Boolean> = listToProcessing.map {
        it?.isEmpty()
    }

    val enabledBtnMissingForShipmentPP = toProcessingSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty()
    }

    val enabledBtnCleanForShipmentPP = processedSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty()
    }

    val enabledBtnSkipForShipmentPP: MutableLiveData<Boolean> by lazy {
        MutableLiveData(taskManager.getReceivingTask()?.taskDescription?.isSkipCountMan == true)
    }

    val isTaskPGE: MutableLiveData<Boolean> by lazy {
        MutableLiveData(taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit)
    }

    init {
        launchUITryCatch {
            searchProductDelegate.init(scanResultHandler = this@GoodsListViewModel::handleProductSearchResult)
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        eanCode.postValue("")
        return false
    }

    fun onResume() {
        updateData()
        if (selectedPage.value == GoodsListViewPages.GOODS_LIST_VIEW_PAGE_COUNTED_OR_TO_PROCESSING) {
            requestFocusCountedOrToProcessing.value = true
        } else {
            requestFocusWithoutBarcodeOrProcessed.value = true
        }
    }

    private fun updateData() {
        if (taskType.value == TaskType.ShipmentPP) {
            updateListToProcessing()
            updateListProcessed()
        } else {
            updateListCounted()
            updateListWithoutBarcode()
        }
    }

    private fun updateListCounted() {
        val arrayCounted: ArrayList<ListCountedItem> = ArrayList()
        var index = 0
        var addBatchProduct = ""
        taskManager.getReceivingTask()?.let { task ->
            task.getProcessedProducts()
                    .filter {
                        if (task.taskHeader.taskType == TaskType.RecalculationCargoUnit) {
                            (task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(it) +
                                    task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(it)) > 0.0
                        } else {
                            (task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(it) +
                                    task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(it)) > 0.0
                        }
                    }
                    .sortedByDescending { sorted ->
                        sorted.materialNumber
                    }
                    .map { productInfo ->
                        val uom = if (task.taskHeader.taskType == TaskType.DirectSupplier) {
                            productInfo.purchaseOrderUnits
                        } else {
                            productInfo.uom
                        }

                        if (isBatches.value == true && productInfo.type == ProductType.NonExciseAlcohol && !productInfo.isBoxFl && !productInfo.isMarkFl) {
                            if (addBatchProduct != productInfo.materialNumber) { //показываем партии без разбивки по расхождениям
                                addBatchProduct = productInfo.materialNumber
                                val batchesInfoOfProduct = task.taskRepository.getBatches().findBatchOfProduct(productInfo)
                                batchesInfoOfProduct?.map { batch ->
                                    val batchInfo = task.taskRepository.getBatches().findBatch(
                                            batchNumber = batch.batchNumber,
                                            materialNumber = batch.materialNumber,
                                            processingUnitNumber = batch.processingUnitNumber
                                    )
                                    arrayCounted.add(
                                            ListCountedItem(
                                                    number = index + 1,
                                                    name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                                    nameMaxLines = 1,
                                                    nameBatch = "ДР-${batchInfo?.bottlingDate} // ${getManufacturerName(batchInfo)}",
                                                    visibilityNameBatch = true,
                                                    countAcceptWithUom = getAcceptTotalCountWithUomBatch(batchInfo, uom),
                                                    countRefusalWithUom = getRefusalTotalCountWithUomBatch(batchInfo, uom),
                                                    isNotEdit = productInfo.isNotEdit,
                                                    productInfo = productInfo,
                                                    zBatchDiscrepancies = null,
                                                    even = index % 2 == 0
                                            )
                                    )
                                    index += 1
                                }
                            }
                        } else if (isBatches.value == true && productInfo.isZBatches && !productInfo.isVet) { //см. SearchProductDelegate
                            if (addBatchProduct != productInfo.materialNumber) { //показываем Z-партии без разбивки по расхождениям
                                addBatchProduct = productInfo.materialNumber
                                val zBatchesInfoOfProduct = task.taskRepository.getZBatchesDiscrepancies().findZBatchDiscrepanciesOfProduct(productInfo.materialNumber)
                                zBatchesInfoOfProduct.map { zBatch ->
                                    val shelfLifeOrProductionDate = getShelfLifeOrProductionDate(zBatch)
                                    val partySign =
                                            task.taskRepository
                                                    .getZBatchesDiscrepancies()
                                                    .findPartySignOfZBatch(zBatch)
                                                    ?.partySign
                                                    ?.partySignsTypeString
                                                    .orEmpty()

                                    arrayCounted.add(
                                            ListCountedItem(
                                                    number = index + 1,
                                                    name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                                    nameMaxLines = 1,
                                                    nameBatch = "$partySign-$shelfLifeOrProductionDate // ${getManufacturerNameZBatch(zBatch.manufactureCode)}",
                                                    visibilityNameBatch = true,
                                                    countAcceptWithUom = getAcceptTotalCountWithUomZBatch(zBatch, uom),
                                                    countRefusalWithUom = "",
                                                    isNotEdit = productInfo.isNotEdit,
                                                    productInfo = productInfo,
                                                    zBatchDiscrepancies = zBatch,
                                                    even = index % 2 == 0
                                            )
                                    )
                                    index += 1
                                }
                            }
                        } else {
                            arrayCounted.add(
                                    ListCountedItem(
                                            number = index + 1,
                                            name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                            nameMaxLines = 2,
                                            nameBatch = "",
                                            visibilityNameBatch = false,
                                            countAcceptWithUom = getAcceptTotalCountWithUomProduct(productInfo, uom),
                                            countRefusalWithUom = getRefusalTotalCountWithUomProduct(productInfo, uom),
                                            isNotEdit = productInfo.isNotEdit,
                                            productInfo = productInfo,
                                            zBatchDiscrepancies = null,
                                            even = index % 2 == 0
                                    )
                            )
                            index += 1
                        }
                    }
        }

        listCounted.value = arrayCounted.reversed()
        countedSelectionsHelper.clearPositions()
    }

    private fun getShelfLifeOrProductionDate(zBatchesDiscrepancies: TaskZBatchesDiscrepancies): String {
        return try {
            val partySignOfZBatch =
                    taskRepository
                            ?.getZBatchesDiscrepancies()
                            ?.findPartySignOfZBatch(zBatchesDiscrepancies)

            when(partySignOfZBatch?.partySign ?: PartySignsTypeOfZBatches.None) {
                PartySignsTypeOfZBatches.ProductionDate -> {
                    partySignOfZBatch
                            ?.productionDate
                            ?.takeIf { it.isNotEmpty() }
                            ?.let { formatterRU.format(formatterERP.parse(it)) }
                            .orEmpty()
                }
                PartySignsTypeOfZBatches.ShelfLife -> {
                    partySignOfZBatch
                            ?.shelfLifeDate
                            ?.takeIf { it.isNotEmpty() }
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

    private fun updateListWithoutBarcode() {
        val arrayWithoutBarcode: ArrayList<ListWithoutBarcodeItem> = ArrayList()
        var index = 0
        taskManager.getReceivingTask()?.let { task ->
            task.getProcessedProducts()
                    .filter {
                        if (task.taskHeader.taskType == TaskType.RecalculationCargoUnit) {
                            it.isNoEAN && (task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProductPGE(it) +
                                    task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProductPGE(it) == 0.0)
                        } else {
                            it.isNoEAN && (task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(it) +
                                    task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(it) == 0.0)
                        }
                    }
                    .sortedByDescending { sorted ->
                        sorted.materialNumber
                    }
                    .map { productInfo ->
                        if (isBatches.value == true && productInfo.type == ProductType.NonExciseAlcohol && !productInfo.isBoxFl && !productInfo.isMarkFl) {
                            val batchesDiscrepanciesOfProduct = task.taskRepository.getBatchesDiscrepancies().findBatchDiscrepanciesOfProduct(productInfo.materialNumber)
                            batchesDiscrepanciesOfProduct.map { batchDiscrepancies ->
                                val batchInfo = task.taskRepository.getBatches().findBatch(
                                        batchNumber = batchDiscrepancies.batchNumber,
                                        materialNumber = batchDiscrepancies.materialNumber,
                                        processingUnitNumber = batchDiscrepancies.processingUnitNumber
                                )
                                arrayWithoutBarcode.add(ListWithoutBarcodeItem(
                                        number = index + 1,
                                        name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                        nameMaxLines = 1,
                                        nameBatch = "ДР-${batchInfo?.bottlingDate} // ${getManufacturerName(batchInfo)}",
                                        visibilityNameBatch = true,
                                        productInfo = productInfo,
                                        even = index % 2 == 0
                                ))
                                index += 1
                            }
                        } else {
                            arrayWithoutBarcode.add(
                                    ListWithoutBarcodeItem(
                                            number = index + 1,
                                            name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                            nameMaxLines = 2,
                                            nameBatch = "",
                                            visibilityNameBatch = false,
                                            productInfo = productInfo,
                                            even = index % 2 == 0
                                    )
                            )
                            index += 1
                        }

                    }
        }

        listWithoutBarcode.postValue(
                arrayWithoutBarcode.reversed()
        )
    }

    private fun updateListToProcessing() {
        taskManager.getReceivingTask()?.let { task ->
            listToProcessing.postValue(
                    task.getProcessedProducts()
                            .filter {
                                task.taskRepository.getProductsDiscrepancies().getQuantityDiscrepanciesOfProduct(it) == 0
                            }.mapIndexed { index, productInfo ->
                                val isEizUnit = productInfo.purchaseOrderUnits.code != productInfo.uom.code
                                val uom = if (isEizUnit) {
                                    productInfo.purchaseOrderUnits
                                } else {
                                    productInfo.uom
                                }
                                ListShipmentPPItem(
                                        number = index + 1,
                                        name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                        countWithUom = "${productInfo.origQuantity.toDouble().toStringFormatted()} ${uom.name}",
                                        productInfo = productInfo,
                                        even = index % 2 == 0)
                            }
                            .reversed())
        }

        toProcessingSelectionsHelper.clearPositions()
    }

    private fun updateListProcessed() {
        taskManager.getReceivingTask()?.let { task ->
            val taskProducedProducts =  task.getProcessedProducts()
            val productsDiscrepancies = task.taskRepository.getProductsDiscrepancies()
            listProcessed.value =
                    taskProducedProducts
                            .filter {
                                val quantityOfProduct = productsDiscrepancies.getQuantityDiscrepanciesOfProduct(it)
                                quantityOfProduct > 0
                            }.mapIndexed { index, productInfo ->
                                val isEizUnit = productInfo.purchaseOrderUnits.code != productInfo.uom.code
                                val uom = if (isEizUnit) {
                                    productInfo.purchaseOrderUnits
                                } else {
                                    productInfo.uom
                                }
                                ListShipmentPPItem(
                                        number = index + 1,
                                        name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                        countWithUom = "${task.taskRepository.getProductsDiscrepancies().getAllCountDiscrepanciesOfProduct(productInfo).toStringFormatted()} ${uom.name}",
                                        productInfo = productInfo,
                                        even = index % 2 == 0)
                            }
                            .reversed()
        }.orIfNull {
            Logg.e { "updateListProcessed() get receiving task is null" }
        }

        processedSelectionsHelper.clearPositions()
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onScanResult(data: String) {
        searchProductDelegate.searchCode(code = data, fromScan = true, isBarCode = true)
    }

    fun onResult(code: Int?) {
        if (searchProductDelegate.handleResultCode(code)) {
            return
        }
    }

    fun onClickItemPosition(position: Int) {
        val materialNumber: String? = if (selectedPage.value == GoodsListViewPages.GOODS_LIST_VIEW_PAGE_COUNTED_OR_TO_PROCESSING) {
            if (taskType.value == TaskType.ShipmentPP) {
                listToProcessing.value
                        ?.get(position)
                        ?.productInfo
                        ?.materialNumber
            } else {
                listCounted.value
                        ?.get(position)
                        ?.productInfo
                        ?.materialNumber
            }
        } else {
            if (taskType.value == TaskType.ShipmentPP) {
                listProcessed.value
                        ?.get(position)
                        ?.productInfo
                        ?.materialNumber
            } else {
                listWithoutBarcode.value
                        ?.get(position)
                        ?.productInfo
                        ?.materialNumber
            }
        }
        searchProductDelegate.searchCode(code = materialNumber.orEmpty(), fromScan = false)
    }

    fun onClickRefusal() {
        screenNavigator.openRejectScreen()
    }

    fun onClickThirdBtn() {
        if (taskType.value == TaskType.ShipmentPP) {//https://trello.com/c/3WVovfmE
            screenNavigator.openSkipRecountScreen()
        } else {
            countedSelectionsHelper
                    .selectedPositions
                    .value
                    ?.map { position ->
                        listCounted
                                .value
                                ?.getOrNull(position)
                                ?.productInfo
                                ?.let { selectedProduct ->
                                    val isNotRecountCargoUnit = isTaskPGE.value == true && selectedProduct.isWithoutRecount
                                    if (isNotRecountCargoUnit) { //если это не пересчетная ГЕ //https://trello.com/c/PRTAVnUP только без признака ВЗЛОМ (обсудили с Колей 17.06.2020)
                                        taskManager
                                                .getReceivingTask()
                                                ?.taskRepository
                                                ?.getProductsDiscrepancies()
                                                ?.deleteProductsDiscrepanciesForProductNotRecountPGE(selectedProduct)
                                    }
                                    if (!selectedProduct.isNotEdit && !isNotRecountCargoUnit) {
                                        if (selectedProduct.isSet) {
                                            repoInMemoryHolder
                                                    .sets
                                                    .value
                                                    ?.filter { setInfo ->
                                                        setInfo.setNumber == selectedProduct.materialNumber
                                                    }
                                                    ?.map { component ->
                                                        deleteDiscrepanciesForSet(component.componentNumber)
                                                    }
                                        }
                                        if (isBatches.value == true && selectedProduct.isZBatches && !selectedProduct.isVet) {
                                            listCounted
                                                    .value
                                                    ?.getOrNull(position)
                                                    ?.zBatchDiscrepancies
                                                    ?.let { deleteDiscrepanciesForZBatch(it) }

                                        } else {
                                            deleteDiscrepanciesForProduct(selectedProduct)
                                        }
                                    }
                                }
                    }
            updateData()
        }
    }

    private fun deleteDiscrepanciesForSet(componentNumber: String) {
        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.let { taskRepository ->
                    taskRepository
                            .getProductsDiscrepancies()
                            .deleteProductsDiscrepanciesForProduct(componentNumber)

                    taskRepository
                            .getBatchesDiscrepancies()
                            .deleteBatchesDiscrepanciesForProduct(componentNumber)
                }
    }

    private fun deleteDiscrepanciesForProduct(product: TaskProductInfo) {
        taskManager
                .getReceivingTask()
                ?.taskRepository
                ?.let { taskRepository ->
                    taskRepository
                            .getProductsDiscrepancies()
                            .deleteProductsDiscrepanciesForProduct(product)

                    taskRepository
                            .getMercuryDiscrepancies()
                            .deleteMercuryDiscrepanciesForProduct(product)

                    taskRepository
                            .getBatchesDiscrepancies()
                            .deleteBatchesDiscrepanciesForProduct(product.materialNumber)

                    taskRepository
                            .getBoxesDiscrepancies()
                            .deleteBoxesDiscrepanciesForProduct(product)

                    taskRepository
                            .getExciseStampsDiscrepancies()
                            .deleteExciseStampsDiscrepanciesForProduct(product)

                    taskRepository
                            .getExciseStampsBad()
                            .deleteExciseStampBadForProduct(product.materialNumber)

                    taskRepository
                            .getBlocksDiscrepancies()
                            .deleteBlocksDiscrepanciesForProduct(product)

                    taskRepository
                            .getZBatchesDiscrepancies()
                            .deleteZBatchesDiscrepanciesForProduct(product.materialNumber)
                }
    }

    private fun deleteDiscrepanciesForZBatch(zBatchDiscrepancies: TaskZBatchesDiscrepancies) {
        taskRepository
                ?.getZBatchesDiscrepancies()
                ?.deleteZBatchDiscrepancies(zBatchDiscrepancies)

        taskRepository
                ?.getProductsDiscrepancies()
                ?.deleteProductDiscrepancyByBatch(
                        materialNumber = zBatchDiscrepancies.materialNumber,
                        typeDiscrepancies = zBatchDiscrepancies.typeDiscrepancies,
                        quantityByDiscrepancyForBatch = zBatchDiscrepancies.numberDiscrepancies.toDouble()
                )
    }

    fun onClickFourthBtn() {
        if (taskType.value == TaskType.ShipmentPP) {//https://trello.com/c/3WVovfmE
            if (selectedPage.value == GoodsListViewPages.GOODS_LIST_VIEW_PAGE_COUNTED_OR_TO_PROCESSING) {
                missingGoodsForShipmentPP()
            } else {
                cleanGoodsForShipmentPP()
            }
        } else {
            isBatches.value?.let {
                isBatches.value = !it
            }
            updateData()
        }
    }

    private fun missingGoodsForShipmentPP() {
        toProcessingSelectionsHelper.selectedPositions.value?.map { position ->
            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.changeProductDiscrepancy(TaskProductDiscrepancies(
                    materialNumber = listToProcessing.value?.get(position)!!.productInfo!!.materialNumber,
                    processingUnitNumber = listToProcessing.value?.get(position)!!.productInfo!!.processingUnit,
                    numberDiscrepancies = "0",
                    uom = listToProcessing.value?.get(position)!!.productInfo!!.uom,
                    typeDiscrepancies = "3",
                    isNotEdit = false,
                    isNew = false,
                    notEditNumberDiscrepancies = ""
            ))
        }

        updateData()
    }

    private fun cleanGoodsForShipmentPP() {
        processedSelectionsHelper.selectedPositions.value?.map { position ->
            taskManager
                    .getReceivingTask()
                    ?.taskRepository
                    ?.getProductsDiscrepancies()
                    ?.deleteProductsDiscrepanciesForProduct(listProcessed.value?.get(position)!!.productInfo!!)

            taskManager
                    .getReceivingTask()
                    ?.taskRepository
                    ?.getMercuryDiscrepancies()
                    ?.deleteMercuryDiscrepanciesForProduct(listProcessed.value?.get(position)!!.productInfo!!)
        }

        updateData()
    }

    private fun getCountProductNotProcessed() : Double {
        return taskManager
                .getReceivingTask()
                ?.let { task ->
                    val taskType =task.taskHeader.taskType
                    val productsDiscrepancies = task.taskRepository.getProductsDiscrepancies()
                    task.getProcessedProducts()
                            .mapNotNull { productInfo ->
                                if (taskType == TaskType.RecalculationCargoUnit) {
                                    val countOrderQuantity =
                                            task.taskRepository
                                                    .getProducts()
                                                    .getProcessingUnitsOfProduct(productInfo.materialNumber)
                                                    .map { unitInfo -> unitInfo.orderQuantity.toDouble() }
                                                    .sumByDouble { orderQuantity -> orderQuantity }
                                    productsDiscrepancies
                                            .getCountProductNotProcessedOfProductPGEOfProcessingUnits(productInfo, countOrderQuantity)
                                            .takeIf { it > 0.0 }
                                } else {
                                    productsDiscrepancies
                                            .getCountProductNotProcessedOfProduct(productInfo)
                                            .takeIf { it > 0.0 }
                                }
                            }
                            .sumByDouble { it }
                } ?: 0.0
    }

    fun onClickSave() {
        if (getCountProductNotProcessed() > 0.0) {
            screenNavigator.openDiscrepancyListScreen()
            return
        }

        saveProductDelegate.saveDataInERP()
    }

    fun onBackPressed() {
        screenNavigator.openUnsavedDataDialog(
                yesCallbackFunc = {
                    screenNavigator.openUnlockTaskLoadingScreen()
                }
        )
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
        setRequestFocus()
        setEanCode()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        setEanCode()
        eanCode.value?.let {
            searchProductDelegate.searchCode(it, fromScan = false)
        }
        return true
    }

    private fun setEanCode() {
        eanCode.value = when (selectedPage.value) {
            GOODS_LIST_VIEW_PAGE_COUNTED_OR_TO_PROCESSING -> eanCodeCountedOrToProcessing.value
            GOODS_LIST_VIEW_PAGE_WITHOUT_BARCODE_OR_PROCESSED -> eanCodeWithoutBarcodeOrProcessed.value
            else -> null
        }
    }

    private fun setRequestFocus() {
        when (selectedPage.value) {
            GOODS_LIST_VIEW_PAGE_COUNTED_OR_TO_PROCESSING -> {
                requestFocusWithoutBarcodeOrProcessed.value = false
                requestFocusCountedOrToProcessing.value = true
            }
            GOODS_LIST_VIEW_PAGE_WITHOUT_BARCODE_OR_PROCESSED -> {
                requestFocusCountedOrToProcessing.value = false
                requestFocusWithoutBarcodeOrProcessed.value = true
            }
        }
    }

    private fun getManufacturerName(batchInfo: TaskBatchInfo?): String {
        return repoInMemoryHolder.manufacturers.value?.findLast { manufacture ->
            manufacture.code == batchInfo?.egais
        }?.name.orEmpty()
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

    private fun getAcceptTotalCountWithUomBatch(batchInfo: TaskBatchInfo?, uom: Uom): String {
        val currentTaskType =
                taskManager.getReceivingTask()
                        ?.taskHeader
                        ?.taskType
        val acceptTotalCountBatch = batchInfo?.let {
            if (currentTaskType == TaskType.RecalculationCargoUnit) {
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getBatchesDiscrepancies()
                        ?.getCountAcceptOfBatchPGE(batchInfo)
            } else {
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getBatchesDiscrepancies()
                        ?.getCountAcceptOfBatch(batchInfo)
            }
        }
        return if (acceptTotalCountBatch != 0.0) {
            "+ ${acceptTotalCountBatch.toStringFormatted()} ${uom.name}"
        } else {
            "0 ${uom.name}"
        }
    }

    private fun getRefusalTotalCountWithUomBatch(batchInfo: TaskBatchInfo?, uom: Uom): String {
        val refusalTotalCountBatch = batchInfo?.let {
            if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
                taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatchPGE(batchInfo)
            } else {
                taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatch(batchInfo)
            }
        }
        return if (refusalTotalCountBatch != 0.0) {
            "- ${refusalTotalCountBatch.toStringFormatted()} ${uom.name}"
        } else {
            "0 ${uom.name}"
        }
    }

    private fun getAcceptTotalCountWithUomZBatch(discrepancies: TaskZBatchesDiscrepancies?, uom: Uom): String {
        val currentTaskType =
                taskManager.getReceivingTask()
                        ?.taskHeader
                        ?.taskType

        val acceptTotalCountBatch = discrepancies?.let {
            if (currentTaskType == TaskType.RecalculationCargoUnit) {
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getZBatchesDiscrepancies()
                        ?.getCountAcceptOfZBatchPGE(discrepancies)
            } else {
                taskManager.getReceivingTask()
                        ?.taskRepository
                        ?.getZBatchesDiscrepancies()
                        ?.getCountAcceptOfZBatch(discrepancies)
            }
        }
        return if (acceptTotalCountBatch != 0.0) {
            "+ ${acceptTotalCountBatch.toStringFormatted()} ${uom.name}"
        } else {
            "0 ${uom.name}"
        }
    }

    private fun getAcceptTotalCountWithUomProduct(productInfo: TaskProductInfo, uom: Uom): String {
        val acceptTotalCount = if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProductPGE(productInfo)
        } else {
            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountAcceptOfProduct(productInfo)
        }
        return if (acceptTotalCount != 0.0) {
            "+ ${acceptTotalCount.toStringFormatted()} ${uom.name}"
        } else {
            "0 ${uom.name}"
        }
    }

    private fun getRefusalTotalCountWithUomProduct(productInfo: TaskProductInfo, uom: Uom): String {
        val refusalTotalCount = if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProductPGE(productInfo)
        } else {
            taskManager.getReceivingTask()?.taskRepository?.getProductsDiscrepancies()?.getCountRefusalOfProduct(productInfo)
        }
        return if (refusalTotalCount != 0.0) {
            "- ${refusalTotalCount.toStringFormatted()} ${uom.name}"
        } else {
            "0 ${uom.name}"
        }
    }

}
