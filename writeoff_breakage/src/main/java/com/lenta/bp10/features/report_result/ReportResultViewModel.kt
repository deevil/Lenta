package com.lenta.bp10.features.report_result

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.lenta.bp10.platform.navigation.IScreenNavigator
import com.lenta.bp10.requests.network.SendWriteOffDataResult
import com.lenta.bp10.requests.network.pojo.CreatedTaskInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.asyncLiveData
import com.lenta.shared.utilities.extentions.mapSkipNulls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReportResultViewModel : CoreViewModel() {

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var screenNavigator: IScreenNavigator


    private val taskList = MutableLiveData(emptyList<CreatedTaskInfo>())

    val items = taskList.switchMap { list ->
        asyncLiveData<List<ItemTaskReport>> {
            val result = list.map {
                ItemTaskReport(
                        number = it.number.takeLast(6),
                        description = it.description
                )
            }

            emit(result)
        }
    }

    fun getMarket(): String {
        return sessionInfo.market.orEmpty()
    }

    fun setWriteOffReportResponse(sendWriteOffDataResult: SendWriteOffDataResult) {
        taskList.value = sendWriteOffDataResult.taskList
    }

    fun onNextClick() {
        screenNavigator.closeAllScreen()
        screenNavigator.openMainMenuScreen()
    }

}

data class ItemTaskReport(
        val number: String,
        val description: String
)
