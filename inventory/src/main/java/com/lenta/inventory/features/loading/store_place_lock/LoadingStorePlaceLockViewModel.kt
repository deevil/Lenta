package com.lenta.inventory.features.loading.store_place_lock

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.lenta.inventory.models.RecountType
import com.lenta.inventory.models.StorePlaceLockMode
import com.lenta.inventory.models.task.IInventoryTaskManager
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.StorePlaceLockNetRequest
import com.lenta.inventory.requests.network.StorePlaceLockParams
import com.lenta.inventory.requests.network.StorePlaceLockRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.launchUITryCatch
import javax.inject.Inject

class LoadingStorePlaceLockViewModel : CoreLoadingViewModel() {

    @Inject
    lateinit var taskManager: IInventoryTaskManager
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var storePlaceLockRequest: StorePlaceLockNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context

    var mode: StorePlaceLockMode = StorePlaceLockMode.None
    var storePlaceNumber: String = ""


    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        launchUITryCatch {
            taskManager.getInventoryTask()?.let {
                title.postValue(it.taskDescription.getTaskTypeAndNumber())
                progress.value = true
                val recountType = taskManager.getInventoryTask()?.taskDescription?.recountType
                val userNumber = if (recountType == RecountType.ParallelByPerNo) sessionInfo.personnelNumber.orEmpty() else "" // указываем номер только при пересчете по номерам
                val storePlaceCode = if (recountType == RecountType.ParallelByStorePlaces) storePlaceNumber else "" //указываем номер только при пересчете по МХ
                storePlaceLockRequest(StorePlaceLockParams(ip = context.getDeviceIp(),
                        taskNumber = it.taskDescription.taskNumber,
                        storePlaceCode = storePlaceCode,
                        mode = mode.mode,
                        userNumber = userNumber)).either(::handleFailure, ::handleSuccess)
                progress.value = false
            }
        }
    }

    override fun handleFailure(failure: Failure) {
        screenNavigator.goBack()
        screenNavigator.openAlertScreen(failure)
    }

    private fun handleSuccess(storePlaceLockInfo: StorePlaceLockRestInfo) {
        Logg.d { "tasksListRestInfo $storePlaceLockInfo" }
        screenNavigator.goBack()
        when(mode) {
            StorePlaceLockMode.Lock -> {
                taskManager.getInventoryTask()?.let {
                    screenNavigator.openGoodsListScreen(storePlaceNumber)
                }
            }
            StorePlaceLockMode.Unlock -> screenNavigator.goBack()
            else -> Unit
        }
    }

    override fun clean() {
        progress.postValue(false)
    }

}