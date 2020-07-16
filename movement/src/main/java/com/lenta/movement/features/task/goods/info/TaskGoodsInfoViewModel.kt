package com.lenta.movement.features.task.goods.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.common.base.Optional
import com.lenta.movement.models.Basket
import com.lenta.movement.models.GoodsSignOfDivision
import com.lenta.movement.models.ITaskManager
import com.lenta.movement.models.ProductInfo
import com.lenta.movement.models.repositories.ITaskBasketsRepository
import com.lenta.movement.platform.extensions.unsafeLazy
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.Supplier
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.mapSkipNulls
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class TaskGoodsInfoViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    @Inject
    lateinit var taskManager: ITaskManager

    @Inject
    lateinit var taskBasketsRepository: ITaskBasketsRepository

    lateinit var productInfo: ProductInfo

    val quantityList = MutableLiveData<List<String>>()

    val quantity = MutableLiveData("")
    val quantityUom = MutableLiveData(Uom.DEFAULT)

    private val supplierSelected = MutableLiveData(Optional.absent<Supplier>())
    val supplierListVisible by lazy {
        val settings = taskManager.getTaskSettings()
        MutableLiveData(productInfo.suppliers.size > 1 && settings.signsOfDiv.contains(GoodsSignOfDivision.LIF_NUMBER))
    }
    val supplierList by unsafeLazy { MutableLiveData(productInfo.suppliers.map { it.name }) }
    val supplierSelectedListener = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            supplierSelected.value =
                Optional.fromNullable(productInfo.suppliers.getOrNull(position))
        }
    }

    val currentBasket: LiveData<Basket> by lazy {
        supplierSelected.mapSkipNulls { selectedSupplier ->
            taskBasketsRepository.getSuitableBasketOrCreate(productInfo, selectedSupplier.orNull())
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
        taskBasketsRepository.addProduct(
            product = productInfo,
            supplier = supplierSelected.value?.orNull(),
            count = quantity.value?.toIntOrNull() ?: 0
        )

        screenNavigator.goBack()
        currentBasket.value?.let { basketValue ->
            screenNavigator.openTaskBasketScreen(basketValue.index)
        }
    }

    fun onDetailsClick() {
        screenNavigator.openTaskGoodsDetailsScreen(productInfo)
    }
}