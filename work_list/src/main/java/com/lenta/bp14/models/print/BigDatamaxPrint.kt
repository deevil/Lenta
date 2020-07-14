package com.lenta.bp14.models.print

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.utilities.Logg
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import java.lang.Exception
import javax.inject.Inject


class BigDatamaxPrintImpl @Inject constructor(
        private val sessionInfo: ISessionInfo,
        private val gson: Gson,
        private val analyticsHelper: AnalyticsHelper
) : BigDatamaxPrint {

    private val client = OkHttpClient()

    private fun getBigDatamaxData(json: String): String {
        return "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ws=\"http://service.ws.printing.setretailx.crystals.ru/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <ws:print>\n" +
                "         <!--Optional:-->\n" +
                "         <request>$json</request>\n" +
                "      </ws:print>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"
    }

    override fun printToBigDatamax(tasks: List<PrintInfo>): Either<Failure, Boolean> {
        return sendJsonToPrint(getJson(tasks))
    }

    override fun printToBigDatamax(
            ean: String,
            regular: Boolean,
            copies: Int): Either<Failure, Boolean> {

        val json = getJson(
                listOf(
                        PrintInfo(
                                barCode = ean,
                                amount = copies,
                                templateCode = if (regular) 1 else 2

                        )
                )
        )

        return sendJsonToPrint(json)

    }

    private fun sendJsonToPrint(json: String): Either<Failure, Boolean> {

        val url = getUrl()
        val data = getBigDatamaxData(json)

        analyticsHelper.logPrintDatamaxRequest(url, data)

        Logg.d { "datamax print request. url: $url, data: $data" }

        val request = Request.Builder()
                .url(url)
                .post(RequestBody.create(MediaType.parse("text/xml"), data))
                .addHeader("authorization", "Basic TUFLQVJPVjoxcTJ3M2U0cg==")
                .addHeader("content-type", "text/xml")
                .build()

        try {
            client.newCall(request).execute().apply {
                Logg.d { "Big Datamax response: $this" }
                val responceBody = this.body()?.string().orEmpty()
                Logg.d { "Big Datamax response body: $responceBody" }
                analyticsHelper.logPrintDatamaxResponce(this.code(), responceBody)
            }
        } catch (e: Exception) {
            Logg.d { "Big Datamax request error: $e" }
            return Either.Left(Failure.NetworkConnection)
        }

        return Either.Right(true)
    }

    private fun getJson(prints: List<PrintInfo>): String {
        return gson.toJson(BigDatamaxPrintInfo(prints))
    }

    private fun getUrl(): String {
        val tkNumber = sessionInfo.market?.toIntOrNull() ?: 0
        return "http://l${tkNumber}pos.retail.lenta.spb.rus:8090/SET-PrintPriceTags/PrintingServiceWS?wsdl"

    }

}

data class BigDatamaxPrintInfo(
        @SerializedName("arguments")
        val arguments: List<PrintInfo>
)

data class PrintInfo(
        @SerializedName("barcode")
        val barCode: String,
        @SerializedName("amount")
        val amount: Int,
        /**
         * 1 - желтый, 2 - красный
         */
        @SerializedName("templateCode")
        val templateCode: Int

)

interface BigDatamaxPrint {
    fun printToBigDatamax(
            ean: String,
            regular: Boolean,
            copies: Int): Either<Failure, Boolean>

    fun printToBigDatamax(tasks: List<PrintInfo>): Either<Failure, Boolean>
}