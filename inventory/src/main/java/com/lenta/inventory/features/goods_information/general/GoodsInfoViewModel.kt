package com.lenta.inventory.features.goods_information.general

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsInfoViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    //val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData(ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.General,
            false, "1", MatrixType.Active, "materialType"))

    val storagePlace: MutableLiveData<String> = MutableLiveData("123456789")
    val spinList: MutableLiveData<List<String>> = MutableLiveData()

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)

    val count: MutableLiveData<String> = MutableLiveData("")

    val countValue: MutableLiveData<Double> = count.map { it?.toDoubleOrNull()?: 0.0 }

    val suffix: MutableLiveData<String> = MutableLiveData()

    val totalCount: MutableLiveData<Double> = countValue.map {
        (it ?: 0.0)
    }

    val totalCountWithUom: MutableLiveData<String> = totalCount.map { "${it.toStringFormatted()} ${productInfo.value!!.uom.name}" }

    fun setProductInfo(productInfo: ProductInfo) {
        this.productInfo.value = productInfo
    }

    init {
        viewModelScope.launch {
            suffix.value = productInfo.value?.uom?.name
        }
    }

    fun onClickMissing() {
        return
    }

    fun onClickApply() {
        return
    }

    fun onClickDetails() {
        screenNavigator.openGoodsDetailsScreen()
    }

    override fun onClickPosition(position: Int) {
        return
    }
}
