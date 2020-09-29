package com.lenta.bp12.platform.resource

import android.content.Context
import com.lenta.bp12.R
import com.lenta.shared.utilities.extentions.getDeviceIp

import javax.inject.Inject

class ResourceManager @Inject constructor(
        val context: Context
) : IResourceManager {

    override val deviceIp: String by lazy { context.getDeviceIp() }
    override val goodNotFoundErrorMsg: String by lazy { context.getString(R.string.good_not_found_error_msg) }
    override val goodsNotFoundErrorMsg: String by lazy { context.getString(R.string.goods_not_found_error_msg) }
    override val basketNotFoundErrorMsg: String by lazy { context.getString(R.string.basket_not_found_error_msg) }
    override val taskNotFoundErrorMsg: String by lazy { context.getString(R.string.task_not_found_error_msg) }
    override val pageNotFoundErrorMsg: String by lazy { context.getString(R.string.page_not_found_error_msg ) }
    override val rub: String by lazy { context.getString(R.string.rub) }
    override val wrongDate: String by lazy { context.getString(R.string.wrong_date_error_msg) }
    override val error: String by lazy { context.getString(R.string.error) }
    override val noStatusMark: String by lazy { context.getString(R.string.no_status_mark) }

    override fun tk(number: String): String = context.getString(R.string.tk_number, number)

    override fun backSalesFromDate(date: String): String = context.getString(R.string.back_sales_from_date, date)

    override fun allSuppliers(): String = context.getString(R.string.all_suppliers)

    override fun wholesaleBuyer(): String = context.getString(R.string.wholesale_buyer)

    override fun alcocodeDoesNotApplyToThisGood(): String = context.getString(R.string.alcocode_does_not_apply_to_this_good)

    override fun unknownAlcocode(): String = context.getString(R.string.unknown_alcocode)

    override fun typeQuantity(): String = context.getString(R.string.type_quantity)

    override fun typeMark(): String = context.getString(R.string.type_mark)

    override fun typePart(): String = context.getString(R.string.type_part)

    override fun chooseProvider(): String = context.getString(R.string.choose_provider)

    override fun chooseProducer(): String = context.getString(R.string.choose_producer)

    override fun totalWithConvertingInfo(info: String): String = context.getString(R.string.total_with_converting_info, info)

    override fun byBasket(): String = context.getString(R.string.by_basket)

    override fun basket(description: String): String = context.getString(R.string.basket_title, description)

    override fun goodList(): String = context.getString(R.string.good_list)

    override fun taskContent(): String = context.getString(R.string.task_content)

    override fun mrcDashCostRub(mrc: String): String = context.getString(R.string.mrc_dash_cost_rub, mrc)

    override fun mrcSpaceRub(mrc: String): String = context.getString(R.string.mrc_space_rub, mrc)
}

interface IResourceManager {

    val deviceIp: String
    val goodNotFoundErrorMsg: String
    val goodsNotFoundErrorMsg: String
    val basketNotFoundErrorMsg: String
    val taskNotFoundErrorMsg: String
    val pageNotFoundErrorMsg: String
    val noStatusMark: String
    val rub: String
    val wrongDate: String
    val error: String

    fun tk(number: String): String
    fun backSalesFromDate(date: String): String
    fun allSuppliers(): String
    fun wholesaleBuyer(): String
    fun alcocodeDoesNotApplyToThisGood(): String
    fun unknownAlcocode(): String
    fun typeQuantity(): String
    fun typeMark(): String
    fun typePart(): String
    fun chooseProvider(): String
    fun chooseProducer(): String
    fun totalWithConvertingInfo(info: String): String
    fun byBasket(): String
    fun basket(description: String): String
    fun goodList(): String
    fun taskContent(): String
    fun mrcDashCostRub(mrc: String): String
    fun mrcSpaceRub(mrc: String): String

}