package com.lenta.shared.platform.statusbar

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.features.network_state.INetworkStateMonitor
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class StatusBarUiModel @Inject constructor(
        val networkStateMonitor: INetworkStateMonitor
) {
    val pageNumber: MutableLiveData<String> = MutableLiveData("")
    var ip: MutableLiveData<String> = networkStateMonitor.networkInfo.map { it?.ip }
    val printerTasksCount: MutableLiveData<Int> = MutableLiveData(0)
    val batteryLevel: MutableLiveData<Int> = MutableLiveData(0)
    val time: MutableLiveData<String> = MutableLiveData("")
    val networkConnected: MutableLiveData<Boolean> = networkStateMonitor.networkInfo.map { it?.connected }
}