package com.lenta.bp16.data

import com.google.gson.Gson
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getServerAddress
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.functional.Either
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import javax.inject.Inject

class Scales @Inject constructor(
        hyperHive: HyperHive,
        private val appSettings: IAppSettings,
        private val gson: Gson,
        private val analyticsHelper: AnalyticsHelper
) : IScales {

    val settings: ZmpUtz14V001 = ZmpUtz14V001(hyperHive) // Настройки

    private val client = OkHttpClient()

    override suspend fun getWeight(): Either<Failure, Int> {

        val serverAddress = getServerAddress()
        val scalesName = appSettings.weightEquipmentName

        if (serverAddress.isNullOrEmpty() || scalesName.isNullOrEmpty()) {
            return Either.Left(Failure.AuthError)
        }

        val startRequest = getStartRequest(serverAddress, scalesName)
        Logg.d { "--> startRequest = $startRequest" }




        return Either.Right(321)
    }

    private fun getStartRequest(serverAddress: String, scalesName: String): String {
        return "http://$serverAddress/ConnectService/pox/Send?connectName=$scalesName&header=I?LV01|RX01|LX02&data=&timeout=5000"
    }

    private suspend fun getServerAddress(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getServerAddress()
        }
    }

}

interface IScales {
    suspend fun getWeight() : Either<Failure, Int>
}