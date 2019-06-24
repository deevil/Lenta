package com.lenta.inventory.features.goods_information.sets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.goods_details.ComponentItem
import com.lenta.inventory.features.goods_details.GoodsDetailsCategoriesItem
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.MatrixType
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.models.core.ProductType
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.toStringFormatted
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class SetsInfoViewModel : CoreViewModel(), OnPositionClickListener, OnOkInSoftKeyboardListener {
    @Inject
    lateinit var screenNavigator: IScreenNavigator

    //val productInfo: MutableLiveData<ProductInfo> = MutableLiveData()
    val productInfo: MutableLiveData<ProductInfo> = MutableLiveData(ProductInfo("materialNumber1", "description", Uom("ST", "шт"), ProductType.ExciseAlcohol,
            true, "1", MatrixType.Active, "materialType"))

    var selectedPage = MutableLiveData(0)

    val componentsItem: MutableLiveData<List<ComponentItem>> = MutableLiveData()

    val componentsSelectionsHelper = SelectionItemsHelper()

    val eanCode: MutableLiveData<String> = MutableLiveData()

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

    fun onResume() {
        return
        //updateComponents()
    }

    fun onClickMissing() {
        //todo
        screenNavigator.openAlertScreen("onClickMissing")
    }

    fun onClickApply() {
        //todo
        screenNavigator.openAlertScreen("onClickApply")
    }

    fun onClickDetails() {
        //todo
        screenNavigator.openAlertScreen("onClickDetails")
    }

    fun onClickClean() {
        //todo
        screenNavigator.openAlertScreen("onClickClean")
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
        //updateComponents()
    }
    fun onBackPressed() {
        return
        //processExciseAlcoProductService.discard()
    }

    override fun onOkInSoftKeyboard(): Boolean {
        //searchEANCode()
        return true
    }

    override fun onClickPosition(position: Int) {
        return
    }
}
