package com.lenta.bp14.features.work_list.sales_of_goods

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch

class SalesOfGoodsViewModel : CoreViewModel() {

    val lastSalesDate: MutableLiveData<String> = MutableLiveData()
    val lastSalesTime: MutableLiveData<String> = MutableLiveData()
    val lastDaySales: MutableLiveData<String> = MutableLiveData()
    val lastWeekSales: MutableLiveData<String> = MutableLiveData()


    init {
        viewModelScope.launch {
            lastSalesDate.value = "29.07.2019"
            lastSalesTime.value = "12:15"
            lastDaySales.value = "150 шт."
            lastWeekSales.value = "1500 шт."
        }

    }



    fun getTitle(): String? {
        return "???"
    }

    fun onClickUpdate() {


    }

}
