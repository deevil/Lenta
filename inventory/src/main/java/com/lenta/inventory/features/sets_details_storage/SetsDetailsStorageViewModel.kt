package com.lenta.inventory.features.sets_details_storage

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.goods_details.GoodsDetailsStorageItem
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetsDetailsStorageViewModel : CoreViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val selectedPage = MutableLiveData(0)
    val countedProssed: MutableLiveData<List<GoodsDetailsStorageItem>> = MutableLiveData()
    val countedNotProssed: MutableLiveData<List<GoodsDetailsStorageItem>> = MutableLiveData()

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
        countedProssed.postValue(listOf(GoodsDetailsStorageItem(
                number = 2,
                name = "123456789",
                quantity = "12 ${productInfo.value!!.uom.name}",
                even = 2 % 2 == 0,
                productInfo = productInfo.value!!),
                GoodsDetailsStorageItem(
                        number = 1,
                        name = "987654321",
                        quantity = "15 ${productInfo.value!!.uom.name}",
                        even = 1 % 2 == 0,
                        productInfo = productInfo.value!!))

        )
    }

    private fun updateNotProcessed() {
        countedNotProssed.postValue(listOf(
                GoodsDetailsStorageItem(
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

