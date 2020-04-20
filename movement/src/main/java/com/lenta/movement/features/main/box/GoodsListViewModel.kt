package com.lenta.movement.features.main.box

import androidx.lifecycle.MutableLiveData
import com.lenta.movement.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.SelectionItemsHelper
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import javax.inject.Inject

class GoodsListViewModel: CoreViewModel(), OnOkInSoftKeyboardListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val selectionsHelper = SelectionItemsHelper()

    val goodsList: MutableLiveData<List<GoodListItem>> = MutableLiveData()
    val eanCode: MutableLiveData<String> = MutableLiveData()
    val requestFocusToEan: MutableLiveData<Boolean> = MutableLiveData()

    fun onScanResult(data: String) {
        //TODO("Not yet implemented")
    }

    fun onResult(code: Int?) {
        //TODO("Not yet implemented")
    }

    override fun onOkInSoftKeyboard(): Boolean {
        TODO("Not yet implemented")
    }

    fun onClickItemPosition(position: Int) {
        //TODO("Not yet implemented")
    }

    fun onBackPressed() {
        screenNavigator.openUnsavedDataDialog(
            yesCallbackFunc = {
                screenNavigator.goBack()
                screenNavigator.goBack()
            }
        )
    }

    fun onDigitPressed(digit: Int) {
        requestFocusToEan.value = true
        eanCode.value = eanCode.value ?: "" + digit
    }

}