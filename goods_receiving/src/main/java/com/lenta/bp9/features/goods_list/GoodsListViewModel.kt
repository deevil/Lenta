package com.lenta.bp9.features.goods_list

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.platform.navigation.ScreenNavigator
import com.lenta.bp9.requests.network.DirectSupplierStarRecountParams
import com.lenta.bp9.requests.network.DirectSupplierStarRecountRestInfo
import com.lenta.bp9.requests.network.DirectSupplierStartRecountNetRequest
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodsListViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var directSupplierStartRecountNetRequest: DirectSupplierStartRecountNetRequest

    val selectedPage = MutableLiveData(0)
    val countedSelectionsHelper = SelectionItemsHelper()
    val countedGoods: MutableLiveData<List<GoodsListCountedItem>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()
    val titleProgressScreen: MutableLiveData<String> = MutableLiveData()

    val visibilityCleanButton: MutableLiveData<Boolean> = selectedPage.map {
        it == 1
    }

    init {
        //todo удалить, данные должен формировать Антон
        testData()
    }

    //todo удалить
    private fun testData() {
        viewModelScope.launch {
            screenNavigator.showProgress(titleProgressScreen.value!!)
            directSupplierStartRecountNetRequest(
                    DirectSupplierStarRecountParams(
                            taskNumber = "46808",
                            ip = "192.168.100.101",
                            personnelNumber = "",
                            dateRecount = "",
                            timeRecount = "",
                            unbindVSD = ""
                    )).either(::handleFailure, ::testDataSuccess)
            screenNavigator.hideProgress()
        }
    }

    //todo удалить
    private fun testDataSuccess(restInfo: DirectSupplierStarRecountRestInfo) {
        Logg.d { "test ${restInfo.taskComposition.size}" }

    }

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
