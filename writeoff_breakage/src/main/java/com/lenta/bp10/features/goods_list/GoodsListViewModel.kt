package com.lenta.bp10.features.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp10.models.repositories.IWriteOffTaskManager
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.db.ProductInfoDbRequest
import com.lenta.bp10.requests.db.ProductInfoRequestParams
import com.lenta.shared.exception.Failure
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var processServiceManager: IWriteOffTaskManager
    @Inject
    lateinit var productInfoDbRequest: ProductInfoDbRequest

    val countedGoods: MutableLiveData<List<GoodItem>> = MutableLiveData()
    val filteredGoods: MutableLiveData<List<GoodItem>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()

    init {
        //TODO (DB) убрать фейковые данные после реализации добавления товаров
        countedGoods.value = listOf(
                GoodItem(number = 3, name = "0000021 Масло", quantity = 1, even = false),
                GoodItem(number = 2, name = "000022 Сыр", quantity = 2, even = true),
                GoodItem(number = 1, name = "000023 Майонез", quantity = 3, even = false)
        )

        filteredGoods.value = listOf(
                GoodItem(number = 2, name = "000021 Яйцо", quantity = 2, even = true),
                GoodItem(number = 1, name = "000021 Молоко", quantity = 3, even = false)
        )
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
        screenNavigator.openGoodInfoScreen(productInfo.materialNumber)
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

}


data class GoodItem(val number: Int, val name: String, val quantity: Int, val even: Boolean) : Evenable {
    override fun isEven() = even

}



