package com.lenta.inventory.features.goods_details_mx

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.goods_details.GoodsDetailsItem
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsDetailsMXViewModel : CoreViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val selectedPage = MutableLiveData(0)
    val countedCategories: MutableLiveData<List<GoodsDetailsItem>> = MutableLiveData()
    val countedMX: MutableLiveData<List<GoodsDetailsMXItem>> = MutableLiveData()
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
            //updateNotProcessed()
        }

    }

    fun onResume() {
        updateCategories()
        //updateNotProcessed()
    }

    private fun updateCategories() {
        countedCategories.postValue(listOf(GoodsDetailsItem(
                number = 2,
                name = "Марочно",
                quantity = "12 ${productInfo.value!!.uom.name}",
                even = 2 % 2 == 0,
                productInfo = productInfo.value!!),
                GoodsDetailsItem(
                        number = 1,
                        name = "Партионно",
                        quantity = "15 ${productInfo.value!!.uom.name}",
                        even = 1 % 2 == 0,
                        productInfo = productInfo.value!!))

        )
        countedSelectionsHelper.clearPositions()
    }

    private fun updateNotProcessed() {
        countedMX.postValue(listOf(
                GoodsDetailsMXItem(
                        number = 2,
                        name = "sf",
                        quantity = "12 ${productInfo.value!!.uom.name}",
                        even = 2 % 2 == 0,
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

data class GoodsDetailsMXItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val even: Boolean,
        val productInfo: ProductInfo
) : Evenable {
    override fun isEven() = even

}
