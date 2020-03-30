package com.lenta.bp12.features.save_data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp12.model.ICreateTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.request.SendTaskDataNetRequest
import com.lenta.bp12.request.TaskData
import com.lenta.bp12.request.pojo.SentTaskInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class SaveDataViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var manager: ICreateTaskManager

    @Inject
    lateinit var sendTaskDataNetRequest: SendTaskDataNetRequest

    @Inject
    lateinit var deviceInfo: DeviceInfo


    val task by lazy {
        manager.currentTask
    }

    val title by lazy {
        "TK - ${sessionInfo.market}"
    }

    private val sentTaskInfoList = MutableLiveData<List<SentTaskInfo>>(null)

    val tasks = sentTaskInfoList.map { list ->
        list?.mapIndexed { index, sentTaskInfo ->
            SaveDataUi(
                    position = "${index + 1}",
                    name = sentTaskInfo.text1,
                    description = sentTaskInfo.text2
            )
        }
    }

    // -----------------------------

    init {
        viewModelScope.launch {
            sendTaskData()
        }
    }

    // -----------------------------

    private fun sendTaskData() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            sendTaskDataNetRequest(TaskData(
                    deviceIp = deviceInfo.getDeviceIp(),
                    tkNumber = sessionInfo.market ?: "Not found!",
                    userNumber = sessionInfo.personnelNumber ?: "Not found!",
                    task = task.value!!
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure) { sendTaskDataResult ->
                sentTaskInfoList.postValue(sendTaskDataResult.sentTasks)
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.goBack()
        navigator.openAlertScreen(failure)
    }

    fun onClickNext() {
        navigator.closeAllScreen()
        navigator.openMainMenuScreen()
    }

}

data class SaveDataUi(
        val position: String,
        val name: String,
        val description: String
)