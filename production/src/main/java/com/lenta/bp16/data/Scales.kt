package com.lenta.bp16.data

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
import okhttp3.*
import java.lang.Exception
import javax.inject.Inject

class Scales @Inject constructor(
        hyperHive: HyperHive,
        private val appSettings: IAppSettings,
        private val analyticsHelper: AnalyticsHelper
) : IScales {

    val settings: ZmpUtz14V001 = ZmpUtz14V001(hyperHive) // Настройки

    private val client = OkHttpClient()

    override suspend fun getWeight(): Either<Failure, String> {
        val serverAddress = getServerAddress()
        val deviceName = appSettings.weightEquipmentName

        if (serverAddress.isNullOrEmpty() || deviceName.isNullOrEmpty()) {
            analyticsHelper.infoScreenMessage("--> Server address or device name is null!")
            return Either.Left(Failure.NetworkConnection)
        }

        val urlOne = HttpUrl.parse("http://$serverAddress/ConnectService/pox/Send")!!.newBuilder()
                .addQueryParameter("connectName", deviceName)
                .addQueryParameter("header", "I?LV01|RX01|LX02")
                .addQueryParameter("data", "")
                .addQueryParameter("timeout", "5000")
                .build()

        Logg.d { "urlOne = $urlOne" }

        var responseOneBody = ""

        try {
            client.newCall(Request.Builder().url(urlOne).build()).execute().apply {
                responseOneBody = this.body()?.string() ?: ""
                Logg.d { "Response one body: $responseOneBody" }
            }
        } catch (e: Exception) {
            Logg.d { "Request one error: $e" }
            analyticsHelper.infoScreenMessage("--> Request one error: $e")
            return Either.Left(Failure.NetworkConnection)
        }

        val handle = getHandleFromResponseOneBody(responseOneBody)

        if (handle?.isEmpty() == true) {
            analyticsHelper.infoScreenMessage("--> Handle is empty!")
            return Either.Left(Failure.NetworkConnection)
        }

        val urlTwo = HttpUrl.parse("http://$serverAddress/ConnectService/pox/ReceiveMessage")!!.newBuilder()
                .addQueryParameter("connectName", deviceName)
                .addQueryParameter("handle", handle)
                .addQueryParameter("timeout", "5000")
                .build()

        Logg.d { "urlTwo = $urlTwo" }

        var responseTwoBody = ""

        try {
            client.newCall(Request.Builder().url(urlTwo).build()).execute().apply {
                responseTwoBody = this.body()?.string() ?: ""
                Logg.d { "Response two body: $responseTwoBody" }
            }
        } catch (e: Exception) {
            Logg.d { "Request two error: $e" }
            analyticsHelper.infoScreenMessage("--> Request two error: $e")
            return Either.Left(Failure.NetworkConnection)
        }

        if (responseTwoBody.isEmpty()) {
            analyticsHelper.infoScreenMessage("--> Second response is empty!")
            return Either.Left(Failure.NetworkConnection)
        }

        val weightInKg = getWeightFromResponse(responseTwoBody)

        return Either.Right(weightInKg)
    }

    private fun getHandleFromResponseOneBody(responseBody: String): String? {
        // <Response>Q|I|L|D|03022020|T|09:25:10:632.762|H|1</Response>
        val handle = responseBody.split("<Response>")[1].split("</Response>")[0]
        Logg.d { "handle = $handle" }

        return handle
    }

    private fun getWeightFromResponse(responseBody: String): String {
        // <Response>I!LV01|GT08|00|GW01|1|GW06|8|GT0A|11000000|GD07|kg;-3;516|GD02|kg;-3;0|GD01|kg;-3;516|LX02</Response>
        val response = responseBody.split("<Response>")[1].split("</Response>")[0]
        val weightInGrams = response.split("GD07|")[1].split("|")[0].replace("\\D".toRegex(), "").toDoubleOrNull() ?: 0.0
        val weightInKilograms = (weightInGrams / 1000).dropZeros()
        Logg.d { "weightInKilograms = $weightInKilograms" }

        return weightInKilograms
    }

    private suspend fun getServerAddress(): String? {
        return withContext(Dispatchers.IO) {
            return@withContext settings.getServerAddress()
        }
    }

}

interface IScales {
    suspend fun getWeight(): Either<Failure, String>
}