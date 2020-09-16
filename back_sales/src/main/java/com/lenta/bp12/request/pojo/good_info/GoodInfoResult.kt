package com.lenta.bp12.request.pojo.good_info

import com.google.gson.annotations.SerializedName
import com.lenta.bp12.request.pojo.*
import com.lenta.shared.requests.SapResponse

/**
 * Результат получения данных товара по ШК\SAP-коду
 * "ZMP_UTZ_BKS_05_V001"
 * @see com.lenta.bp12.request.GoodInfoNetRequest
 */
data class GoodInfoResult(
        /** Справочник ШК */
        @SerializedName("ES_EAN")
        val eanInfo: EanInfo?,
        /** Справочник товаров */
        @SerializedName("ES_MATERIAL")
        val materialInfo: MaterialInfo?,
        /** Таблица наборов */
        @SerializedName("ET_SET")
        val sets: List<SetInfo>?, // Для BKS не используется
        /** Таблица поставщиков */
        @SerializedName("ET_LIFNR")
        val providers: List<ProviderInfo>?,
        /** Таблица производителей */
        @SerializedName("ET_PROD")
        val producers: List<ProducerInfo>?,
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int?,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String?
) : SapResponse