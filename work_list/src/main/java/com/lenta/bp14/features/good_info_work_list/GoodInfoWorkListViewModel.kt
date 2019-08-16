package com.lenta.bp14.features.good_info_work_list

import com.lenta.shared.platform.viewmodel.CoreViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.data.model.Good
import com.lenta.shared.utilities.databinding.PageSelectionListener
import kotlinx.coroutines.launch

class GoodInfoWorkListViewModel : CoreViewModel(), PageSelectionListener {

    val good: MutableLiveData<Good> = MutableLiveData()

    val selectedPage = MutableLiveData(0)

    init {
        viewModelScope.launch {
            good.value = Good("000000000000254128", "Кукуруза")
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
