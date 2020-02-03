package com.lenta.bp16.data

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.lenta.shared.account.ISessionInfo
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import javax.inject.Inject

class Printer @Inject constructor(
        private val sessionInfo: ISessionInfo,
        private val gson: Gson,
        private val analyticsHelper: AnalyticsHelper
) : IPrinter {

    override fun printTag(ean: String): Either<Failure, Boolean> {


        return Either.Right(false)
    }

}

interface IPrinter {
    fun printTag(ean: String): Either<Failure, Boolean>
}