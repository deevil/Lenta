package com.lenta.bp14.features.good_info_work_list

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.model.Good
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch

class GoodInfoWorkListViewModel : CoreViewModel(), PageSelectionListener {

    val good: MutableLiveData<Good> = MutableLiveData()

    val quantity: MutableLiveData<String> = MutableLiveData()

    val totalQuantity: MutableLiveData<Int> = quantity.map {
        val currentQuantity = if (it?.isNotEmpty() == true) it.toInt() else 0
        val goodQuantity = if(good.value != null) good.value!!.total else 0
        currentQuantity + goodQuantity
    }

    val selectedPage = MutableLiveData(0)

    init {
        viewModelScope.launch {
            good.value = Good(
                    material = "000000000000254128",
                    name = "Кукуруза",
                    total = 5
            )

            quantity.value = "1"
        }
    }


    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    fun onClickBtn1() {

    }

    fun onClickBtn2() {

    }

    fun onClickBtn3() {

    }

    fun onClickBtn4() {

    }

    fun onClickBtn5() {

    }

    fun onClickBtn6() {

    }

    fun onClickBtn7() {

    }

}
