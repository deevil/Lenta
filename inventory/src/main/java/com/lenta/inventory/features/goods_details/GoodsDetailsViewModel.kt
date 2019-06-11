package com.lenta.inventory.features.goods_details

import androidx.lifecycle.MutableLiveData
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.Evenable
import javax.inject.Inject

class GoodsDetailsViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val selectedPage = MutableLiveData(0)
    val countedGoods: MutableLiveData<List<GoodsItem>> = MutableLiveData()
    val countedSelectionsHelper = SelectionItemsHelper()

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
    }

    fun onDoubleClickPosition(position: Int) {
        if (selectedPage.value == 0) {
            countedGoods.value?.getOrNull(position)?.productInfo
        } else {
            countedGoods.value?.getOrNull(position)?.productInfo
        }?.let {
            screenNavigator.openGoodsInfoScreen() //openGoodsInfoScreen(it)
        }
    }

    fun onClickDelete() {
        Logg.d { "INV_onClickDelete" }
        return
    }

}

data class GoodsItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val even: Boolean,
        val productInfo: ProductInfo
) : Evenable {
    override fun isEven() = even

}
