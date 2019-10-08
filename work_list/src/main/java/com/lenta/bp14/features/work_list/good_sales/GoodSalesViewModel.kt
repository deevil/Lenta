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
                    daySales = "${sales?.daySales.dropZeros()} ${task.currentGood.value!!.getUnits()}",
                    weekSales = "${sales?.weekSales.dropZeros()} ${task.currentGood.value!!.getUnits()}"
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
                            tkNumber = sessionInfo.market ?: "Not Found!",
                            material = task.currentGood.value?.common?.material ?: ""
                    )
            ).either(::handleFailure) { result -> updateSales(result) }
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
                    lastSaleDate = DateTimeUtil.getDateFromString("${sales.lastSaleDate}_${sales.lastSaleTime}", Constants.DATE_TIME_ONE),
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
