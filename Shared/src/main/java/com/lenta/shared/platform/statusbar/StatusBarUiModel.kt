package com.lenta.shared.platform.statusbar

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.battery_state.IBatteryStateMonitor
import com.lenta.shared.platform.network_state.INetworkStateMonitor
import com.lenta.shared.platform.time.ITimeMonitor
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.extentions.map
import javax.inject.Inject

class StatusBarUiModel @Inject constructor(
        val networkStateMonitor: INetworkStateMonitor,
        val batteryStateMonitor: IBatteryStateMonitor,
        val timeMonitor: ITimeMonitor,
        val appSettings: IAppSettings
) {
    val pageNumber: MutableLiveData<String> = MutableLiveData("")
    var ip: MutableLiveData<String> = networkStateMonitor.networkInfo.map { it?.ip }
    val printer = appSettings.printerLiveData.map { if (it.isNullOrBlank()) "?" else it }
    val batteryLevel: MutableLiveData<Int> = batteryStateMonitor.batteryState.map { it?.level }
    val batteryIsCharging: MutableLiveData<Boolean> = batteryStateMonitor.batteryState.map { it?.isCharging }
    val time: MutableLiveData<Long> = timeMonitor.observeUnixTime().map { it }
    val networkConnected: MutableLiveData<Boolean> = networkStateMonitor.networkInfo.map { it?.connected }
}