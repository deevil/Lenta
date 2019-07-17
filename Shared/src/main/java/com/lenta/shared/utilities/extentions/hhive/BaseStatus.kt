package com.lenta.shared.utilities.extentions.hhive

import android.annotation.SuppressLint
import com.lenta.shared.analytics.AnalyticsHelper
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.models.BaseStatus
import com.mobrun.plugin.models.Error

@SuppressLint("StaticFieldLeak")
var ANALYTICS_HELPER: AnalyticsHelper? = null

fun BaseStatus.getFailure(): Failure {
    //TODO need improve detection failure
    this.errors?.forEach {
        when (it.source) {
            "cURL" -> return Failure.NetworkConnection
            "Server" -> return it.getServerFailure()
            "SQLite" -> return it.getDbFailure()
        }
    }

    return Failure.ServerError
}

fun Error.getServerFailure(): Failure {
    if (this.code != null && this.code == 401) {
        return Failure.AuthError
    }
    return Failure.ServerError
}

fun Error.getDbFailure(): Failure.DbError {
    return Failure.DbError("${this.descriptions}")
}


fun BaseStatus.toEitherBoolean(nameResource: String? = null): Either<Failure, Boolean> {
    return toEither(true, nameResource)
}

fun <T> BaseStatus.toEither(data: T?, nameResource: String? = null): Either<Failure, T> {
    return if (this.isNotBad() && data != null) {
        ANALYTICS_HELPER?.onFinishFmpRequest(nameResource)
        Either.Right(data)
    } else {
        ANALYTICS_HELPER?.logRequestError(nameResource, this)
        Logg.e { "Failure FMP request for resource $nameResource: ${this}" }
        Either.Left(this.getFailure())
    }
}

fun BaseStatus.isNotBad(): Boolean {
    return this.isOk || this.httpStatus?.status == 304
}