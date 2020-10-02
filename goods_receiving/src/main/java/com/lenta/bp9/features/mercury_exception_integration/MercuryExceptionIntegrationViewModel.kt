package com.lenta.bp9.features.mercury_exception_integration

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.bp9.model.task.IReceivingTaskManager
import com.lenta.bp9.platform.navigation.IScreenNavigator
import com.lenta.bp9.repos.IDataBaseRepo
import com.lenta.bp9.requests.network.ExclusionFromIntegrationNetRequest
import com.lenta.bp9.requests.network.ExclusionFromIntegrationParameters
import com.lenta.bp9.requests.network.ExclusionFromIntegrationResult
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import com.lenta.shared.view.OnPositionClickListener
import javax.inject.Inject

class MercuryExceptionIntegrationViewModel : CoreViewModel(), OnPositionClickListener {


    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var taskManager: IReceivingTaskManager
    @Inject
    lateinit var dataBase: IDataBaseRepo
    @Inject
    lateinit var exclusionFromIntegration: ExclusionFromIntegrationNetRequest
    @Inject
    lateinit var context: Context
    @Inject
    lateinit var sessionInfo: ISessionInfo

    val selectedPosition: MutableLiveData<Int> = MutableLiveData(0)
    private val reasonsExclusionInfo: MutableLiveData<List<QualityInfo>> = MutableLiveData()
    val spinReasonsExclusion: MutableLiveData<List<String>> = MutableLiveData()


    init {
        launchUITryCatch {
            reasonsExclusionInfo.value = dataBase.getExclusionFromIntegration()
            spinReasonsExclusion.value = reasonsExclusionInfo.value?.map {
                it.name
            }
        }
    }

    fun getTitle(): String {
        return taskManager.getReceivingTask()?.taskHeader?.caption ?: ""
    }

    fun onClickNext() {
        launchUITryCatch {
            screenNavigator.showProgressLoadingData(::handleFailure)
            taskManager.getReceivingTask()?.let { task ->
                val params = ExclusionFromIntegrationParameters(
                        taskNumber = task.taskHeader.taskNumber,
                        deviceIP = context.getDeviceIp(),
                        personalNumber = sessionInfo.personnelNumber ?: "",
                        reason = reasonsExclusionInfo.value!![selectedPosition.value!!].code
                )
                exclusionFromIntegration(params).either(::handleFailure, ::handleSuccess)
            }
            screenNavigator.hideProgress()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleSuccess(result: ExclusionFromIntegrationResult) {
        screenNavigator.openFinishReviseLoadingScreen()
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.openAlertScreen(failure)
    }

    override fun onClickPosition(position: Int) {
        selectedPosition.value = position
    }
}
