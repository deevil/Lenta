package com.lenta.shared.utilities.extentions.hhive

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.utilities.Logg
import com.mobrun.plugin.models.BaseStatus
import com.mobrun.plugin.models.Error

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


fun BaseStatus.toEitherBoolean(): Either<Failure, Boolean> {
    return toEither(true)
}

fun <T> BaseStatus.toEither(data: T?): Either<Failure, T> {
    return if (this.isNotBad() && data != null) {
        Either.Right(data)
    } else {
        Logg.e { "Failure FMP request: ${this}" }
        Either.Left(this.getFailure())
    }
}

fun BaseStatus.isNotBad(): Boolean {
    return this.isOk || this.httpStatus?.status == 304
}