package com.lenta.bp12.platform.resource

import android.content.Context
import com.lenta.bp12.R

import javax.inject.Inject

class ResourceManager @Inject constructor(
        val context: Context
) : IResourceManager {

    override fun tkNumber(number: String): String = context.getString(R.string.tk_number, number)

    override fun backSalesFromDate(date: String): String = context.getString(R.string.back_sales_from_date, date)

    override fun alcocodeDoesNotApplyToThisGood(): String = context.getString(R.string.alcocode_does_not_apply_to_this_good)

    override fun unknownAlcocode(): String = context.getString(R.string.unknown_alcocode)
}

interface IResourceManager {

    fun tkNumber(number: String): String
    fun backSalesFromDate(date: String): String

    fun alcocodeDoesNotApplyToThisGood(): String
    fun unknownAlcocode(): String

}