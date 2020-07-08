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

    override fun typeQuantity(): String = context.getString(R.string.type_quantity)

    override fun typeMark(): String = context.getString(R.string.type_mark)

    override fun typePart(): String = context.getString(R.string.type_part)

    override fun chooseProvider(): String = context.getString(R.string.choose_provider)

    override fun chooseProducer(): String = context.getString(R.string.choose_producer)

    override fun totalWithConvertingInfo(info: String): String = context.getString(R.string.total_with_converting_info, info)

    override fun byBasket(): String = context.getString(R.string.by_basket)

    override fun basketTitle(description: String): String = context.getString(R.string.basket_title, description)
}

interface IResourceManager {

    fun tkNumber(number: String): String
    fun backSalesFromDate(date: String): String
    fun alcocodeDoesNotApplyToThisGood(): String
    fun unknownAlcocode(): String
    fun typeQuantity(): String
    fun typeMark(): String
    fun typePart(): String
    fun chooseProvider(): String
    fun chooseProducer(): String
    fun totalWithConvertingInfo(info: String): String
    fun byBasket(): String
    fun basketTitle(description: String): String

}