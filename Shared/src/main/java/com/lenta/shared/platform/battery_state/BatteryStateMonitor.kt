package com.lenta.shared.platform.battery_state

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.MutableLiveData
import android.os.BatteryManager
import com.lenta.shared.utilities.Logg


class BatteryStateMonitor : BroadcastReceiver(), IBatteryStateMonitor {

    override val batteryState: MutableLiveData<BatteryState> = MutableLiveData(BatteryState.unknown)

    override fun onReceive(context: Context, intent: Intent) {

        val statusCharge = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = statusCharge == BatteryManager.BATTERY_STATUS_CHARGING ||
                statusCharge == BatteryManager.BATTERY_STATUS_FULL
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryLevel = ((level / scale.toFloat()) * 100).toInt()

        BatteryState(isCharging = isCharging, level = batteryLevel).let {
            Logg.d { "BatteryState: $it" }
            batteryState.postValue(it)
        }
    }

    fun start(activity: Activity) {
        activity.registerReceiver(this, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }

    fun stop(activity: Activity) {
        activity.unregisterReceiver(this)
    }


}

interface IBatteryStateMonitor {
    val batteryState: MutableLiveData<BatteryState>
}

data class BatteryState(
        val isCharging: Boolean,
        val level: Int
) {
    companion object {
        val unknown: BatteryState by lazy {
            BatteryState(false, -1)
        }

    }
}
