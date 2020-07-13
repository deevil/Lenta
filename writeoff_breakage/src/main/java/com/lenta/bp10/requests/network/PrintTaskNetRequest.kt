package com.lenta.bp10.requests.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.exception.Failure
import com.lenta.shared.fmp.BaseRestSapStatus
import com.lenta.shared.fmp.ObjectRawStatus
import com.lenta.shared.functional.Either
import com.lenta.shared.interactor.UseCase
import com.lenta.shared.utilities.extentions.hhive.ANALYTICS_HELPER
import com.lenta.shared.utilities.extentions.hhive.getFailure
import com.lenta.shared.utilities.extentions.hhive.isNotBad
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.callparams.WebCallParams
import javax.inject.Inject


class PrintTaskNetRequest
@Inject constructor(private val hyperHive: HyperHive,
                    private val gson: Gson,
                    private val sessionInfo: ISessionInfo) :
        UseCase<Boolean, PrintTask> {


    override suspend fun run(params: PrintTask): Either<Failure, Boolean> {
        val resName = "ZMP_UTZ_WOB_05_V001"
        val printTaskStatus = hyperHive.requestAPI.web(resName,
                WebCallParams().apply {
                    data = gson.toJson(params)
                    headers = mapOf(
                            "X-SUP-DOMAIN" to "DM-MAIN",
                            "Content-Type" to "application/json",
                            "Web-Authorization" to sessionInfo.basicAuth
                    )
                }.apply {
                    ANALYTICS_HELPER?.onStartFmpRequest(resName, "headers: ${this.headers}, data: ${this.data}")
                },
                PrintTaskStatus::class.java
        )
                .execute()

        if (printTaskStatus.isNotBad()) {
            val errorText = printTaskStatus.result?.raw?.errorText
            return if (errorText.isNullOrEmpty()) {
                Either.Right(true)
            } else {
                Either.Left(Failure.SapError(errorText))
            }

        }

        return Either.Left(printTaskStatus.getFailure())

    }

}


class PrintTaskStatus : ObjectRawStatus<BaseRestSapStatus>()


data class PrintTask(
        @SerializedName("IV_TYPE_TASK")
        val typeTask: String,
        @SerializedName("IV_PLANT")
        val tkNumber: String,
        @SerializedName("IV_STORLOC")
        val storloc: String,
        @SerializedName("IV_PRINTERNAME")
        val printerName: String,
        @SerializedName("IT_WOB_LINE")
        val products: List<PrintProduct>

)

data class PrintProduct(
        @SerializedName("MATERIAL")
        val materialNumber: String,
        @SerializedName("ENTRY_QNT")
        val quantity: Double,
        @SerializedName("ENTRY_UOM")
        val uomCode: String,
        @SerializedName("REASON")
        val reasonCode: String
)

