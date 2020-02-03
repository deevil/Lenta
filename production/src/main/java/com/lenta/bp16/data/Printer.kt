package com.lenta.bp16.data

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.settings.IAppSettings
import javax.inject.Inject

class Printer @Inject constructor(
        private val sessionInfo: ISessionInfo,
        private val appSettings: IAppSettings,
        private val gson: Gson,
        private val analyticsHelper: AnalyticsHelper
) : IPrinter {

    override fun printTag(printInnerTagInfo: PrintInnerTagInfo): Either<Failure, Boolean> {
        val ip = appSettings.printerIpAddress

        return Either.Right(false)
    }

}

interface IPrinter {
    fun printTag(printInnerTagInfo: PrintInnerTagInfo): Either<Failure, Boolean>
}

data class PrintInnerTagInfo(
        val quantity: String,
        val codeCont: String,
        val storCond: String,
        val planAufFinish: String,
        val aufnr: String,
        val nameOsn: String,
        val dateExpir: String,
        val goodsName: String,
        val weigher: String,
        val productTime: String,
        val nameDone: String,
        val goodsCode: String,
        val barcode: String
)