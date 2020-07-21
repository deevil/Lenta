package com.lenta.movement.fmp.resources.fast

import com.google.gson.annotations.SerializedName
import com.lenta.movement.models.MovementType
import com.lenta.movement.models.TaskType
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.helper.LocalTableResourceHelper
import com.mobrun.plugin.api.request_assistant.CustomParameter
import com.mobrun.plugin.api.request_assistant.RequestBuilder
import com.mobrun.plugin.api.request_assistant.ScalarParameter
import com.mobrun.plugin.models.StatusSelectTable

/**
 * Справочник расшифровки заданий
 */
class ZmpUtz102V001(private val hyperHive: HyperHive) {
    val localHelper_ET_MVM_TXT: LocalTableResourceHelper<Item_Local_ET_MVM_TXT, Status_ET_MVM_TXT>

    fun newRequest(): RequestBuilder<Params, LimitedScalarParameter> {
        return RequestBuilder(hyperHive, NAME_RESOURCE, true)
    }

    class Status_ET_MVM_TXT : StatusSelectTable<Item_Local_ET_MVM_TXT?>()

    class Item_Local_ET_MVM_TXT {
        @SerializedName("MVM_TYPE")
        var taskType: String? = null

        @SerializedName("MVM_TXT")
        var taskTypeTxt: String? = null
    }

    interface Params : CustomParameter

    class LimitedScalarParameter(name: String?, value: Any?): ScalarParameter<Any>(name, value) {
        companion object {
            fun IV_NODEPLOY(value: String?): LimitedScalarParameter {
                return LimitedScalarParameter("IV_NODEPLOY", value)
            }
        }
    }

    companion object {
        const val NAME_RESOURCE = "ZMP_UTZ_102_V001"
        const val NAME_OUT_PARAM_ET_TASK_TSP = "ET_MVM_TXT"
        const val LIFE_TIME = "1 day, 0:00:00"
    }

    init {
        localHelper_ET_MVM_TXT = LocalTableResourceHelper<Item_Local_ET_MVM_TXT, Status_ET_MVM_TXT>(
                NAME_RESOURCE,
                NAME_OUT_PARAM_ET_TASK_TSP,
                hyperHive,
                Status_ET_MVM_TXT::class.java
        )
    }
}