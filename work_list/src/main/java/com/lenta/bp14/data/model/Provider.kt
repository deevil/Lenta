package com.lenta.bp14.data.model

import com.lenta.shared.utilities.extentions.getFormattedDate
import java.util.*


data class Provider(
        val id: Int,
        val code: String,
        val name: String,
        val kipStart: Date,
        val kipEnd: Date
) {

    fun getKipPeriod(): String {
        return "${kipStart.getFormattedDate()} - ${kipEnd.getFormattedDate()}"
    }

}