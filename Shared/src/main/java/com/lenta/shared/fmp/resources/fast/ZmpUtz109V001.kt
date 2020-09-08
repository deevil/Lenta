package com.lenta.shared.fmp.resources.fast

import com.google.gson.annotations.SerializedName
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.helper.LocalTableResourceHelper
import com.mobrun.plugin.api.request_assistant.CustomParameter
import com.mobrun.plugin.api.request_assistant.RequestBuilder
import com.mobrun.plugin.api.request_assistant.ScalarParameter
import com.mobrun.plugin.models.StatusSelectTable

/**
 * Справочник BKS – группы маркировки
 */
class ZmpUtz109V001(private val hyperHive: HyperHive) {
    val localHelper_ET_MARK_GROUP: LocalTableResourceHelper<Item_Local_ET_MARK_GROUP, Status_ET_MARK_GROUP>

    init {
        localHelper_ET_MARK_GROUP = LocalTableResourceHelper<Item_Local_ET_MARK_GROUP, Status_ET_MARK_GROUP>(
                NAME_RESOURCE,
                NAME_OUT_PARAM_ET_TASK_TSP,
                hyperHive,
                Status_ET_MARK_GROUP::class.java
        )
    }

    fun newRequest(): RequestBuilder<Params, LimitedScalarParameter> {
        return RequestBuilder(hyperHive, NAME_RESOURCE, true)
    }

    class Status_ET_MARK_GROUP : StatusSelectTable<Item_Local_ET_MARK_GROUP?>()

    class Item_Local_ET_MARK_GROUP {
        @SerializedName("ZMARKTYPE")
        var markType: String? = null

        @SerializedName("MARKTYPE_GROUP")
        var markGroupCode: String? = null

        @SerializedName("MARKTP_GRP_TEXT")
        var markGroupName: String? = null

        @SerializedName("MARKTP_GRP_ABR")
        var markGroupAbr: String? = null
    }

    interface Params : CustomParameter

    class LimitedScalarParameter(name: String?, value: Any?): ScalarParameter<Any>(name, value) {
        companion object {
            const val KEY_IV_NODEPLOY = "IV_NODEPLOY"

            fun newInstance(value: String?): LimitedScalarParameter {
                return LimitedScalarParameter(KEY_IV_NODEPLOY, value)
            }
        }
    }

    companion object {
        const val NAME_RESOURCE = "ZMP_UTZ_109_V001"
        const val NAME_OUT_PARAM_ET_TASK_TSP = "ET_MARK_GROUP"
        const val LIFE_TIME = "1 day, 0:00:00"
    }
}