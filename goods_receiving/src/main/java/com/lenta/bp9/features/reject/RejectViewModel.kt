package com.lenta.bp9.features.reject

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.features.loading.tasks.TaskListLoadingMode
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.requests.network.RejectNetRequest
import com.lenta.bp9.requests.network.RejectRequestParameters
import com.lenta.bp9.requests.network.RejectRequestResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo
import com.lenta.shared.utilities.extentions.combineLatest
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.view.OnPositionClickListener
import kotlinx.coroutines.launch
import javax.inject.Inject

class RejectViewModel : CoreViewModel(), OnPositionClickListener {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var rejectRequest: RejectNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var dataBase: IDataBaseRepo

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val failureReasons: MutableLiveData<List<String>> = MutableLiveData()
    private val failureReasonsInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    val customComment: MutableLiveData<String> = MutableLiveData("")

    val customReasonEnabled: MutableLiveData<Boolean> = selectedPosition.map { it == 0 }
    val buttonsEnabled: MutableLiveData<Boolean> = customReasonEnabled.combineLatest(customComment).map {
        !it?.second.isNullOrEmpty() || it?.first == false
    }

    private var currentRejectionType: RejectType? = null

    init {
        viewModelScope.launch {
            failureReasonsInfo.value = dataBase.getFailureReasons()
            failureReasons.value = failureReasonsInfo.value?.map {
                it.name
            }
        }
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }

    fun onClickFull() {
        rejectWithType(RejectType.Full)
    }

    fun onClickTemporary() {
        rejectWithType(RejectType.Temporary)
    }

    private fun getRejectString(): String {
        return if (failureReasonsInfo.value!![selectedPosition.value!!].code == "1") {
            customComment.value ?: ""
        } else {
            failureReasonsInfo.value!![selectedPosition.value!!].name
        }
    }

    private fun rejectWithType(type: RejectType) {
        currentRejectionType = type
        val params = RejectRequestParameters(
                deviceIP = context.getDeviceIp(),
                personalNumber = sessionInfo.personnelNumber ?: "",
                taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
                rejectMode = type.rejectTypeString,
                rejectReason = getRejectString()
        )
        viewModelScope.launch {
            screenNavigator.showProgress(rejectRequest)
            rejectRequest(params).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
        currentRejectionType = null
    }

    private fun goToTaskList() {
        screenNavigator.openMainMenuScreen()
        screenNavigator.openTaskListLoadingScreen(TaskListLoadingMode.Receiving)
    }

    fun handleSuccess(result: RejectRequestResult) {
        when (currentRejectionType) {
            RejectType.Temporary -> screenNavigator.openAlertWithoutConfirmation(context.getString(R.string.reject_success_temp)) { goToTaskList()}
            RejectType.Full -> {
                if (taskManager.getReceivingTask()?.taskDescription?.isAlco == true) {
                    screenNavigator.openAlertWithoutConfirmation(context.getString(R.string.reject_success_full_alco)) { goToTaskList()}
                } else {
                    screenNavigator.openAlertWithoutConfirmation(context.getString(R.string.reject_success_full)) { goToTaskList()}
                }
            }
        }
        currentRejectionType = null
    }
}

enum class RejectType(val rejectTypeString: String) {
    Temporary("1"),
    Full("2")
}
