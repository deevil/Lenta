package com.lenta.bp14.features.loading.fast

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app_update.AppUpdateInstaller
import com.google.gson.Gson
import com.lenta.bp14.models.IGeneralTaskManager
import com.lenta.bp14.models.ITask
import com.lenta.bp14.models.ITaskDescription
import com.lenta.bp14.models.ITaskManager
import com.lenta.bp14.models.check_list.CheckListData
import com.lenta.bp14.models.check_list.CheckListTaskManager
import com.lenta.bp14.models.check_price.CheckPriceData
import com.lenta.bp14.models.check_price.CheckPriceTaskManager
import com.lenta.bp14.models.general.AppTaskTypes
import com.lenta.bp14.models.general.IGeneralRepo
import com.lenta.bp14.models.not_exposed.NotExposedData
import com.lenta.bp14.models.not_exposed.NotExposedTaskManager
import com.lenta.bp14.models.work_list.WorkListData
import com.lenta.bp14.models.work_list.WorkListTaskManager
import com.lenta.bp14.platform.navigation.IScreenNavigator
import com.lenta.bp14.repos.IRepoInMemoryHolder
import com.lenta.bp14.requests.FastResourcesMultiRequest
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.exception.IFailureInterpreter
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.functional.Either
import com.lenta.shared.platform.app_update.AppUpdateChecker
import com.lenta.shared.platform.resources.ISharedStringResourceManager
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.requests.network.Auth
import com.lenta.shared.requests.network.ServerTime
import com.lenta.shared.requests.network.ServerTimeRequest
import com.lenta.shared.requests.network.ServerTimeRequestParam
import com.lenta.shared.utilities.Logg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FastLoadingViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var failureInterpreter: IFailureInterpreter
    @Inject
    lateinit var fastResourcesNetRequest: FastResourcesMultiRequest
    @Inject
    lateinit var appUpdateChecker: AppUpdateChecker
    @Inject
    lateinit var generalRepo: IGeneralRepo
    @Inject
    lateinit var auth: Auth
    @Inject
    lateinit var appUpdateInstaller: AppUpdateInstaller
    @Inject
    lateinit var resourceManager: ISharedStringResourceManager
    @Inject
    lateinit var repoInMemoryHolder: IRepoInMemoryHolder
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager
    @Inject
    lateinit var gson: Gson
    @Inject
    lateinit var checkListTaskManager: CheckListTaskManager
    @Inject
    lateinit var workListTaskManager: WorkListTaskManager
    @Inject
    lateinit var checkPriceTaskManager: CheckPriceTaskManager
    @Inject
    lateinit var notExposedTaskManager: NotExposedTaskManager
    @Inject
    lateinit var serverTimeRequest: ServerTimeRequest
    @Inject
    lateinit var timeMonitor: ITimeMonitor


    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            withContext(Dispatchers.IO) {
                repoInMemoryHolder.storesRequestResult?.markets?.find { it.tkNumber == sessionInfo.market }.let { market ->
                    val codeVersion = market?.version?.toIntOrNull()
                    Logg.d { "codeVersion for update: $codeVersion" }
                    if (codeVersion == null) {
                        Either.Right("")
                    } else {
                        appUpdateInstaller.checkNeedAndHaveUpdate(codeVersion)
                    }
                }
            }.either({
                Logg.e { "checkNeedAndHaveUpdate failure: $it" }
                handleFailure(failure = it)
            }) { updateFileName ->
                Logg.d { "update fileName: $updateFileName" }
                if (updateFileName.isBlank()) {
                    getServerTime()
                } else {
                    installUpdate(updateFileName)
                }
            }
        }
    }

    private fun installUpdate(updateFileName: String) {
        viewModelScope.launch {
            title.value = resourceManager.loadingNewAppVersion()
            progress.value = true
            withContext(Dispatchers.IO) {
                appUpdateInstaller.installUpdate(updateFileName)
            }.either(::handleFailure) {
                // do nothing. App is finished
            }
        }

    }

    private fun getServerTime() {
        viewModelScope.launch {
            serverTimeRequest(ServerTimeRequestParam(sessionInfo.market
                   .orEmpty())).either(::handleFailure, ::handleSuccessServerTime)
        }
    }

    private fun handleSuccessServerTime(serverTime: ServerTime) {
        timeMonitor.setServerTime(time = serverTime.time, date = serverTime.date)
        viewModelScope.launch {
            fastResourcesNetRequest(null).either(::handleFailure, ::handleSuccess)
        }
    }

    override fun handleFailure(failure: Failure) {
        navigator.openLoginScreen()
        navigator.openAlertScreen(failureInterpreter.getFailureDescription(failure).message)
        progress.postValue(false)
    }

    private fun handleSuccess(notUsed: Boolean) {
        viewModelScope.launch {
            generalRepo.onDbReady()
            if (generalTaskManager.isExistSavedTaskData()) {
                navigator.showUnsavedDataFoundOnDevice(
                        deleteCallback = {
                            generalTaskManager.clearSavedTaskData()
                            navigator.openMainMenuScreen()
                        },
                        goOverCallback = {
                            restoreSavedTask()
                        }
                )
            } else {
                navigator.openMainMenuScreen()
            }
            progress.value = false
        }
    }

    override fun clean() {
        progress.postValue(false)
    }

    private fun restoreSavedTask() {
        generalTaskManager.getSavedData()?.let { taskData ->
            when (taskData.taskType) {
                AppTaskTypes.CheckPrice.taskType -> {
                    val data = gson.fromJson(taskData.data, CheckPriceData::class.java)
                    newTask(
                            taskName = data.taskDescription.taskName,
                            taskManager = checkPriceTaskManager,
                            taskDescription = data.taskDescription
                    )
                }

                AppTaskTypes.CheckList.taskType -> {
                    val data = gson.fromJson(taskData.data, CheckListData::class.java)
                    newTask(
                            taskName = data.taskDescription.taskName,
                            taskManager = checkListTaskManager,
                            taskDescription = data.taskDescription
                    )
                }

                AppTaskTypes.WorkList.taskType -> {
                    val data = gson.fromJson(taskData.data, WorkListData::class.java)
                    newTask(
                            taskName = data.taskDescription.taskName,
                            taskManager = workListTaskManager,
                            taskDescription = data.taskDescription
                    )
                }

                AppTaskTypes.NotExposedProducts.taskType -> {
                    val data = gson.fromJson(taskData.data, NotExposedData::class.java)
                    newTask(
                            taskName = data.taskDescription.taskName,
                            taskManager = notExposedTaskManager,
                            taskDescription = data.taskDescription
                    )
                }

                else -> navigator.openNotImplementedScreenAlert("")
            }
        }

    }

    private fun <S : ITask, D : ITaskDescription> newTask(taskName: String, taskManager: ITaskManager<S, D>, taskDescription: D) {
        if (taskManager.getTask() == null) {
            taskManager.clearTask()
            taskManager.newTask(
                    taskDescription = taskDescription
            )
        } else {
            taskManager.getTask()?.getDescription()?.taskName = taskName
        }

        generalTaskManager.restoreSavedData()

        navigator.openMainMenuScreen()

        if (taskManager.getTask()?.isFreeMode() == false) {
            navigator.openTaskListScreen()
        }

        navigator.openJobCardScreen()
    }

}