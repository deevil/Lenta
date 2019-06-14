package com.lenta.inventory.features.goods_details_mx

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.goods_details.GoodsDetailsCategoriesItem
import com.lenta.inventory.features.goods_details.GoodsDetailsMXItem
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsDetailsMXViewModel : CoreViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val selectedPage = MutableLiveData(0)
    val countedCategories: MutableLiveData<List<GoodsDetailsCategoriesItem>> = MutableLiveData()
    val countedProssed: MutableLiveData<List<GoodsDetailsMXItem>> = MutableLiveData()
    val countedNotProssed: MutableLiveData<List<GoodsDetailsMXItem>> = MutableLiveData()
    val countedSelectionsHelper = SelectionItemsHelper()

    val deleteButtonEnabled: MutableLiveData<Boolean> = countedSelectionsHelper.selectedPositions.map {
        it?.isNotEmpty() ?: false
    }

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
    }

    init {
        viewModelScope.launch {
            //searchProductDelegate.init(viewModelScope = this@GoodsListViewModel::viewModelScope)
            updateCategories()
            updateNotProcessed()
            updateProcessed()
        }

    }

    fun onResume() {
        updateCategories()
        updateNotProcessed()
        updateProcessed()
    }

    private fun updateCategories() {
        countedCategories.postValue(listOf(GoodsDetailsCategoriesItem(
                number = 2,
                name = "Марочно",
                quantity = "12 ${productInfo.value!!.uom.name}",
                even = 2 % 2 == 0,
                productInfo = productInfo.value!!),
                GoodsDetailsCategoriesItem(
                        number = 1,
                        name = "Партионно",
                        quantity = "15 ${productInfo.value!!.uom.name}",
                        even = 1 % 2 == 0,
                        productInfo = productInfo.value!!))

        )
        countedSelectionsHelper.clearPositions()
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

    fun onClickDelete() {
        return
    }

}
