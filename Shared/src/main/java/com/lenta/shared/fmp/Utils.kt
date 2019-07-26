package com.lenta.shared.fmp

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.lenta.shared.exception.Failure
import com.lenta.shared.functional.Either
import com.lenta.shared.utilities.Logg
import com.lenta.shared.utilities.extentions.hhive.ANALYTICS_HELPER
import com.lenta.shared.utilities.extentions.hhive.toEither
import com.mobrun.plugin.models.BaseStatus

fun <N, T : ObjectRawStatus<N>> String?.toFmpObjectRawStatusEither(clazz: Class<T>, gson: Gson, resourceName: String? = null): Either<Failure, N> {
    return try {
        val status = gson.fromJson(this, clazz)
        Logg.d { "status: $status" }
        status.toEither(status.result?.raw, resourceName = resourceName)
    } catch (ex: JsonSyntaxException) {
        val status = gson.fromJson(this, BaseStatus::class.java)
        Logg.d { "status: $status" }
        ANALYTICS_HELPER?.onFinishFmpRequest(resourceName)
        status.toEither(null, resourceName = resourceName)
    }

}