package com.lenta.shared.utilities.extentions

import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.functional.rightToLeft

fun <R : IResultWithRetCodes> Either<Failure, R>.getResult(defaultRetCode: Int = 1): Either<Failure, R> {
    return this.rightToLeft(
            fnRtoL = { result ->
                result.retCodes?.firstOrNull { retCode ->
                    retCode.retCode == defaultRetCode
                }?.let { retCode ->
                    return@rightToLeft Failure.SapError(retCode.errorText)
                }
            }
    )
}

interface IResultWithRetCodes {
    val retCodes: List<IRetCode>?
}

interface IRetCode {
    val retCode: Int
    val errorText: String
}