package com.lenta.bp16.model.ingredients

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.lenta.shared.utilities.extentions.dropZeros
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IngredientInfo(
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
        val planQnt: Double? = null,

        // Фактическое количество итогового ПФ, на которое уже скомплектованы ингредиенты
        @SerializedName("DONE_QNT")
        val doneQnt: String? = null
) : Parcelable {

        val isByOrder: Boolean
                get() = objType == TYPE_ORDER

        val isByMaterial: Boolean
                get() = objType == TYPE_MATERIAL

        fun getPlanQntStr(): String {
                return planQnt.dropZeros()
        }

        companion object {
                const val TYPE_ORDER = "4"
                const val TYPE_MATERIAL = "5"

                const val BLOCK_BY_MYSELF = "1"
                const val BLOCK_BY_OTHER = "2"

                const val MODE_BLOCK_DATA = "5" // 5 – Получение данных с блокировкой заказа
                const val MODE_RE_BLOCK_DATA = "6" // 6 – Получение данных с переблокировкой заказа
        }
}