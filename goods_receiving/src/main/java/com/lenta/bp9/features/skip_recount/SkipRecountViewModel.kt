package com.lenta.bp9.features.skip_recount

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp9.R
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.SkipRecountNetRequest
import com.lenta.bp9.requests.network.SkipRecountParameters
import com.lenta.bp9.requests.network.SkipRecountResult
import com.lenta.bp9.requests.network.TaskContentsReceptionDistrCenterParameters
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.map
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class SkipRecountViewModel : CoreViewModel() {

    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var skipRecountNetRequest: SkipRecountNetRequest

    var taskNumber: MutableLiveData<String> = MutableLiveData("")

    val customComment: MutableLiveData<String> = MutableLiveData("")

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val enabledBtnSave: MutableLiveData<Boolean> = customComment.map {
        it?.isNotEmpty()
    }

    fun onClickSave() {
        viewModelScope.launch {
            screenNavigator.showProgress(context.getString(R.string.skipping_recount))
            val params = SkipRecountParameters(
                    taskNumber = taskNumber.value!!,
                    deviceIP = context.getDeviceIp(),
                    personalNumber = sessionInfo.personnelNumber ?: "",
                    comment = customComment.value!!
            )
            skipRecountNetRequest(params).either(::handleFailure, ::handleSuccess)
            screenNavigator.hideProgress()
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(result: SkipRecountResult) {
        val notifications = result.notifications.map { TaskNotification.from(it) }
        val sectionInfo = result.sectionsInfo.map { TaskSectionInfo.from(it) }
        val sectionProducts = result.sectionProducts.map { TaskSectionProducts.from(it) }
        taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
        taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(notifications, null, null, null)
        taskManager.getReceivingTask()?.taskRepository?.getSections()?.updateSections(sectionInfo, sectionProducts)
        if (taskManager.getReceivingTask()?.taskDescription?.isSpecialControlGoods == true) {
            //есть спецтовары
            screenNavigator.openTransferGoodsSectionScreen()
            screenNavigator.openAlertHaveIsSpecialGoodsScreen()
        } else {
            //нет спецтоваров
            screenNavigator.goBack()
            screenNavigator.openAlertNoIsSpecialGoodsScreen()
        }
    }

}
