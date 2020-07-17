package com.lenta.shared.fmp.resources.fast

import com.google.gson.annotations.SerializedName
import com.mobrun.plugin.api.HyperHive
import com.mobrun.plugin.api.helper.LocalTableResourceHelper
import com.mobrun.plugin.api.request_assistant.CustomParameter
import com.mobrun.plugin.api.request_assistant.RequestBuilder
import com.mobrun.plugin.api.request_assistant.ScalarParameter
import com.mobrun.plugin.models.StatusSelectTable

//Получение списка объектов
class ZmpUtzPro10V001(private val hyperHive: HyperHive) {

    val localHelper_ET_OBJ_LIST: LocalTableResourceHelper<ItemLocal_ET_OBJ_LIST, Status_ET_OBJ_LIST>

    init {
        localHelper_ET_OBJ_LIST = LocalTableResourceHelper<ItemLocal_ET_OBJ_LIST, Status_ET_OBJ_LIST>(NAME_RESOURCE,
                NAME_OUT_PARAM_ET_UOMS,
                hyperHive,
                Status_ET_OBJ_LIST::class.java)
    }

    fun newRequest(): RequestBuilder<Params, LimitedScalarParameter> {
        return RequestBuilder(hyperHive, NAME_RESOURCE, true)
    }

    class Status_ET_OBJ_LIST : StatusSelectTable<ItemLocal_ET_OBJ_LIST>()

    data class ItemLocal_ET_OBJ_LIST(
            // Код объекта
            //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
            @SerializedName("OBJ_CODE")
            val code: String? = null,

            // Тип объекта: 4 – Заказ , 5 - материал
            //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
            @SerializedName("OBJ_TYPE")
            val objType: String? = null,

            // Тип блокировки:
            //1 - Своя блокировка
            //2 – Чужая блокировка
            @SerializedName("BLOCK_TYPE")
            val blockType: String? = null,

            //Имя пользователя
            @SerializedName("LOCK_USER")
            val lockUser: String? = null,

            //IP адрес ТСД
            @SerializedName("LOCK_IP")
            val lockIp: String? = null,

            //Признак «Обработка начата»
            @SerializedName("IS_PLAY")
            val isPlay: Boolean? = null,

            // Признак «Обработка завершена»
            @SerializedName("IS_DONE")
            val isDone: Boolean? = null,

            // Текст первой строки
            @SerializedName("TEXT1")
            val text1: String? = null,

            // Текст второй строки
            @SerializedName("TEXT2")
            val text2: String? = null,

            // Текст третьей строки
            @SerializedName("TEXT3")
            val text3: String? = null,

            // Наименование итогового полуфабриката
            @SerializedName("NAME_MATNR_OSN")
            val nameMatnrOsn: String? = null,

            // Склад
            @SerializedName("LGORT")
            val lgort: String? = null,

            // Плановое количество итогового ПФ
            @SerializedName("PLAN_QNT")
            val planQnt: String? = null,

            // Фактическое количество итогового ПФ, на которое уже скомплектованы ингредиенты
            @SerializedName("DONE_QNT")
            val doneQnt: String? = null
    )

    interface Params : CustomParameter
    class LimitedScalarParameter(name: String?, value: Any?) : ScalarParameter<Any?>(name, value) {
        companion object {
            fun IV_NODEPLOY(value: String?): LimitedScalarParameter {
                return LimitedScalarParameter("IV_NODEPLOY", value)
            }
        }
    }

    companion object {
        //Справочник складов централизованного производства
        const val NAME_RESOURCE = "ZMP_UTZ_PRO_10_V001"
        const val NAME_OUT_PARAM_ET_UOMS = "ET_OBJ_LIST"
        const val LIFE_TIME = "1 day, 0:00:00"
    }
}