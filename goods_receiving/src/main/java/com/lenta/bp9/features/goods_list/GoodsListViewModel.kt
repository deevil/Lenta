package com.lenta.bp9.features.goods_list

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener

class GoodsListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    val selectedPage = MutableLiveData(0)
    val countedSelectionsHelper = SelectionItemsHelper()
    val countedGoods: MutableLiveData<List<GoodsListCountedItem>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    fun getTitle(): String {
        return "???"
    }

    fun onScanResult(data: String) {
        return
    }

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickItemPosition(position: Int) {
        //todo
        /**if (selectedPage.value == 0) {
            countedGoods.value?.getOrNull(position)?.productInfo
        } else {
            filteredGoods.value?.getOrNull(position)?.productInfo
        }?.let {
            searchProductDelegate.openProductScreen(it, 0.0)
        }*/
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToEan.value = true
        eanCode.value = eanCode.value ?: "" + digit
    }

    override fun onOkInSoftKeyboard(): Boolean {
        eanCode.value?.let {
            //todo
            //searchProductDelegate.searchCode(it, fromScan = false)
        }
        return true
    }

    fun onClickGoodsTitle(position: Int) {
        //todo
        return
    }
}
