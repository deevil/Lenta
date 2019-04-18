package com.lenta.shared.platform.statusbar

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.battery_state.IBatteryStateMonitor
import com.lenta.shared.platform.network_state.INetworkStateMonitor
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class StatusBarUiModel @Inject constructor(
        val networkStateMonitor: INetworkStateMonitor,
        var batteryStateMonitor: IBatteryStateMonitor
) {
    val pageNumber: MutableLiveData<String> = MutableLiveData("")
    var ip: MutableLiveData<String> = networkStateMonitor.networkInfo.map { it?.ip }
    val printerTasksCount: MutableLiveData<Int> = MutableLiveData(0)
    val batteryLevel: MutableLiveData<Int> = batteryStateMonitor.batteryState.map { it?.level }
    val batteryIsCharging: MutableLiveData<Boolean> = batteryStateMonitor.batteryState.map { it?.isCharging }
    val time: MutableLiveData<String> = MutableLiveData("")
    val networkConnected: MutableLiveData<Boolean> = networkStateMonitor.networkInfo.map { it?.connected }
}