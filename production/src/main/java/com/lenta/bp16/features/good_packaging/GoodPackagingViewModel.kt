package com.lenta.bp16.features.good_packaging

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.bp16.model.ITaskManager
import com.lenta.bp16.platform.navigation.IScreenNavigator
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


    val title by lazy {
        taskManager.currentGood.getNameWithMaterial(" - ")
    }

    val deviceIp: MutableLiveData<String> = MutableLiveData("")

    val good by lazy {
        taskManager.currentGood
    }

    val raw by lazy {
        taskManager.currentRaw
    }

    val weight = MutableLiveData("")

    private val enteredWeight = weight.map {
        it?.toDoubleOrNull() ?: 0.0
    }

    private val totalWeight = enteredWeight.map {
        it.sumWith(raw.totalQuantity)
    }

    val totalWeightWithUnits = totalWeight.map {
        "${it.dropZeros()} ${good.units.name}"
    }

    val planned by lazy {
        "${raw.planned} ${good.units.name}"
    }

    val completeEnabled: MutableLiveData<Boolean> = weight.map {
        it?.toDoubleOrNull() ?: 0.0 != 0.0
    }

    // -----------------------------

    fun onClickComplete() {
        navigator.showFixingPackagingPhaseSuccessful {
            viewModelScope.launch {
                navigator.showProgressLoadingData()

                packGoodNetRequest(
                        PackGoodParams(
                                marketNumber = sessionInfo.market ?: "Not found!",
                                deviceIp = deviceIp.value ?: "Not found!",
                                material = good.material,
                                orderNumber = raw.orderNumber,
                                quantity = raw.totalQuantity,
                                taskNumber = taskManager.currentTask.task.number
                        )
                ).also {
                    navigator.hideProgress()
                }.either(::handleFailure) {
                    taskManager.currentTask.isProcessed = true

                    navigator.closeAllScreen()
                    navigator.openTaskListScreen()
                }
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        super.handleFailure(failure)
        navigator.openAlertScreen(failure)
    }

}