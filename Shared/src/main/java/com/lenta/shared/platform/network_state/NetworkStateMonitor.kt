package com.lenta.shared.platform.network_state

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp


class NetworkStateMonitor : BroadcastReceiver(), INetworkStateMonitor {

    override val networkInfo: MutableLiveData<NetworkInfo> = MutableLiveData(NetworkInfo.noInternet)

    override fun onReceive(context: Context, intent: Intent) {

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val activeNetworkInfo = connectivityManager?.activeNetworkInfo
        val isConnected = activeNetworkInfo?.isConnected == true

        val deviceIp = context.getDeviceIp()
        networkInfo.postValue(if (!isConnected) NetworkInfo.noInternet else NetworkInfo.connected(deviceIp))
        Logg.d { "Internet connected: $isConnected" }


    }

    fun start(activity: Activity) {
        @Suppress("DEPRECATION")
        activity.registerReceiver(this, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    fun stop(activity: Activity) {
        activity.unregisterReceiver(this)
    }


}

interface INetworkStateMonitor {
    val networkInfo: MutableLiveData<NetworkInfo>
}

data class NetworkInfo(
        val connected: Boolean,
        val ip: String?
) {
    companion object {
        val noInternet: NetworkInfo by lazy {
            NetworkInfo(false, null)
        }

        fun connected(ip: String): NetworkInfo = NetworkInfo(connected = true, ip = ip)
    }
}
