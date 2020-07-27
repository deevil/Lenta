package com.lenta.movement.features.task.goods.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import com.google.common.base.Optional
import com.lenta.movement.features.main.box.ScanInfoHelper
import com.lenta.movement.models.Basket
import com.lenta.movement.models.GoodsSignOfDivision
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.Supplier
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TaskGoodsInfoViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var taskBasketsRepository: ITaskBasketsRepository

    lateinit var productInfo: ProductInfo

    @Inject
    lateinit var scanInfoHelper: ScanInfoHelper

    val quantityList = MutableLiveData<List<String>>()

    val quantity = MutableLiveData("0")
    val quantityUom = MutableLiveData(Uom.DEFAULT)

    private val supplierSelected = MutableLiveData(Optional.absent<Supplier>())
    val supplierListVisible by lazy {
        asyncLiveData<Boolean> {
            val settings = taskManager.getTaskSettings()
            val isSupplierListVisible = productInfo.suppliers.size > 1 && settings.signsOfDiv.contains(GoodsSignOfDivision.LIF_NUMBER)
            emit(isSupplierListVisible)
        }
    }

    val supplierList by unsafeLazy { MutableLiveData(productInfo.suppliers.map { it.name }) }
    val supplierSelectedListener = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            supplierSelected.value =
                    Optional.fromNullable(productInfo.suppliers.getOrNull(position))
        }
    }

    val currentBasket: LiveData<Basket> by unsafeLazy {
        supplierSelected.switchMap { selectedSupplier ->
            asyncLiveData<Basket> {
                val basket = taskBasketsRepository.getSuitableBasketOrCreate(productInfo, selectedSupplier.orNull())
                emit(basket)
            }
        }
    }

    val forBasketQuantity: LiveData<Int> by unsafeLazy {
        currentBasket.combineLatest(quantity).mapSkipNulls { (currentBasket, quantityString) ->
            (currentBasket[productInfo] ?: 0) + (quantityString.toIntOrNull() ?: 0)
        }
    }

    val totalQuantity: LiveData<Int> by lazy {
        forBasketQuantity.combineLatest(currentBasket)
                .mapSkipNulls { (forBasketQuantity, currentBasket) ->
                    taskBasketsRepository.getAll()
                            .filter { basket ->
                                basket.index != currentBasket.index
                            }
                            .flatMap { basket ->
                                basket.filterKeys { basketEntity ->
                                    basketEntity.materialNumber == productInfo.materialNumber
                                }.values
                            }
                            .sum() + forBasketQuantity
                }
    }

    val applyEnabled: LiveData<Boolean> by lazy {
        quantity.mapSkipNulls { quantity ->
            (quantity.toIntOrNull() ?: 0) > 0
        }
    }

    fun getTitle(): String {
        return "${productInfo.getMaterialLastSix()} ${productInfo.description}"
    }

    fun onApplyClick() {
        launchAsyncTryCatch {
            addProductToRepository()
        }
        screenNavigator.goBack()
        currentBasket.value?.let { basketValue ->
            screenNavigator.openTaskBasketScreen(basketValue.index)
        }
    }

    private suspend fun addProductToRepository() =
            withContext(Dispatchers.IO) {
                taskBasketsRepository.addProduct(
                        product = productInfo,
                        supplier = supplierSelected.value?.orNull(),
                        count = quantity.value?.toIntOrNull() ?: 0
                )
            }

    fun onDetailsClick() {
        screenNavigator.openTaskGoodsDetailsScreen(productInfo)
    }

    fun onScanResult(data: String) {
        if (applyEnabled.value == true) {
            launchUITryCatch {
                addProductToRepository()
                searchCode(data)
            }
        }
    }

    private suspend fun searchCode(code: String) {
            scanInfoHelper.searchCode(code, fromScan = true, isBarCode = true) {
                with(screenNavigator) {
                    goBack()
                    openTaskGoodsInfoScreen(it)
                }
            }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        if (applyEnabled.value == true) {
            onApplyClick()
        }
        return true
    }
}