package com.lenta.bp14.features.work_list.good_sales

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.getFormattedDate
import com.lenta.shared.utilities.extentions.getFormattedTime
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodSalesViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var task: WorkListTask


    val title = MutableLiveData<String>("")

    val sales: MutableLiveData<SalesStatisticsUi> by lazy {
        task.sales.map { sales ->
            SalesStatisticsUi(
                    lastSaleDate = sales?.lastSaleDate.getFormattedDate(),
                    lastSaleTime = sales?.lastSaleDate.getFormattedTime(),
                    daySales = sales?.getDaySalesWithUnits() ?: "",
                    weekSales = sales?.getWeekSalesWithUnits() ?: ""
            )
        }
    }

    init {
        viewModelScope.launch {
            title.value = task.currentGood.value?.getFormattedMaterialWithName()
            onClickUpdate()
        }
    }

    fun onClickUpdate() {
        viewModelScope.launch {
            task.loadSalesStatistics()
        }
    }

}

data class SalesStatisticsUi(
        val lastSaleDate: String,
        val lastSaleTime: String,
        val daySales: String,
        val weekSales: String
)
