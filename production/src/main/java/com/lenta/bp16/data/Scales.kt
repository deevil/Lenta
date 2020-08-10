package com.lenta.bp16.data

import com.lenta.bp16.repository.IDatabaseRepository
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.settings.IAppSettings
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.dropZeros
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

class Scales @Inject constructor(
        private val appSettings: IAppSettings,
        private val analyticsHelper: AnalyticsHelper,
        private val database: IDatabaseRepository
) : IScales {

    private val client = OkHttpClient()

    override suspend fun getWeight(): Either<Failure, String> {
        val serverAddress = database.getServerAddress()
        val deviceName = appSettings.weightEquipmentName

        if (serverAddress.isNullOrEmpty() || deviceName.isNullOrEmpty()) {
            analyticsHelper.infoScreenMessage("--> Server address or device name is null!")
            return Either.Left(Failure.WeighingError)
        }

        val urlOne = "http://$serverAddress/ConnectService/pox/Send".toHttpUrlOrNull()!!.newBuilder()
                .addQueryParameter("connectName", deviceName)
                .addQueryParameter("header", "I?LV01|RX01|LX02")
                .addQueryParameter("data", "")
                .addQueryParameter("timeout", "5000")
                .build()

        Logg.d { "urlOne = $urlOne" }

        var responseOneBody = ""

        try {
            client.newCall(Request.Builder().url(urlOne).build()).execute().apply {
                responseOneBody = this.body?.string().orEmpty()
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

        val urlTwo = "http://$serverAddress/ConnectService/pox/ReceiveMessage".toHttpUrlOrNull()!!.newBuilder()
                .addQueryParameter("connectName", deviceName)
                .addQueryParameter("handle", handle)
                .addQueryParameter("timeout", "5000")
                .build()

        Logg.d { "urlTwo = $urlTwo" }

        var responseTwoBody = ""

        try {
            client.newCall(Request.Builder().url(urlTwo).build()).execute().apply {
                responseTwoBody = this.body?.string().orEmpty()
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
        val weightInfo = response.split("GD07|")[1].split("|")[0].split(";") // kg;-3;516
        var weight = weightInfo[2]
        val decimalPlaces = weightInfo[1].takeLast(1).toIntOrNull() ?: 0

        while (weight.length < decimalPlaces + 1) {
            weight = "0$weight"
        }

        weight = StringBuilder(weight).insert(weight.length - decimalPlaces, ".").toString()

        val weightInKilograms = (weight.toDoubleOrNull() ?: 0.0).dropZeros()
        Logg.d { "weightInKilograms = $weightInKilograms" }

        return weightInKilograms
    }

}

interface IScales {
    suspend fun getWeight(): Either<Failure, String>
}