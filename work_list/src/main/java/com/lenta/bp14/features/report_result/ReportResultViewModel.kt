package com.lenta.bp14.features.report_result

import androidx.lifecycle.MutableLiveData
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import javax.inject.Inject

class ReportResultViewModel : CoreViewModel() {

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager

    @Inject
    lateinit var navigator: IScreenNavigator

    val reports by lazy {
        MutableLiveData<List<ItemTaskReport>>(generalTaskManager.getLatestSentReportResult()?.createdTasks?.map {
            ItemTaskReport(
                    number = it.taskNumber,
                    description = it.text1
            )
        } ?: emptyList())
    }

    fun getMarket(): String {
        return sessionInfo.market!!
    }

    fun onNextClick() {
        generalTaskManager.clearCurrentTask()
        navigator.closeAllScreen()
        navigator.openMainMenuScreen()
    }
}

data class ItemTaskReport(val number: String, val description: String)
