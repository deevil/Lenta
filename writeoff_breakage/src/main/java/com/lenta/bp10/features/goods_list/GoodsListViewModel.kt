package com.lenta.bp10.features.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.models.repositories.getTotalCountForProduct
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.db.ProductInfoDbRequest
import com.lenta.bp10.requests.db.ProductInfoRequestParams
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.resources.IStringResourceManager
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager
    @Inject
    lateinit var productInfoDbRequest: ProductInfoDbRequest
    @Inject
    lateinit var stringResourceManager: IStringResourceManager

    val countedGoods: MutableLiveData<List<GoodItem>> = MutableLiveData()
    val filteredGoods: MutableLiveData<List<FilterItem>> = MutableLiveData()
    val categories: MutableLiveData<List<String>> = MutableLiveData()
    val selectedCategoryPosition: MutableLiveData<Int> = MutableLiveData(0)
    val eanCode: MutableLiveData<String> = MutableLiveData()

    val onCategoryPositionClickListener = object : OnPositionClickListener {
        override fun onClickPosition(position: Int) {
            selectedCategoryPosition.value = position
            updateFilter()
        }
    }

    init {
        viewModelScope.launch {
            updateCounted()
            updateFilter()
        }

    }

    fun onResume() {
        updateCounted()
        updateFilter()
    }

    private fun updateCounted() {
        processServiceManager.getWriteOffTask()?.let {
            countedGoods.postValue(
                    it.getProcessedProducts()
                            .mapIndexed { index, productInfo ->
                                GoodItem(
                                        number = index + 1,
                                        name = productInfo.description,
                                        quantity = "${it.taskRepository.getTotalCountForProduct(productInfo)} ${productInfo.uom.name}",
                                        even = index % 2 == 0)
                            }
                            .reversed())
        }

    }

    private fun updateFilter() {
        processServiceManager.getWriteOffTask()?.let { writeOffTask ->

            val writeOffReasons = writeOffTask.taskRepository.getWriteOffReasons().getWriteOffReasons()

            val reasons = writeOffReasons.map {
                it.writeOffReason.code to it
            }.toMap().values.toList()

            (if (reasons.size == 1) {
                mutableListOf()
            } else {
                mutableListOf(stringResourceManager.notSelected())
            }).let {
                it.addAll(reasons.map { taskWriteOffReason -> taskWriteOffReason.writeOffReason.name })
                categories.postValue(it)
            }

            val selectedCategory = reasons.getOrNull((selectedCategoryPosition.value ?: -1) - 1)

            filteredGoods.postValue(writeOffReasons
                    .filter {
                        selectedCategory == null
                                || selectedCategory.writeOffReason.code == it.writeOffReason.code
                    }
                    .mapIndexed { index, taskWriteOffReason ->
                        val productInfo =
                                writeOffTask.taskRepository.getProducts().findProduct(taskWriteOffReason.materialNumber)!!
                        FilterItem(
                                number = index + 1,
                                name = productInfo.description,
                                reason = taskWriteOffReason.writeOffReason.name,
                                quantity = "${taskWriteOffReason.count} ${productInfo.uom.name}",
                                even = index % 2 == 0)
                    }.reversed())
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        viewModelScope.launch {
            eanCode.value?.let {
                productInfoDbRequest(ProductInfoRequestParams(ean = it)).either(::handleFailure, ::handleScanSuccess)
            }

        }

        Logg.d { "processServiceManager taskDescription: ${processServiceManager.getWriteOffTask()?.taskDescription}" }
        return true
    }

    private fun handleScanSuccess(productInfo: ProductInfo) {
        Logg.d { "productInfo: $productInfo" }
        screenNavigator.openGoodInfoScreen(productInfo)
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }


    fun onEanInfoClick() {
        screenNavigator.openEanInfoScreen()
    }

}


data class GoodItem(val number: Int, val name: String, val quantity: String, val even: Boolean) : Evenable {
    override fun isEven() = even

}

data class FilterItem(val number: Int, val name: String, val reason: String, val quantity: String, val even: Boolean) : Evenable {
    override fun isEven() = even

}



