package com.lenta.bp16.data

import com.google.gson.Gson
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.resources.dao_ext.getServerAddress
import com.lenta.shared.fmp.resources.fast.ZmpUtz14V001
import com.lenta.shared.functional.Either
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.dropZeros
import com.mobrun.plugin.api.HyperHive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import javax.inject.Inject

class Scales @Inject constructor(
        hyperHive: HyperHive,
        private val appSettings: IAppSettings,
        private val gson: Gson,
        private val analyticsHelper: AnalyticsHelper
) : IScales {

    val settings: ZmpUtz14V001 = ZmpUtz14V001(hyperHive) // Настройки

    private val client = OkHttpClient()

    override suspend fun getWeight(): Either<Failure, String> {
        val serverAddress = getServerAddress()
        val scalesName = appSettings.weightEquipmentName

        if (serverAddress.isNullOrEmpty() || scalesName.isNullOrEmpty()) {
            return Either.Left(Failure.AuthError)
        }

        val requestUrlOne = "http://$serverAddress/ConnectService/pox/Send?connectName=$scalesName&header=I?LV01|RX01|LX02&data=&timeout=5000"
        Logg.d { "--> requestUrlOne = $requestUrlOne" }

        val requestOne = Request.Builder()
                .url(requestUrlOne)
                .build()

        var handleForRequestTwo = ""

        try {
            client.newCall(requestOne).execute().apply {
                Logg.d { "--> Request one response: $this" }
                handleForRequestTwo = this.body()?.string() ?: ""
                Logg.d { "--> Request one response body: $handleForRequestTwo" }
            }
        } catch (e: Exception) {
            Logg.d { "--> Request one error: $e" }
            return Either.Left(Failure.NetworkConnection)
        }

        if (handleForRequestTwo.isEmpty()) {
            return Either.Left(Failure.NetworkConnection)
        }

        val requestUrlTwo = "http://$serverAddress/ConnectService/pox/ReceiveMessage?connectName=$scalesName&handle=$handleForRequestTwo&timeout=5000"
        Logg.d { "--> requestUrlTwo = $requestUrlTwo" }

        val requestTwo = Request.Builder()
                .url(requestUrlTwo)
                .build()

        var weightInfo = ""

        try {
            client.newCall(requestTwo).execute().apply {
                Logg.d { "--> Request two response: $this" }
                weightInfo = this.body()?.string() ?: ""
                Logg.d { "--> Request two response body: $weightInfo" }
            }
        } catch (e: Exception) {
            Logg.d { "--> Request two error: $e" }
            return Either.Left(Failure.NetworkConnection)
        }

        if (weightInfo.isEmpty()) {
            return Either.Left(Failure.NetworkConnection)
        }

        val weightInKg = getWeightFromResponse(weightInfo)

        return Either.Right(weightInKg)
    }

    private fun getWeightFromResponse(weightInfo: String): String {
        // Пример - I!LV01|GT08|00|GW01|1|GW06|8|GT0A|11000000|GD07|kg;-3;516|GD02|kg;-3;0|GD01|kg;-3;516|LX02

        val infoParts = weightInfo.split("|")
        val targetPartIndex = infoParts.indexOfFirst { it == "GD07" }

        val weightInGrams = infoParts[targetPartIndex + 1].replace("\\D", "").toDoubleOrNull() ?: 0.0
        val weightInKilograms = weightInGrams / 1000

        return weightInKilograms.dropZeros()
    }

    private suspend fun getServerAddress(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getServerAddress()
        }
    }

}

interface IScales {
    suspend fun getWeight() : Either<Failure, String>
}