package com.lenta.inventory.features.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.shared.models.core.ProductInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.Evenable
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListViewModel : CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val unprocessedGoods: MutableLiveData<List<GoodItem>> = MutableLiveData()
    val processedGoods: MutableLiveData<List<GoodItem>> = MutableLiveData()
    var selectedPage = MutableLiveData(0)

    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    val deleteEnabled: MutableLiveData<Boolean> = selectedPage.map { it ?: 0 != 0 }

    fun getTitle(): String {
        return "Номер задания - тип задания"
    }

    init {
        viewModelScope.launch {
            updateUnprocessed()
            updateProcessed()
        }

    }

    fun onResume() {
        updateUnprocessed()
        updateProcessed()
    }

    fun updateProcessed() {
        val goodItem = GoodItem(1, "Good Processed Good", "Што шт.", false)
        val badItem = GoodItem(2, "Bad Processed Good", "Што шт.", true)
        val uglyItem = GoodItem(3, "Ugly Processed Good", "Што шт.", false)
        processedGoods.postValue(listOf(goodItem, badItem, uglyItem))
    }

    fun updateUnprocessed() {
        val goodItem = GoodItem(1, "Good Good", "Што шт.", false)
        val badItem = GoodItem(2, "Bad Good", "Што шт.", true)
        val uglyItem = GoodItem(3, "Ugly Good", "Што шт.", false)
        unprocessedGoods.postValue(listOf(goodItem, badItem, uglyItem))
    }

    fun onClickClean() {
        return
    }

    fun onClickComplete() {
        return
    }

    fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onDoubleClickPosition(position: Int) {
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToEan.value = true
        eanCode.value = eanCode.value ?: "" + digit
    }

    fun onScanResult(data: String) {
        eanCode.value = data
    }

    override fun onOkInSoftKeyboard(): Boolean {


        return true
    }
}

data class GoodItem(
        val number: Int,
        val name: String,
        val quantity: String,
        val even: Boolean
//        val productInfo: ProductInfo
) : Evenable {
    override fun isEven() = even
}