package com.lenta.shared.fmp

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.mobrun.plugin.models.BaseStatus

open class ObjectRawStatus<T> : BaseStatus() {
    @Expose
    @SerializedName("result")
    var result: ResultObject<T>? = null

    override fun toString(): String {
        return "${super.toString()}\n${result?.raw}"
    }
}

open class ResultObject<T>(
        val raw: T?
)

open class BaseRestSapStatus(
        @SerializedName("EV_RETCODE")
        val retCode: String,

        @SerializedName("EV_ERROR_TEXT")
        val errorText: String
)