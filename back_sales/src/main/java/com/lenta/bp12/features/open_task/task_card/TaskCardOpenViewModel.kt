package com.lenta.bp12.features.open_task.task_card

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.model.IOpenTaskManager
import com.lenta.bp12.platform.extention.isAlcohol
import com.lenta.bp12.platform.extention.isCommon
import com.lenta.bp12.platform.extention.isMark
import com.lenta.bp12.platform.extention.isWholesaleType
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.request.*
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.device_info.DeviceInfo
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.databinding.PageSelectionListener
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.orIfNull
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

    @Inject
    lateinit var resource: IResourceManager


    val title by lazy {
        resource.tk(sessionInfo.market.orEmpty())
    }

    val task by lazy {
        manager.currentTask
    }

    val selectedPage = MutableLiveData(0)

    val ui by lazy {
        task.map {
            it?.let { task ->
                val provider = task.getProviderCodeWithName().takeIf { codeWithName ->
                    codeWithName.isNotEmpty()
                } ?: resource.wholesaleBuyer()

                TaskCardOpenUi(
                        name = task.name,
                        provider = provider,
                        storage = task.storage,
                        reason = task.reason?.description.orEmpty(),
                        description = task.type?.description.orEmpty(),
                        comment = task.comment,
                        isStrict = task.isStrict,
                        isAlcohol = task.control.isAlcohol(),
                        isCommon = task.control.isCommon(),
                        isMark = task.control.isMark()
                )
            }
        }
    }

    val isExistComment by lazy {
        task.map {
            it?.comment?.isNotEmpty() ?: false
        }
    }

    /**
    Блок инициализации
     */

    init {
        launchUITryCatch {
            manager.isWholesaleTaskType = task.value?.type?.isWholesaleType() == true
        }
    }

    /**
    Методы
     */

    override fun onPageSelected(position: Int) {
        selectedPage.value = position
    }

    private fun loadGoodList() {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)

            taskContentNetRequest(TaskContentParams(
                    deviceIp = deviceInfo.getDeviceIp(),
                    taskNumber = task.value?.number.orEmpty(),
                    mode = 1,
                    userNumber = appSettings.lastPersonnelNumber.orEmpty()
            )).also {
                navigator.hideProgress()
            }.either(::handleFailure, ::handleTaskContentResult)
        }
    }

    private fun handleTaskContentResult(result: TaskContentResult) {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            manager.addGoodsInCurrentTask(result)
            navigator.hideProgress()

            openGoodListScreen()
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    private fun openGoodListScreen() {
        if (!manager.isExistStartTaskInfo()) {
            manager.saveStartTaskInfo()
        }

        navigator.openGoodListScreen()
    }

    /**
    Обработка нажатий кнопок
     */

    fun onClickNext() {
        val goodList = task.value?.goods.orEmpty()
        if (goodList.isEmpty()) {
            loadGoodList()
        } else {
            openGoodListScreen()
        }
    }

    fun onBackPressed() {
        task.value?.let { task ->
            if (manager.isTaskWasChanged()) {
                navigator.showTaskUnsentDataWillBeDeleted(task.name) {
                    manager.clearCurrentTask()
                    unblockTaskAndExit(task.number)
                }
            } else {
                unblockTaskAndExit(task.number)
            }
        }.orIfNull {
            Logg.e { "task null" }
            navigator.showInternalError(resource.taskNotFoundErrorMsg)
        }
    }

    private fun unblockTaskAndExit(taskNumber: String) {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)

            unblockTaskNetRequest(
                    UnblockTaskParams(
                            taskNumber = taskNumber,
                            userNumber = sessionInfo.personnelNumber.orEmpty(),
                            deviceIp = deviceInfo.getDeviceIp()
                    )
            ).also {
                navigator.hideProgress()
            }

            manager.clearStartTaskInfo()
            navigator.goBack()
        }
    }

}