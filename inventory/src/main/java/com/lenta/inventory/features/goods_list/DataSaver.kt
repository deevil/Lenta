package com.lenta.inventory.features.goods_list

import android.content.Context
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.InvSendReportNetRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.utilities.extentions.getDeviceIp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class DataSaver @Inject constructor(
        private val screenNavigator: IScreenNavigator,
        private val taskManager: IInventoryTaskManager,
        private val context: Context,
        private val sessionInfo: ISessionInfo,
        private val invSendReportNetRequest: InvSendReportNetRequest) {

    private lateinit var viewModelScope: () -> CoroutineScope

    fun setViewModelScopeFunc(viewModelScope: () -> CoroutineScope) {
        this.viewModelScope = viewModelScope
    }

    fun saveData(final: Boolean) {
        viewModelScope().launch {
            screenNavigator.showProgress(invSendReportNetRequest)
            taskManager.getInventoryTask()?.let { task ->
                invSendReportNetRequest(
                        task.getReport(
                                isFinish = final,
                                ip = context.getDeviceIp(),
                                personnelNumber = if (final) sessionInfo.personnelNumber!! else "",
                                isRecount = task.taskDescription.isRecount
                        )).either(::handleFailure) {
                    taskManager.clearTask()

                    screenNavigator.openSuccessSaveDataScreen() {
                        screenNavigator.closeAllScreen()
                        screenNavigator.openTasksList()
                    }
                }
            }
            screenNavigator.hideProgress()
        }
    }

    private fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }
}