package com.lenta.shared.requests.combined.scan_info.pojo

import com.google.gson.annotations.SerializedName

data class GroupInfo(
        /**Код предприятия*/
        @SerializedName("WERKS")
        val werks: String,
        /**Код группы весового оборудования*/
        @SerializedName("GRNUM")
        val number: String,
        /**Наименование группы весового оборудования*/
        @SerializedName("GRNAME")
        val name: String
)