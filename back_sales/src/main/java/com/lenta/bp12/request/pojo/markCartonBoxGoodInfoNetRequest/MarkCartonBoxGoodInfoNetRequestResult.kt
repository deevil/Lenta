package com.lenta.bp12.request.pojo.markCartonBoxGoodInfoNetRequest

import com.google.gson.annotations.SerializedName
import com.lenta.bp12.request.pojo.MaterialInfo
import com.lenta.shared.requests.SapResponse

data class MarkCartonBoxGoodInfoNetRequestResult(
        /** Статус марки */
        @SerializedName("EV_STAT")
        val markStatus: MarkRequestStatus,
        /** Текст статуса */
        @SerializedName("EV_STAT_TEXT")
        val markStatusText: String,
        /** Список марок задания для передачи в МП */
        @SerializedName("ET_MARKS")
        val marks: List<MarkInfo>?,
        /** Свойства по товарам */
        @SerializedName("ET_PROPERTIES")
        val properties: List<PropertiesInfo>?,
        /** Справочные данные товара (заполнять, если IV_MATNR = « ») */
        @SerializedName("ET_MATERIALS")
        val materials: List<MaterialInfo>?,
        /** Код возврата */
        @SerializedName("EV_RETCODE")
        override val retCode: Int?,
        /** Текст ошибки */
        @SerializedName("EV_ERROR_TEXT")
        override val errorText: String?
) : SapResponse