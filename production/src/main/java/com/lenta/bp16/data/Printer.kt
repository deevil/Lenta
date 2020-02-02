package com.lenta.bp16.data

import com.google.gson.Gson
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

    override fun printTag(): Either<Failure, Boolean> {


        return Either.Right(true)
    }

}

interface IPrinter {
    fun printTag(): Either<Failure, Boolean>
}