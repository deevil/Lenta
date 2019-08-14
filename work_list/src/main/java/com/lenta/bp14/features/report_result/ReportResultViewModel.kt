package com.lenta.bp14.features.report_result

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

class ReportResultViewModel : CoreViewModel() {

    @Inject
    lateinit var sessionInfo: ISessionInfo

    val reports = MutableLiveData<List<ItemTaskReport>>()

    fun getMarket(): String {
        return sessionInfo.market!!
    }

    fun onNextClick() {

    }

    init {
        viewModelScope.launch {
            reports.value = getTestData()
        }

    }

    private fun getTestData(): List<ItemTaskReport>? {
        return List(100) {
            ItemTaskReport(
                    "${it + 1}",
                    "Отчет $it"
            )
        }
    }

}

data class ItemTaskReport(val number: String, val description: String)
