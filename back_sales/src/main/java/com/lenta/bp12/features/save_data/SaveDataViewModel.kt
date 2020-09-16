package com.lenta.bp12.features.save_data

import androidx.lifecycle.MutableLiveData
import com.lenta.bp12.managers.interfaces.IGeneralTaskManager
import com.lenta.bp12.platform.navigation.IScreenNavigator
import com.lenta.bp12.platform.resource.IResourceManager
import com.lenta.bp12.request.SendTaskDataNetRequest
import com.lenta.bp12.request.SendTaskDataResult
import com.lenta.bp12.request.pojo.SentTaskInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class SaveDataViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator

    @Inject
    lateinit var sessionInfo: ISessionInfo

    @Inject
    lateinit var generalTaskManager: IGeneralTaskManager

    @Inject
    lateinit var sendTaskDataNetRequest: SendTaskDataNetRequest

    @Inject
    lateinit var resource: IResourceManager


    val title by lazy {
        resource.tk(sessionInfo.market.orEmpty())
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
        sendTaskData()
    }

    // -----------------------------

    private fun sendTaskData() {
        launchUITryCatch {
            navigator.showProgressLoadingData(::handleFailure)

            sendTaskDataNetRequest(
                    generalTaskManager.getSendTaskDataParams()
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure, ::handleTaskDataResult)
        }
    }

    private fun handleTaskDataResult(result: SendTaskDataResult) {
        sentTaskInfoList.postValue(result.sentTasks)
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