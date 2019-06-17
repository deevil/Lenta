package com.lenta.inventory.features.goods_details

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsDetailsViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()

    val selectedPage = MutableLiveData(0)
    val countedCategories: MutableLiveData<List<GoodsDetailsCategoriesItem>> = MutableLiveData()
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
        }

    }

    fun onResume() {
        updateCategories()
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

    fun onClickDelete() {
        return
    }

}
