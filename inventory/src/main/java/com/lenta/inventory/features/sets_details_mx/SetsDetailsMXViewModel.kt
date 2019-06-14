package com.lenta.inventory.features.sets_details_mx

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.goods_details.GoodsDetailsMXItem
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetsDetailsMXViewModel : CoreViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val selectedPage = MutableLiveData(0)
    val countedProssed: MutableLiveData<List<GoodsDetailsMXItem>> = MutableLiveData()
    val countedNotProssed: MutableLiveData<List<GoodsDetailsMXItem>> = MutableLiveData()

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
    }

    init {
        viewModelScope.launch {
            //searchProductDelegate.init(viewModelScope = this@GoodsListViewModel::viewModelScope)
            updateNotProcessed()
            updateProcessed()
        }

    }

    fun onResume() {
        updateNotProcessed()
        updateProcessed()
    }

    private fun updateProcessed() {
        countedProssed.postValue(listOf(GoodsDetailsMXItem(
                number = 2,
                name = "123456789",
                quantity = "12 ${productInfo.value!!.uom.name}",
                even = 2 % 2 == 0,
                productInfo = productInfo.value!!),
                GoodsDetailsMXItem(
                        number = 1,
                        name = "987654321",
                        quantity = "15 ${productInfo.value!!.uom.name}",
                        even = 1 % 2 == 0,
                        productInfo = productInfo.value!!))

        )
    }

    private fun updateNotProcessed() {
        countedNotProssed.postValue(listOf(
                GoodsDetailsMXItem(
                        number = 1,
                        name = "000000",
                        quantity = "100 ${productInfo.value!!.uom.name}",
                        even = 1 % 2 == 0,
                        productInfo = productInfo.value!!)
        )
        )
    }


    fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

}

