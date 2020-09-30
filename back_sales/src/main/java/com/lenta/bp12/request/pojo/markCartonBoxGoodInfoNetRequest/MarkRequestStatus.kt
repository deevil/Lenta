package com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest

import com.google.gson.annotations.SerializedName

/**
 * @see MarkCartonBoxGoodInfoNetRequestResult
 * */
enum class MarkRequestStatus(val code: String) {
    /** Статус марки */

    /** Марка найдена в сети «Лента» */
    @SerializedName("0")
    MARK_FOUND("0"),
    /** Марка не найдена в сети «Лента» или является проблемной */
    @SerializedName("1")
    MARK_NOT_FOUND_OR_PROBLEMATIC("1"),
    /** Марка числится в сети «Лента» и относится к другому товару */
    @SerializedName("2")
    MARK_OF_DIFFERENT_GOOD("2"),
    /** Марка не найдена в задании (если по товару есть заказ) */
    @SerializedName("3")
    MARK_NOT_FOUND_IN_TASK("3"),

    /** Статус блока: */

    /** Блок найден в сети «Лента» или по блоку есть признак GRAYZONE=X */
    @SerializedName("100")
    CARTON_FOUND_OR_GRAYZONE("100"),
    /** Блок не найден в сети «Лента» */
    @SerializedName("101")
    CARTON_NOT_FOUND("101"),
    /** Блок числится в сети «Лента» и относится к другому товару */
    @SerializedName("102")
    CARTON_OF_DIFFERENT_GOOD("102"),
    /** Нецелый блок (в блоке есть марки серой зоны, кол-во пачек не равно NQNT) */
    @SerializedName("103")
    CARTON_INCOMPLETE("103"),
    /** Блок не найден в задании (если по товару есть заказ) */
    @SerializedName("104")
    CARTON_NOT_FOUND_IN_TASK("104"),
    /** старый блок */
    @SerializedName("105")
    CARTON_OLD("105"),
    /** МРЦ в системе другая */
    @SerializedName("106")
    CARTON_NOT_SAME_IN_SYSTEM("106"),

    /** Статус коробки: */

    /** Коробка найдена в сети «Лента» */
    @SerializedName("200")
    BOX_FOUND("200"),
    /** Коробка не найдена в сети «Лента» */
    @SerializedName("201")
    BOX_NOT_FOUND("201"),
    /** Коробка числится в сети «Лента» и относится к другому товару */
    @SerializedName("202")
    BOX_OF_DIFFERENT_GOOD("202"),
    /** Нецелый короб (в коробе есть марки пачки/блока серой зоны, кол-во пачек не равно NQNT) */
    @SerializedName("203")
    BOX_INCOMPLETE("203"),
    /** Коробка не найдена в задании (если по товару есть заказ) */
    @SerializedName("204")
    BOX_NOT_FOUND_IN_TASK("204"),
    /** ??? Коробка не та в системе */
    @SerializedName("205")
    BOX_NOT_SAME_IN_SYSTEM("205")
}