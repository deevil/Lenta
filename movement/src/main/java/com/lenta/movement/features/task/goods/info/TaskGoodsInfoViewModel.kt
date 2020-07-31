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
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.*
import com.lenta.shared.utilities.orIfNull
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

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()

    @Inject
    lateinit var scanInfoHelper: ScanInfoHelper

    val quantityList = MutableLiveData<List<String>>()

    val quantity = MutableLiveData(DEFAULT_QUANTITY_VALUE)
    val quantityUom by unsafeLazy {
        MutableLiveData(productInfo.value?.uom)
    }

    private val supplierSelected = MutableLiveData(Optional.absent<Supplier>())
    val supplierListVisible by lazy {
        asyncLiveData<Boolean> {
            val settings = taskManager.getTaskSettings()
            productInfo.value?.let { product ->
                val isSupplierListVisible = product.suppliers.size > 1 && settings.signsOfDiv.contains(GoodsSignOfDivision.LIF_NUMBER)
                emit(isSupplierListVisible)
            }
        }
    }

    val supplierList by unsafeLazy { MutableLiveData(productInfo.value?.suppliers?.map { it.name }.orEmpty()) }
    val supplierSelectedListener = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            supplierSelected.value =
                    Optional.fromNullable(productInfo.value?.suppliers?.getOrNull(position))
        }
    }

    val currentBasket: LiveData<Basket> by unsafeLazy {
        supplierSelected.switchMap { selectedSupplier ->
            asyncLiveData<Basket> {
                productInfo.value?.let {
                    val signOfDiv = taskManager.getTaskSettings().signsOfDiv
                    val basket = taskBasketsRepository.getSuitableBasketOrCreate(
                            it,
                            selectedSupplier.orNull(),
                            signOfDiv
                    )
                    emit(basket)
                }.orIfNull {
                    Logg.e {
                        "ProductInfo null"
                    }
                }
            }
        }
    }

    val forBasketQuantity by unsafeLazy {
        productInfo.value?.let {
            currentBasket.combineLatest(quantity).mapSkipNulls { (currentBasket, quantityString) ->
                (currentBasket.getOrElse(it) { DEFAULT_ZERO_PRODUCTS }) + (quantityString.toIntOrNull() ?: DEFAULT_ZERO_PRODUCTS)
            }
        }
    }

    val totalQuantity by lazy {
        forBasketQuantity?.combineLatest(currentBasket)
                ?.mapSkipNulls { (forBasketQuantity, currentBasket) ->
                    taskBasketsRepository.getAll()
                            .filter { basket ->
                                basket.index != currentBasket.index
                            }
                            .flatMap { basket ->
                                basket.filterKeys { basketEntity ->
                                    basketEntity.materialNumber == productInfo.value?.materialNumber
                                }.values
                            }
                            .sum() + forBasketQuantity
                }
    }

    val applyEnabled: LiveData<Boolean> by lazy {
        quantity.mapSkipNulls { quantity ->
            (quantity.toIntOrNull() ?: DEFAULT_ZERO_PRODUCTS) > 0
        }
    }

    val isMarkScanAndBoxVisible by unsafeLazy {
        MutableLiveData<Boolean>(productInfo.value?.isExcise)
    }

    fun getTitle(): String {
        return "${productInfo.value?.getMaterialLastSix()} ${productInfo.value?.description}"
    }

    fun onApplyClick() {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData()
            addProductToRepository()
            screenNavigator.hideProgress()
            screenNavigator.goBack()
            productInfo.value?.let { productInfoValue ->
                val indexOfLastProduct = taskBasketsRepository.getLastIndexOfProduct(productInfoValue)
                screenNavigator.openTaskBasketScreen(indexOfLastProduct)
            }
        }
    }

    private suspend fun addProductToRepository() =
            productInfo.value?.let { productInfoValue ->
                withContext(Dispatchers.IO) {
                    taskBasketsRepository.addProduct(
                            product = productInfoValue,
                            supplier = supplierSelected.value?.orNull(),
                            count = quantity.value?.toIntOrNull() ?: DEFAULT_ZERO_PRODUCTS
                    )
                }
            }

    fun onDetailsClick() {
        productInfo.value?.let(
                screenNavigator::openTaskGoodsDetailsScreen
        )
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
            return true
        }
        return false
    }

    companion object {
        private const val DEFAULT_QUANTITY_VALUE = "0"
        private const val DEFAULT_ZERO_PRODUCTS = 0
    }
}