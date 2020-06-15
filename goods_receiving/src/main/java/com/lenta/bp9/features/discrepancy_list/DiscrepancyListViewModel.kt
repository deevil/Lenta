package com.lenta.bp9.features.discrepancy_list

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.features.goods_list.ListCountedItem
import com.lenta.bp9.features.goods_list.SearchProductDelegate
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.repos.IRepoInMemoryHolder
import com.lenta.bp9.requests.network.EndRecountDDParameters
import com.lenta.bp9.requests.network.EndRecountDDResult
import com.lenta.bp9.requests.network.EndRecountDirectDeliveriesNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getProductInfoByMaterial
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.models.core.getProductType
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.ScanInfoResult
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    @Inject
    lateinit var hyperHive: HyperHive

    private val zfmpUtz48V001: ZfmpUtz48V001 by lazy {
        ZfmpUtz48V001(hyperHive)
    }

    val selectedPage = MutableLiveData(0)
    val processedSelectionsHelper = SelectionItemsHelper()
    val countNotProcessed: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()
    val countProcessed: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()
    val countControl: MutableLiveData<List<GoodsDiscrepancyItem>> = MutableLiveData()
    private val isBatches: MutableLiveData<Boolean> = MutableLiveData(false)
    private val qualityInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()

    val isAlco: MutableLiveData<Boolean> by lazy {
        //проверяем, есть ли алкогольные акцизные товары в задании и стоит ли признак isAlco в задании
        MutableLiveData(!taskManager.getReceivingTask()?.taskRepository?.getProducts()?.getProducts()?.filter {product ->
            product.isBoxFl || product.isMarkFl
        }.isNullOrEmpty() && taskManager.getReceivingTask()?.taskDescription?.isAlco == true)
    }

    val visibilityCleanButton: MutableLiveData<Boolean> = selectedPage.map {
        if (isAlco.value == false) {
            it == 1
        } else {
            it == 2
        }
    }

    val enabledCleanButton: MutableLiveData<Boolean> = processedSelectionsHelper.selectedPositions.map {
        !it.isNullOrEmpty()
    }

    val enabledSaveButton: MutableLiveData<Boolean> = countProcessed.map {
        it.isNullOrEmpty()
    }

    val visibilityBatchesButton: MutableLiveData<Boolean> by lazy {
        MutableLiveData(taskManager.getReceivingTask()?.taskDescription?.isAlco == true && !(taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ShipmentPP || taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.ShipmentRC)) //для заданий ОПП и ОРЦ не показываем кнопку Партия, уточнил у Артема
    }

    init {
        viewModelScope.launch {
            searchProductDelegate.init(viewModelScope = this@DiscrepancyListViewModel::viewModelScope,
                    scanResultHandler = this@DiscrepancyListViewModel::handleProductSearchResult)
        }
    }

    private fun handleProductSearchResult(@Suppress("UNUSED_PARAMETER") scanInfoResult: ScanInfoResult?): Boolean {
        return false
    }

    fun onResume() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
                qualityInfo.value = dataBase.getQualityInfoPGE()
            } else {
                qualityInfo.value = dataBase.getAllReasonRejectionInfo()?.map {
                    QualityInfo(
                            id = it.id,
                            code = it.code,
                            name = it.name
                    )
                }
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
                    }.sortedByDescending {sorted ->
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
                            batchesInfoOfProduct?.map {batch ->
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
                                                isNormDiscrepancies = false,
                                                productInfo = productInfo,
                                                productDiscrepancies = null,
                                                batchInfo = null,
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
                                    val countOrderQuantity = processingUnitsOfProduct.map {unitInfo ->
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
                                            isNormDiscrepancies = false,
                                            productInfo = productInfo,
                                            productDiscrepancies = null,
                                            batchInfo = null,
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

    }

    private fun updateCountProcessed() {
        val arrayCounted: ArrayList<GoodsDiscrepancyItem> = ArrayList()
        var index = 0
        var addeBatchProduct = ""
        taskManager.getReceivingTask()?.let { task ->
            task.taskRepository.getProductsDiscrepancies().getProductsDiscrepancies()
                    .filter {
                        it.typeDiscrepancies != "1"
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
                        val isNormDiscrepancies = when (repoInMemoryHolder.taskList.value?.taskListLoadingMode) {
                            TaskListLoadingMode.PGE -> productDiscrepancies.typeDiscrepancies == "1" || productDiscrepancies.typeDiscrepancies == "2"
                            else -> productDiscrepancies.typeDiscrepancies == "1"
                        }
                        if (isBatches.value == true && productInfo?.type == ProductType.NonExciseAlcohol && !productInfo.isBoxFl && !productInfo.isMarkFl) {
                            if (addeBatchProduct != productInfo.materialNumber) { //показываем партии без разбивки по расхождениям
                                addeBatchProduct = productInfo.materialNumber
                                val batchesInfoOfProduct = task.taskRepository.getBatches().findBatchOfProduct(productInfo)
                                batchesInfoOfProduct?.map {batch ->
                                    val batchInfo = task.taskRepository.getBatches().findBatch(
                                            batchNumber = batch.batchNumber,
                                            materialNumber = batch.materialNumber,
                                            processingUnitNumber = batch.processingUnitNumber
                                    )
                                    val countRefusal = task.taskRepository.getBatchesDiscrepancies().findBatchDiscrepanciesOfBatch(batch).filter {fIt ->
                                        fIt.typeDiscrepancies != "1"
                                    }.map {mIt ->
                                        mIt.numberDiscrepancies
                                    }.sumByDouble {
                                        it.toDouble()
                                    }
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
                                                    isNormDiscrepancies = isNormDiscrepancies,
                                                    productInfo = productInfo,
                                                    productDiscrepancies = productDiscrepancies,
                                                    batchInfo = null,
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
                                            countRefusalWithUom = "${productDiscrepancies.numberDiscrepancies.toDouble().toStringFormatted()} ${uom?.name}",
                                            quantityNotProcessedWithUom = "",
                                            discrepanciesName = discrepanciesName ?: "",
                                            isNormDiscrepancies = isNormDiscrepancies,
                                            productInfo = productInfo,
                                            productDiscrepancies = productDiscrepancies,
                                            batchInfo = null,
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
            countControl.postValue( //https://trello.com/c/9K1FZnUU, отображать перечень маркированных товаров, по которым не пройден хотя бы один из видов контроля
                    task.getProcessedProducts()
                            .filter {goodsInfo ->
                                normEnteredButControlNotPassed(goodsInfo)
                            }
                            .sortedByDescending {
                                it.materialNumber
                            }
                            .mapIndexed { index, productInfo ->
                                GoodsDiscrepancyItem(
                                        number = index + 1,
                                        name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                        nameMaxLines = 2,
                                        nameBatch = "",
                                        visibilityNameBatch = false,
                                        countRefusalWithUom = "",
                                        quantityNotProcessedWithUom = "",
                                        discrepanciesName = "",
                                        isNormDiscrepancies = false,
                                        productInfo = productInfo,
                                        productDiscrepancies = null,
                                        batchInfo = null,
                                        checkBoxControl = taskManager.getReceivingTask()?.controlBoxesOfProduct(productInfo) ?: false,
                                        checkStampControl = taskManager.getReceivingTask()?.controlExciseStampsOfProduct(productInfo) ?: false,
                                        even = index % 2 == 0)
                            }
                            .reversed())
        }
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickItemPosition(position: Int) {
        val matnr: String? = if (selectedPage.value == 0) {
            countNotProcessed.value?.get(position)?.productInfo?.materialNumber
        } else {
            if ( (isAlco.value == true && selectedPage.value == 2) || (isAlco.value == false && selectedPage.value == 1)) {
                countProcessed.value?.get(position)?.productInfo?.materialNumber
            } else {
                countControl.value?.get(position)?.productInfo?.materialNumber
            }
        }

        if (repoInMemoryHolder.taskList.value?.taskListLoadingMode == TaskListLoadingMode.Receiving &&
                countNotProcessed.value?.get(position)?.productInfo?.isBoxFl == true &&
                selectedPage.value == 0) { //коробочный учет для ПРИЕМКИ https://trello.com/c/WeGFSdAW
            screenNavigator.openExciseAlcoBoxProductFailureScreen(countNotProcessed.value?.get(position)?.productInfo!!)
        } else {
            searchProductDelegate.searchCode(code = matnr ?: "", fromScan = false, isDiscrepancy = true)
        }
    }

    fun onClickClean() {
        processedSelectionsHelper.selectedPositions.value?.map { position ->
            if (!countProcessed.value?.get(position)!!.productInfo!!.isNotEdit) {
                if (isBatches.value == true && !countProcessed.value?.get(position)!!.productInfo!!.isBoxFl && !countProcessed.value?.get(position)!!.productInfo!!.isMarkFl) {
                    //удаляем все расхождения, кроме Нормы
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getProductsDiscrepancies()
                            ?.deleteProductsDiscrepanciesNotNormForProduct(countProcessed.value?.get(position)!!.productInfo!!)


                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getMercuryDiscrepancies()
                            ?.deleteMercuryDiscrepanciesNotNormForProduct(countProcessed.value?.get(position)!!.productInfo!!)

                    //но здесь не учитывается, что может быть несколько партий, а только одна
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getBatchesDiscrepancies()
                            ?.deleteBatchesDiscrepanciesNotNormForProduct(countProcessed.value?.get(position)!!.productInfo!!.materialNumber)
                } else {
                    //удаляем конкретное расхождение
                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getProductsDiscrepancies()
                            ?.deleteProductDiscrepancy(countProcessed.value?.get(position)!!.productDiscrepancies!!)

                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getMercuryDiscrepancies()
                            ?.deleteMercuryDiscrepancyOfProduct(countProcessed.value?.get(position)!!.productInfo!!.materialNumber, countProcessed.value?.get(position)!!.productDiscrepancies!!.typeDiscrepancies)

                    taskManager
                            .getReceivingTask()
                            ?.taskRepository
                            ?.getBatchesDiscrepancies()
                            ?.deleteBatchesDiscrepanciesForProductAndDiscrepancies(countProcessed.value?.get(position)!!.productInfo!!.materialNumber, countProcessed.value?.get(position)!!.productDiscrepancies!!.typeDiscrepancies)
                }
            }

        }

        updateData()
    }

    fun onClickBatches() {
        isBatches.value = !isBatches.value!!
        updateData()
    }

    fun onClickSave() {
        viewModelScope.launch {
            screenNavigator.showProgressLoadingData()
            //очищаем таблицу ET_TASK_DIFF от не акцизного алкоголя, т.к. для этих товаров необходимо передавать только данные из таблицы ET_PARTS_DIFF
            taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getProductsDiscrepancies().map {
                val materialInfo = zfmpUtz48V001.getProductInfoByMaterial(it.materialNumber)
                val productType = getProductType(isAlco = materialInfo?.isAlco == "X", isExcise = materialInfo?.isExc == "X")
                if (productType == ProductType.NonExciseAlcohol) {
                    taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().deleteProductsDiscrepanciesForProduct(it.materialNumber)
                }
            }

            endRecountDirectDeliveries(EndRecountDDParameters(
                    taskNumber = taskManager.getReceivingTask()!!.taskHeader.taskNumber,
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber ?: "",
                    discrepanciesProduct = taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getProductsDiscrepancies().map { TaskProductDiscrepanciesRestData.from(it) },
                    discrepanciesBatches = taskManager.getReceivingTask()!!.taskRepository.getBatchesDiscrepancies().getBatchesDiscrepancies().map { TaskBatchesDiscrepanciesRestData.from(it) },
                    discrepanciesBoxes = taskManager.getReceivingTask()!!.taskRepository.getBoxesDiscrepancies().getBoxesDiscrepancies().map { TaskBoxDiscrepanciesRestData.from(it) },
                    discrepanciesExciseStamp = taskManager.getReceivingTask()!!.taskRepository.getExciseStampsDiscrepancies().getExciseStampDiscrepancies().map { TaskExciseStampDiscrepanciesRestData.from(it) },
                    exciseStampBad = taskManager.getReceivingTask()!!.taskRepository.getExciseStampsBad().getExciseStampsBad().map { TaskExciseStampBadRestData.from(it) },
                    discrepanciesMercury = taskManager.getReceivingTask()!!.taskRepository.getMercuryDiscrepancies().getMercuryDiscrepancies().map { TaskMercuryDiscrepanciesRestData.from(it) }
            )).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    private fun handleSuccess(result: EndRecountDDResult) {
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None)
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        screenNavigator.openAlertScreen(failure, pageNumber = "97")
    }

    private fun normEnteredButControlNotPassed(productInfo: TaskProductInfo) : Boolean {
        /** Артем:
          товар на вкладке Контроль появляется если по товару была введеная норма, но не был пройден контроль
          Причем если по товару
          10 шт
          контроль 2 шт,
          а по факту мы ввели 1 норму (и подтвердили сканированием) и 9 брака то контроль считается пройден*/
        return if (taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo) <= 0 || taskManager.getReceivingTask()!!.controlBoxesOfProduct(productInfo)) {
            false
        } else {
            ((taskManager.getReceivingTask()?.countBoxesPassedControlOfProduct(productInfo) ?: 0) +
                    taskManager.getReceivingTask()!!.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo)) < productInfo.origQuantity.toDouble()
        }
    }

    private fun getManufacturerName(batchInfo: TaskBatchInfo?) : String {
        return repoInMemoryHolder.manufacturers.value?.findLast {manufacture ->
            manufacture.code == batchInfo?.egais
        }?.name ?: ""
    }

    private fun getRefusalTotalCountWithUomBatch(batchInfo: TaskBatchInfo?, uom: Uom?) : String {
        val refusalTotalCountBatch = batchInfo?.let {
            if (taskManager.getReceivingTask()?.taskHeader?.taskType == TaskType.RecalculationCargoUnit) {
                taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatchPGE(batchInfo)
            } else {
                taskManager.getReceivingTask()?.taskRepository?.getBatchesDiscrepancies()?.getCountRefusalOfBatch(batchInfo)
            }
        }
        return "${refusalTotalCountBatch.toStringFormatted()} ${uom?.name}"
    }

}
