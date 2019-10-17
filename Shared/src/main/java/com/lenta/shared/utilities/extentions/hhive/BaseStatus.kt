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

fun <T> BaseStatus.toEither(data: T?, resourceName: String? = null): Either<Failure, T> {
    ANALYTICS_HELPER?.onFinishFmpRequest(resourceName)
    return if (this.isNotBad() && data != null) {
        Either.Right(data)
    } else {
        ANALYTICS_HELPER?.logRequestError(resourceName, this)
        Logg.w { "Failure FMP request for resource $resourceName: ${this.toStringSafety()}" }
        Either.Left(this.getFailure())
    }
}

fun BaseStatus.isNotBad(): Boolean {
    return this.isOk || this.httpStatus?.status == 304
}

fun BaseStatus.toStringSafety(): String {
    return "BaseStatusV08{status=" + this.status + ", httpStatus=" + this.httpStatus + ", errors=" + this.errors?.map { it.toStringSafety() } + ", retryCount=" + this.retryCount + '}'.toString()
}

fun Error.toStringSafety(): String {
    return "Error{code=" + this.code + ", source='" + this.source + '\''.toString() + ", description='" + this.description + '\''.toString() + ", descriptions=" + this.descriptions + '}'.toString()
}