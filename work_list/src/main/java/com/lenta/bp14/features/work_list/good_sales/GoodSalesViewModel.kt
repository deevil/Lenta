package com.lenta.bp14.features.work_list.good_sales

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp14.models.work_list.SalesStatistics
import com.lenta.bp14.models.work_list.WorkListTask
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.requests.work_list.GoodSalesParams
import com.lenta.bp14.requests.work_list.GoodSalesResult
import com.lenta.bp14.requests.work_list.IGoodSalesNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.constants.Constants
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.date_time.DateTimeUtil
import com.lenta.shared.utilities.extentions.*
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodSalesViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var task: WorkListTask
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var goodSalesNetRequest: IGoodSalesNetRequest


    val title = MutableLiveData<String>("")

    val sales: MutableLiveData<SalesStatisticsUi> by lazy {
        task.currentGood.value!!.sales.map { sales ->
            SalesStatisticsUi(
                    lastSaleDate = sales?.lastSaleDate.getFormattedDate(),
                    lastSaleTime = sales?.lastSaleDate.getFormattedTime(),
                    daySales = "${sales?.daySales.dropZeros()} ${task.currentGood.value!!.units.name}",
                    weekSales = "${sales?.weekSales.dropZeros()} ${task.currentGood.value!!.units.name}"
            )
        }
    }

    // -----------------------------

    init {
        viewModelScope.launch {
            title.value = task.currentGood.value?.getFormattedMaterialWithName()
            onClickUpdate()
        }
    }

    // -----------------------------

    fun onClickUpdate() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()
            goodSalesNetRequest(
                    GoodSalesParams(
                            tkNumber = sessionInfo.market.orEmpty(),
                            material = task.currentGood.value?.material.orEmpty()
                    )
            ).either(::handleFailure, ::updateSales)
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.hideProgress()
        navigator.openAlertScreen(failure)
    }

    private fun updateSales(result: GoodSalesResult) {
        Logg.d { "GoodSalesResult: $result" }
        viewModelScope.launch {
            val sales = result.sales.first()
            task.currentGood.value?.sales?.value = SalesStatistics(
                    lastSaleDate = "${sales.lastSaleDate}_${sales.lastSaleTime}".getDate(Constants.DATE_TIME_ONE),
                    daySales = sales.daySales,
                    weekSales = sales.weekSales
            )
        }
        navigator.hideProgress()
    }

}

data class SalesStatisticsUi(
        val lastSaleDate: String,
        val lastSaleTime: String,
        val daySales: String,
        val weekSales: String
)
