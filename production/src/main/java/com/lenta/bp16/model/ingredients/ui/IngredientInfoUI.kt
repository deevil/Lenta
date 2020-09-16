package com.lenta.bp16.model.ingredients.ui

import android.os.Parcelable
import com.lenta.shared.utilities.extentions.dropZeros
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IngredientInfoUI(
        // Код объекта
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        val code: String,

        // Тип объекта: 4 – Заказ , 5 - материал
        //  type: TEXT, source: {'name': 'SAP', 'type': 'C'}
        val objType: String,

        // Тип блокировки:
        //1 - Своя блокировка
        //2 – Чужая блокировка
        val blockType: String,

        //Имя пользователя
        val lockUser: String,

        //IP адрес ТСД
        val lockIp: String,

        //Признак «Обработка начата»
        val isPlay: Boolean,

        // Признак «Обработка завершена»
        val isDone: Boolean,

        // Текст первой строки
        val text1: String,

        // Текст второй строки
        val text2: String,

        // Текст третьей строки
        val text3: String,

        // Наименование итогового полуфабриката
        val nameMatnrOsn: String,

        // Склад
        val lgort: String,

        // Плановое количество итогового ПФ
        val planQnt: Double,

        // Фактическое количество итогового ПФ, на которое уже скомплектованы ингредиенты
        val doneQnt: String
) : Parcelable {
    val isByOrder: Boolean
        get() = objType == TYPE_ORDER

    val isByMaterial: Boolean
        get() = objType == TYPE_MATERIAL

    fun getPlanQntStr(): String {
        return planQnt.dropZeros()
    }

    fun getFormattedCode(): String? {
        return code.takeLast(6)
    }

    companion object {
        const val TYPE_ORDER = "4"
        const val TYPE_MATERIAL = "5"

        const val BLOCK_BY_MYSELF = "1"
        const val BLOCK_BY_OTHER = "2"

        const val MODE_ORDER_BLOCK_DATA = "5" // 5 – Получение данных с блокировкой заказа
        const val MODE_ORDER_RE_BLOCK_DATA = "6" // 6 – Получение данных с переблокировкой заказа

        const val MODE_MATERIAL_BLOCK_DATA = "7" // 7 – Получение данных с блокировкой заказа
        const val MODE_MATERIAL_RE_BLOCK_DATA = "8" // 8 – Получение данных с переблокировкой заказа
    }
}