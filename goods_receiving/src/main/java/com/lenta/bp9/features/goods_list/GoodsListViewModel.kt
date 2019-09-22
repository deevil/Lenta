package com.lenta.bp9.features.goods_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.model.task.TaskProductInfo
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class GoodsListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager

    val selectedPage = MutableLiveData(0)
    val countedSelectionsHelper = SelectionItemsHelper()
    val listCounted: MutableLiveData<List<ListCountedItem>> = MutableLiveData()
    val listWithoutBarcode: MutableLiveData<List<ListWithoutBarcodeItem>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()
    private val isBatches: MutableLiveData<Boolean> = MutableLiveData(false)

    val visibilityCleanButton: MutableLiveData<Boolean> = selectedPage.map {
        it == 0
    }

    val enabledCleanButton: MutableLiveData<Boolean> = countedSelectionsHelper.selectedPositions.map {
        val selectedComponentsPositions = countedSelectionsHelper.selectedPositions.value
        !selectedComponentsPositions.isNullOrEmpty()
    }

    val visibilityBatchesButton: MutableLiveData<Boolean> = MutableLiveData()


    fun onResume() {
        visibilityBatchesButton.value = taskManager.getReceivingTask()?.taskDescription?.isAlco
        updateListCounted()
        updateListWithoutBarcode()
    }

    private fun updateListCounted() {
        taskManager.getReceivingTask()?.let { task ->
            if (!isBatches.value!!) {
                listCounted.postValue(
                        task.getProcessedProducts()
                                .filter {
                                    !it.isNoEAN
                                }
                                .mapIndexed { index, productInfo ->
                                    ListCountedItem(
                                            number = index + 1,
                                            name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                            countAccept = task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo),
                                            countRefusal = task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo),
                                            uomName = productInfo.uom.name,
                                            productInfo = productInfo,
                                            batchInfo = null,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            } else {
                listCounted.postValue(
                        task.taskRepository.getBatches().getBatches()
                                .filter {
                                    !it.isNoEAN
                                }
                                .mapIndexed { index, batchInfo ->
                                    ListCountedItem(
                                            number = index + 1,
                                            name = "${batchInfo.getMaterialLastSix()} ${batchInfo.description}",
                                            countAccept = task.taskRepository.getBatchesDiscrepancies().getCountAcceptOfBatch(batchInfo),
                                            countRefusal = task.taskRepository.getBatchesDiscrepancies().getCountRefusalOfBatch(batchInfo),
                                            uomName = batchInfo.uom.name,
                                            productInfo = null,
                                            batchInfo = batchInfo,
                                            even = index % 2 == 0)
                                }
                                .reversed())
            }

        }

        countedSelectionsHelper.clearPositions()

    }

    private fun updateListWithoutBarcode() {
        taskManager.getReceivingTask()?.let { task ->
            if (!isBatches.value!!) {
                listWithoutBarcode.postValue(
                        task.getProcessedProducts()
                                .filter {
                                    it.isNoEAN
                                }.mapIndexed { index, productInfo ->
                                    ListWithoutBarcodeItem(
                                            number = index + 1,
                                            name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                            even = index % 2 == 0)
                                }
                                .reversed())
            } else {
                listWithoutBarcode.postValue(
                        task.taskRepository.getBatches().getBatches()
                                .filter {
                                    it.isNoEAN
                                }.mapIndexed { index, batchInfo ->
                                    ListWithoutBarcodeItem(
                                            number = index + 1,
                                            name = "${batchInfo.getMaterialLastSix()} ${batchInfo.description}",
                                            even = index % 2 == 0)
                                }
                                .reversed())
            }
        }
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onScanResult(data: String) {
        //todo
        return
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickItemPosition(position: Int) {
        //todo
        /**if (selectedPage.value == 0) {
        countedGoods.value?.getOrNull(position)?.productInfo
        } else {
        filteredGoods.value?.getOrNull(position)?.productInfo
        }?.let {
        searchProductDelegate.openProductScreen(it, 0.0)
        }*/
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToEan.value = true
        eanCode.value = eanCode.value ?: "" + digit
    }

    override fun onOkInSoftKeyboard(): Boolean {
        eanCode.value?.let {
            //todo
            //searchProductDelegate.searchCode(it, fromScan = false)
        }
        return true
    }

    fun onClickGoodsTitle(position: Int) {
        //todo
        return
    }

    fun onClickRefusal() {
        //todo экран "Отказ в приемке" еще в разработке
        screenNavigator.openInfoScreen("экран \"Отказ в приемке\" еще в разработке")
    }

    fun onClickClean() {
        if (!isBatches.value!!) {
            countedSelectionsHelper.selectedPositions.value?.map { position ->
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getProductsDiscrepancies()
                        ?.deleteProductsDiscrepanciesForProduct(listCounted.value?.get(position)!!.productInfo!!)
                taskManager.getReceivingTask()?.taskRepository?.getProducts()?.changeProduct(listCounted.value?.get(position)!!.productInfo!!.copy(isNoEAN = true))
            }
        } else {
            countedSelectionsHelper.selectedPositions.value?.map { position ->
                taskManager
                        .getReceivingTask()
                        ?.taskRepository
                        ?.getBatchesDiscrepancies()
                        ?.deleteBatchesDiscrepanciesForBatch(listCounted.value?.get(position)!!.batchInfo!!)
                taskManager.getReceivingTask()?.taskRepository?.getBatches()?.changeBatch(listCounted.value?.get(position)!!.batchInfo!!.copy(isNoEAN = true))
            }
        }

        updateListCounted()
        updateListWithoutBarcode()
    }

    fun onClickBatches() {
        isBatches.value = !isBatches.value!!
        updateListCounted()
        updateListWithoutBarcode()
    }

    fun onClickSave() {
        //todo
        val tmpProduct = taskManager.getReceivingTask()!!.taskRepository.getProducts().findProduct(TaskProductInfo(
                materialNumber = "000021",
                description = "Р/к горбуша (Россия) 230/250г",
                uom = Uom("ST", "шт"),
                type = ProductType.General,
                isSet = false,
                sectionId = "01",
                matrixType = MatrixType.Active,
                materialType = "",
                origQuantity = "",
                orderQuantity = "",
                quantityCapitalized = "",
                overdToleranceLimit = "",
                underdToleranceLimit = "",
                upLimitCondAmount = "",
                quantityInvest = "",
                roundingSurplus = "",
                roundingShortages = "",
                isNoEAN = false,
                isWithoutRecount = false,
                isUFF = false,
                isNotEdit = false,
                totalExpirationDate = "",
                remainingShelfLife = "",
                isRus = false,
                isBoxFl = false,
                isMarkFl = false,
                isVet = false,
                numberBoxesControl = "",
                numberStampsControl = ""
        ))
        screenNavigator.openGoodsInfoScreen(tmpProduct!!)
    }
}
