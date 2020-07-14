package com.lenta.bp10.features.report_result

import androidx.lifecycle.MutableLiveData
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.network.WriteOffReportResponse
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class ReportResultViewModel : CoreViewModel() {
    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var screenNavigator: IScreenNavigator

    val items = MutableLiveData<List<ItemTaskReport>>()


    fun getMarket(): String {
        return sessionInfo.market.orEmpty()
    }

    fun setWriteOffReportResponse(writeOffReportResponse: WriteOffReportResponse) {
        items.value = writeOffReportResponse.taskList.map {
            ItemTaskReport(
                    number = it.number.takeLast(6),
                    description = it.description
            )
        }

    }

    fun onNextClick() {
        screenNavigator.closeAllScreen()
        screenNavigator.openMainMenuScreen()
    }
}

data class ItemTaskReport(val number: String, val description: String)
