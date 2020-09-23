package com.lenta.movement.features.task.basket

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.features.main.box.ScanInfoHelper
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.models.SimpleListItem
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.IFormatter
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.*
import java.util.*
import javax.inject.Inject

class TaskBasketViewModel() : CoreViewModel(),
        OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var taskBasketsRepository: ITaskBasketsRepository

    @Inject
    lateinit var scanInfoHelper: ScanInfoHelper

    @Inject
    lateinit var formatter: IFormatter

    val basketIndex by unsafeLazy {
        MutableLiveData(DEFAULT_BASKET_INDEX)
    }

    val basket by unsafeLazy {
        basketIndex.value?.let {
            taskBasketsRepository.getBasketByIndex(it)
        }
    }

    val selectionsHelper = SelectionItemsHelper()

    val goods by unsafeLazy { MutableLiveData(getGoods()) }
    val goodsItemList by unsafeLazy {
        goods.mapSkipNulls { goods ->
            goods.mapIndexed { index, (product, count) ->
                val uom = product.uom.name.toLowerCase(Locale.getDefault())
                SimpleListItem(
                        number = index + 1,
                        title = formatter.getProductName(product),
                        countWithUom = "$count $uom",
                        isClickable = true
                )
            }
        }
    }

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val deleteEnabled = selectionsHelper.selectedPositions.map { selectedPositions ->
        selectedPositions.orEmpty().isNotEmpty()
    }

    val title by unsafeLazy {
        asyncLiveData<String> {
            val task = taskManager.getTask()
            val taskSettings = getSettings()
            basket?.let {
                val innerTitle = formatter.getBasketTitle(
                        basket = it,
                        task = task,
                        taskSettings = taskSettings
                )
                emit(innerTitle)
            }
        }
    }

    private suspend fun getSettings() = taskManager.getTaskSettings()

    fun onDeleteClick() {
        basketIndex.value?.let { basketIndexValue ->
            val basket = taskBasketsRepository.getBasketByIndex(basketIndexValue)
            selectionsHelper.selectedPositions.value.orEmpty()
                    .mapNotNull { doRemoveProductIndex ->
                        basket?.getByIndex(doRemoveProductIndex)
                    }
                    .forEach { doRemoveProduct ->
                        basket?.remove(doRemoveProduct)
                    }
            basket.takeIf { it.isNullOrEmpty() }?.let(
                    taskBasketsRepository::removeBasket
            )
            selectionsHelper.clearPositions()
            goods.value = getGoods()
        }
    }

    fun onCharacteristicsClick() {
        basketIndex.value?.let {
            screenNavigator.openTaskBasketCharacteristicsScreen(it)
        }
    }

    fun onNextClick() {
        screenNavigator.goBack()
    }

    fun onItemClick(position: Int) {
        goods.value?.let { list ->
            list.getOrNull(position)?.let { (product, _) ->
                screenNavigator.openTaskGoodsInfoScreen(product)
            }
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        searchCode(eanCode.value.orEmpty(), fromScan = false)
        return true
    }

    fun onScanResult(data: String) {
        searchCode(code = data, fromScan = true, isBarCode = true)
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToEan.value = true
        eanCode.value = eanCode.value ?: "" + digit
    }

    private fun getGoods(): List<Pair<ProductInfo, Int>> {
        return basketIndex.value?.let { basketIndexValue ->
            taskBasketsRepository.getBasketByIndex(basketIndexValue)?.toList()
        }.orEmpty()
    }

    private fun searchCode(code: String, fromScan: Boolean, isBarCode: Boolean? = null) {
        launchUITryCatch {
            scanInfoHelper.searchCode(code, fromScan, isBarCode) { productInfo ->
                onSearchResult(productInfo)
            }
        }
    }

    private fun addProductToRep(productInfo: ProductInfo) {
        launchAsyncTryCatch {
            taskBasketsRepository.addProduct(
                    product = productInfo,
                    count = ONE_PRODUCT_TO_ADD)
        }
    }

    private fun onSearchResult(productInfo: ProductInfo) {
        launchUITryCatch {
            screenNavigator.goBack()
            screenNavigator.openTaskGoodsInfoScreen(productInfo)
        }
    }

    companion object {
        private const val DEFAULT_BASKET_INDEX = 0
        private const val ONE_PRODUCT_TO_ADD = 1
    }
}