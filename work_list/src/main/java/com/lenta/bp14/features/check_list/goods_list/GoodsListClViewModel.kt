package com.lenta.bp14.features.check_list.goods_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener
import kotlinx.coroutines.launch

class GoodsListClViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {
    val deleteButtonEnabled = MutableLiveData(false)
    val saveButtonEnabled = MutableLiveData(false)
    val selectedPage = MutableLiveData(0)
    val searchCode = MutableLiveData("")
    val goods: MutableLiveData<List<GoodUiCl>> = MutableLiveData()


    init {
        viewModelScope.launch {
            goods.value = getTestGoods()
        }


    }

    private fun getTestGoods(): List<GoodUiCl>? {
        return List(100) {
            GoodUiCl(
                    number = it + 1,
                    name = "0000$it Селедка",
                    suffix = "шт",
                    uom = Uom.DEFAULT
            )
        }
    }


    fun getTitle(): String {
        return "???"
    }

    fun onClickDelete() {
    }

    fun onClickSave() {

    }

    override fun onPageSelected(position: Int) {


    }

    override fun onOkInSoftKeyboard(): Boolean {
        return true
    }
}


data class GoodUiCl(
        val number: Int,
        val name: String,
        val quantity: MutableLiveData<String> = MutableLiveData("1"),
        val suffix: String,
        val uom: Uom
)