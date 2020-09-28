package com.lenta.bp12.features.open_task.task_card

import com.lenta.bp12.managers.interfaces.IOpenTaskManager
import com.lenta.bp12.model.ControlType
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

    /**
     * ZMP_UTZ_BKS_03_V001
     * "Получение состава задания"
     **/
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
                        isAlcohol = task.controlTypes.contains(ControlType.ALCOHOL),
                        isCommon = task.controlTypes.contains(ControlType.COMMON),
                        isMark = task.controlTypes.contains(ControlType.MARK)
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

            taskContentNetRequest(
                    TaskContentParams(
                            deviceIp = deviceInfo.getDeviceIp(),
                            taskNumber = task.value?.number.orEmpty(),
                            mode = GET_GOOD_LIST_MODE,
                            userNumber = appSettings.lastPersonnelNumber.orEmpty()
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure, ::handleTaskContentResult)
        }
    }

    private fun handleTaskContentResult(result: TaskContentResult) {
        launchUITryCatch {
            navigator.showProgressLoadingData()
            manager.addTaskContentInCurrentTask(result)
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

    companion object {
        /** Режим работы: 1 - получение состава задания, 2 - получение состава задания с переблокировкой */
        private const val GET_GOOD_LIST_MODE = 1
    }

}