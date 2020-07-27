package com.lenta.bp9.features.skip_recount

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.R
import com.lenta.bp9.features.loading.tasks.TaskCardMode
import com.lenta.bp9.model.task.*
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.requests.network.SkipRecountNetRequest
import com.lenta.bp9.requests.network.SkipRecountParameters
import com.lenta.bp9.requests.network.SkipRecountResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.mobrun.plugin.api.HyperHive
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
    @Inject
    lateinit var hyperHive: HyperHive

    val customComment: MutableLiveData<String> = MutableLiveData("")

    val taskCaption: String by lazy {
        taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    val enabledBtnSave: MutableLiveData<Boolean> = customComment.map {
        it?.isNotEmpty()
    }

    fun onClickSave() {
        launchUITryCatch {
            screenNavigator.showProgress(context.getString(R.string.skipping_recount))
            val params = SkipRecountParameters(
                    taskNumber = taskManager.getReceivingTask()?.taskHeader?.taskNumber ?: "",
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
        launchUITryCatch {
            val notifications = result.notifications.map { TaskNotification.from(it) }
            val sectionInfo = result.sectionsInfo.map { TaskSectionInfo.from(it) }
            val sectionProducts = result.sectionProducts.map { TaskSectionProducts.from(hyperHive, it) }
            taskManager.updateTaskDescription(TaskDescription.from(result.taskDescription))
            taskManager.getReceivingTask()?.taskRepository?.getNotifications()?.updateWithNotifications(notifications, null, null, null)
            taskManager.getReceivingTask()?.taskRepository?.getSections()?.updateSections(sectionInfo, sectionProducts)
            if (taskManager.getReceivingTask()?.taskDescription?.isSpecialControlGoods == true) {
                //есть спецтовары
                screenNavigator.openTransferGoodsSectionScreen()
                screenNavigator.openAlertHaveIsSpecialGoodsScreen()
            } else {
                //нет спецтоваров
                screenNavigator.openMainMenuScreen()
                screenNavigator.openTaskListScreen()
                screenNavigator.openTaskCardScreen(TaskCardMode.Full, taskManager.getReceivingTask()?.taskHeader?.taskType ?: TaskType.None)
                screenNavigator.openAlertNoIsSpecialGoodsScreen()
            }
        }
    }

}
