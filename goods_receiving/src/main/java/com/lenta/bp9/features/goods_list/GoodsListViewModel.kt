package com.lenta.bp9.features.goods_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.ReceivingProductInfo
import com.lenta.bp9.model.task.IReceivingTaskManager
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
    val countedGoods: MutableLiveData<List<GoodsListCountedItem>> = MutableLiveData()
    val withoutBarcodeGoods: MutableLiveData<List<GoodsListWithoutBarcodeItem>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val visibilityCleanButton: MutableLiveData<Boolean> = selectedPage.map {
        it == 0
    }

    val enabledCleanButton: MutableLiveData<Boolean> = countedSelectionsHelper.selectedPositions.map {
        val selectedComponentsPositions = countedSelectionsHelper.selectedPositions.value
        !selectedComponentsPositions.isNullOrEmpty()
    }


    fun onResume() {
        updateCounted()
        updateWithoutBarcode()
    }

    private fun updateCounted() {
        taskManager.getReceivingTask()?.let {task ->
            countedGoods.postValue(
                    task.getProcessedProducts()
                            .filter {
                                !it.isNoEAN
                            }
                            .mapIndexed { index, productInfo ->
                                GoodsListCountedItem(
                                        number = index + 1,
                                        name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                        countAccept = task.taskRepository.getProductsDiscrepancies().getCountAcceptOfProduct(productInfo),
                                        countRefusal = task.taskRepository.getProductsDiscrepancies().getCountRefusalOfProduct(productInfo),
                                        even = index % 2 == 0,
                                        productInfo = productInfo)
                            }
                            .reversed())
        }

        countedSelectionsHelper.clearPositions()

    }

    private fun updateWithoutBarcode() {
        taskManager.getReceivingTask()?.let {task ->
            withoutBarcodeGoods.postValue(
                    task.getProcessedProducts()
                            .filter {
                                it.isNoEAN
                            }.mapIndexed {index, productInfo ->
                                GoodsListWithoutBarcodeItem(
                                        number = index + 1,
                                        name = "${productInfo.getMaterialLastSix()} ${productInfo.description}",
                                        even = index % 2 == 0,
                                        productInfo = productInfo)
                            }
                            .reversed())
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

    fun onClickRefusal(){
        //todo
        return
    }

    fun onClickClean(){
        countedSelectionsHelper.selectedPositions.value?.map { position ->
            taskManager
                    .getReceivingTask()
                    ?.taskRepository
                    ?.getProductsDiscrepancies()
                    ?.deleteProductsDiscrepanciesForProduct(countedGoods.value?.get(position)!!.productInfo)
            taskManager.getReceivingTask()?.taskRepository?.getProducts()?.changeProduct(countedGoods.value?.get(position)!!.productInfo.copy(isNoEAN = true))
        }
        updateCounted()
        updateWithoutBarcode()
    }

    fun onClickBatchsProducts(){
        //todo
        return
    }

    fun onClickSave(){
        //todo
        screenNavigator.openGoodsInfoScreen(ReceivingProductInfo(
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
    }
}
