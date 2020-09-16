package com.lenta.bp16.model.ingredients

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.lenta.bp16.model.ingredients.ui.IngredientInfoUI
import com.lenta.bp16.platform.converter.IConvertable
import com.lenta.shared.utilities.orIfNull
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IngredientInfo(
        // Код объекта
        @SerializedName("OBJ_CODE")
        val code: String?,

        // Тип объекта: 4 – Заказ , 5 - материал
        @SerializedName("OBJ_TYPE")
        val objType: String?,

        // Тип блокировки:
        //1 - Своя блокировка
        //2 – Чужая блокировка
        @SerializedName("BLOCK_TYPE")
        val blockType: String?,

        //Имя пользователя
        @SerializedName("LOCK_USER")
        val lockUser: String?,

        //IP адрес ТСД
        @SerializedName("LOCK_IP")
        val lockIp: String?,

        //Признак «Обработка начата»
        @SerializedName("IS_PLAY")
        val isPlay: Boolean?,

        // Признак «Обработка завершена»
        @SerializedName("IS_DONE")
        val isDone: Boolean?,

        // Текст первой строки
        @SerializedName("TEXT1")
        val text1: String?,

        // Текст второй строки
        @SerializedName("TEXT2")
        val text2: String?,

        // Текст третьей строки
        @SerializedName("TEXT3")
        val text3: String?,

        // Наименование итогового полуфабриката
        @SerializedName("NAME_MATNR_OSN")
        val nameMatnrOsn: String?,

        // Склад
        @SerializedName("LGORT")
        val lgort: String?,

        // Плановое количество итогового ПФ
        @SerializedName("PLAN_QNT")
        val planQnt: Double?,

        // Фактическое количество итогового ПФ, на которое уже скомплектованы ингредиенты
        @SerializedName("DONE_QNT")
        val doneQnt: String?
) : Parcelable, IConvertable<IngredientInfoUI?> {
    override fun convert(): IngredientInfoUI? {
        return IngredientInfoUI(
                code = code.orEmpty(),
                objType = objType.orEmpty(),
                blockType = blockType.orEmpty(),
                lockUser = lockUser.orEmpty(),
                lockIp = lockIp.orEmpty(),
                isPlay = isPlay.orIfNull { false },
                isDone = isDone.orIfNull { false },
                text1 = text1.orEmpty(),
                text2 = text2.orEmpty(),
                text3 = text3.orEmpty(),
                nameMatnrOsn = nameMatnrOsn.orEmpty(),
                lgort = lgort.orEmpty(),
                planQnt = planQnt.orIfNull { 0.0 },
                doneQnt = doneQnt.orEmpty()
        )
    }
}