package com.lenta.bp14.features.work_list.goods_list

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.data.model.Good
import com.lenta.shared.models.core.Uom
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.OnOkInSoftKeyboardListener
import com.lenta.shared.utilities.databinding.PageSelectionListener

class GoodsListWlViewModel : CoreViewModel(), PageSelectionListener, OnOkInSoftKeyboardListener {

    val selectedPage = MutableLiveData(0)

    val taskName = MutableLiveData("Рабочий список от 23.07.19 23:15")

    val goods = MutableLiveData<List<Good>>(getTestItems())

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    private fun getTestItems(): List<Good>? {
        return List(3) {
            Good(
                    id = it + 1,
                    material = "000000000000000021",
                    name = "Селедка",
                    uom = Uom.DEFAULT
            )
        }
    }

    override fun onOkInSoftKeyboard(): Boolean {
        return false
    }

    fun scanQrCode() {

    }

    fun scanBarCode() {

    }
}
