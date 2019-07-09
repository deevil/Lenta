package com.lenta.inventory.features.loading.store_place_lock

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.lenta.inventory.features.task_list.TaskItem
import com.lenta.inventory.models.StorePlaceLockMode
import com.lenta.inventory.models.task.TaskStorePlaceInfo
import com.lenta.inventory.platform.navigation.IScreenNavigator
import com.lenta.inventory.requests.network.StorePlaceLockNetRequest
import com.lenta.inventory.requests.network.StorePlaceLockParams
import com.lenta.inventory.requests.network.StorePlaceLockRestInfo
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.features.loading.CoreLoadingViewModel
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp
import kotlinx.coroutines.launch
import javax.inject.Inject

class LoadingStorePlaceLockViewModel : CoreLoadingViewModel() {
    @Inject
    lateinit var screenNavigator: IScreenNavigator
    @Inject
    lateinit var storePlaceLockRequest: StorePlaceLockNetRequest
    @Inject
    lateinit var sessionInfo: ISessionInfo
    @Inject
    lateinit var context: Context

    var storePlaceInfo: TaskStorePlaceInfo? = null
    var mode: StorePlaceLockMode = StorePlaceLockMode.None
    var taskInfo: TaskItem? = null

    override val title: MutableLiveData<String> = MutableLiveData()
    override val progress: MutableLiveData<Boolean> = MutableLiveData(true)
    override val speedKbInSec: MutableLiveData<Int> = MutableLiveData()
    override val sizeInMb: MutableLiveData<Float> = MutableLiveData()

    init {
        viewModelScope.launch {
            progress.value = true
            storePlaceLockRequest(StorePlaceLockParams(ip = context.getDeviceIp(),
                    taskNumber = taskInfo?.number ?: "",
                    storePlaceCode = storePlaceInfo?.placeCode ?: "",
                    mode = mode.mode,
                    userNumber = sessionInfo.personnelNumber ?: "")).either(::handleFailure, ::handleSuccess)
            progress.value = false
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
            StorePlaceLockMode.Lock -> screenNavigator.openGoodsListScreen()
            StorePlaceLockMode.Unlock -> screenNavigator.openStoragesList()
        }
    }

    override fun clean() {
        progress.postValue(false)
    }

}