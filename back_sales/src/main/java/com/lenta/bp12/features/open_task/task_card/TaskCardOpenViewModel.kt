package com.lenta.bp12.features.open_task.task_card

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.ControlType
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.request.TaskContentNetRequest
import com.lenta.bp12.request.TaskContentParams
import com.lenta.bp12.request.UnblockTaskNetRequest
import com.lenta.bp12.request.UnblockTaskParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TaskCardOpenViewModel : CoreViewModel(), PageSelectionListener {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var manager: IOpenTaskManager

    @Inject
    lateinit var unblockTaskNetRequest: UnblockTaskNetRequest

    @Inject
    lateinit var deviceInfo: DeviceInfo

    @Inject
    lateinit var taskContentNetRequest: TaskContentNetRequest

    @Inject
    lateinit var appSettings: IAppSettings


    val title by lazy {
        "ТК - ${sessionInfo.market}"
    }

    val task by lazy {
        manager.currentTask
    }

    val selectedPage = MutableLiveData(0)

    val ui by lazy {
        task.map {
            it?.let { task ->
                TaskCardOpenUi(
                        name = task.name,
                        provider = task.getProviderCodeWithName(),
                        storage = task.storage,
                        reason = task.reason?.description ?: "",
                        description = task.type?.description ?: "",
                        comment = task.comment,
                        isStrict = task.isStrict,
                        isAlcohol = task.control == ControlType.ALCOHOL,
                        isCommon = task.control == ControlType.COMMON
                )
            }
        }
    }

    /**
    Методы
     */

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    private fun loadGoodList() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            taskContentNetRequest(TaskContentParams(
                    deviceIp = deviceInfo.getDeviceIp(),
                    taskNumber = task.value!!.number,
                    mode = 1,
                    userNumber = appSettings.lastPersonnelNumber ?: ""
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { taskContentResult ->
                viewModelScope.launch {
                    manager.addGoodsInCurrentTask(taskContentResult)
                    navigator.openGoodListScreen()
                }
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    /**
    Обработка нажатий кнопок
     */

    fun onClickNext() {
        if (task.value!!.goods.isEmpty()) {
            loadGoodList()
        } else {
            navigator.openGoodListScreen()
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            unblockTaskNetRequest(
                    UnblockTaskParams(
                            taskNumber = task.value!!.number,
                            userNumber = sessionInfo.personnelNumber ?: "",
                            deviceIp = deviceInfo.getDeviceIp()
                    )
            ).also {
                navigator.hideProgress()
            }

            navigator.goBack()
        }
    }

}

data class TaskCardOpenUi(
        val name: String,
        val provider: String,
        val storage: String,
        val reason: String,
        val description: String,
        val comment: String,
        val isStrict: Boolean,
        val isAlcohol: Boolean,
        val isCommon: Boolean
)