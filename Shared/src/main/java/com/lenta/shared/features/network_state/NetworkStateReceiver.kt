package com.lenta.shared.features.network_state

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.lifecycle.MutableLiveData
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.getDeviceIp

class NetworkStateReceiver : BroadcastReceiver(), INetworkStateReceiver {

    override val networkInfo: MutableLiveData<NetworkInfo> = MutableLiveData(NetworkInfo.noInternet)

    override fun onReceive(context: Context, intent: Intent) {
        val noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)
        networkInfo.postValue(if (noConnectivity) NetworkInfo.noInternet else NetworkInfo.connected(context.getDeviceIp()))
        Logg.d { "Internet connected: ${!noConnectivity}" }
    }


}

interface INetworkStateReceiver {
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
