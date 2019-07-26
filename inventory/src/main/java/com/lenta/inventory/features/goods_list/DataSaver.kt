package com.lenta.inventory.features.goods_list

import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.InvSendReportNetRequest
import com.lenta.shared.exception.Failure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class DataSaver @Inject constructor(
        private val screenNavigator: IScreenNavigator,
        private val taskManager: IInventoryTaskManager,
        private val invSendReportNetRequest: InvSendReportNetRequest) {

    private lateinit var viewModelScope: () -> CoroutineScope

    fun saveData() {
        viewModelScope().launch {
            screenNavigator.showProgress(invSendReportNetRequest)
            taskManager.getInventoryTask()?.let { task ->
                invSendReportNetRequest(task.getReport(isFinish = true)).either(::handleFailure) {
                    if (it.retCode != "0") {
                        screenNavigator.openInfoScreen(it.errorText)
                    } else {
                        screenNavigator.openSuccessSaveDataScreen()
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