package com.lenta.bp16.features.good_packaging

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
import com.lenta.bp16.request.EndProcessingNetRequest
import com.lenta.bp16.request.EndProcessingParams
import com.lenta.bp16.request.PackGoodNetRequest
import com.lenta.bp16.request.PackGoodParams
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.platform.viewmodel.CoreViewModel
import com.lenta.shared.utilities.extentions.dropZeros
import com.lenta.shared.utilities.extentions.map
import com.lenta.shared.utilities.extentions.sumWith
import kotlinx.coroutines.launch
import javax.inject.Inject

class GoodPackagingViewModel : CoreViewModel() {

    @Inject
    lateinit var navigator: IScreenNavigator
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var taskManager: ITaskManager
    @Inject
    lateinit var packGoodNetRequest: PackGoodNetRequest


    val good by lazy {
        taskManager.currentGood
    }

    val raw by lazy {
        taskManager.currentRaw
    }

    val title by lazy {
        good.map { it?.getNameWithMaterial() }
    }

    val deviceIp = MutableLiveData("")

    val weightField = MutableLiveData("0")

    private val entered = weightField.map {
        it?.toDoubleOrNull() ?: 0.0
    }

    val enteredWithUnits = entered.map {
        "${it.dropZeros()} ${good.value!!.units.name}"
    }

    private val defect by lazy {
        MutableLiveData(0.0)
    }

    val defectWithUnits = defect.map {
        "${it.dropZeros()} ${good.value!!.units.name}"
    }

    val planned by lazy {
        "${raw.value?.planned.dropZeros()} ${good.value?.units?.name}"
    }

    val completeEnabled = entered.map {
        it ?: 0.0 != 0.0
    }

    // -----------------------------

    fun onClickComplete() {
        viewModelScope.launch {
            navigator.showProgressLoadingData()

            packGoodNetRequest(
                    PackGoodParams(
                            marketNumber = sessionInfo.market ?: "Not found!",
                            taskType = taskManager.getTaskTypeCode(),
                            deviceIp = deviceIp.value ?: "Not found!",
                            material = good.value!!.material,
                            orderNumber = raw.value!!.orderNumber,
                            quantity = entered.value!!,
                            taskNumber = taskManager.currentTask.value!!.taskInfo.number
                    )
            ).also {
                navigator.hideProgress()
            }.either(::handleFailure) {
                navigator.showFixingPackagingPhaseSuccessful {
                    taskManager.setDataSentForPackTask()
                    navigator.goBack()
                }
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

    fun onClickDefect() {
        navigator.openDefectInfoScreen()
    }

}