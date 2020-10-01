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
    override val allSuppliers: String by lazy { context.getString(R.string.all_suppliers)  }
    override val wholesaleBuyer: String by lazy { context.getString(R.string.wholesale_buyer) }
    override val alcocodeDoesNotApplyToThisGood: String by lazy { context.getString(R.string.alcocode_does_not_apply_to_this_good) }
    override val unknownAlcocode: String by lazy { context.getString(R.string.unknown_alcocode) }
    override val typeQuantity: String by lazy { context.getString(R.string.type_quantity) }
    override val typeMark: String by lazy { context.getString(R.string.type_mark) }
    override val typePart: String by lazy { context.getString(R.string.type_part) }
    override val chooseProvider: String by lazy { context.getString(R.string.choose_provider) }
    override val chooseProducer: String by lazy { context.getString(R.string.choose_producer) }
    override val byBasket: String by lazy { context.getString(R.string.by_basket) }
    override val goodList: String by lazy { context.getString(R.string.good_list) }
    override val taskContent: String by lazy { context.getString(R.string.task_content) }


    override fun tk(number: String): String = context.getString(R.string.tk_number, number)
    override fun backSalesFromDate(date: String): String = context.getString(R.string.back_sales_from_date, date)
    override fun totalWithConvertingInfo(info: String): String = context.getString(R.string.total_with_converting_info, info)
    override fun basket(description: String): String = context.getString(R.string.basket_title, description)
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
    val allSuppliers: String
    val wholesaleBuyer: String
    val alcocodeDoesNotApplyToThisGood: String
    val unknownAlcocode: String
    val typeQuantity: String
    val typeMark: String
    val typePart: String
    val chooseProvider: String
    val chooseProducer: String
    val goodList: String
    val taskContent: String
    val byBasket: String

    fun tk(number: String): String
    fun backSalesFromDate(date: String): String
    fun totalWithConvertingInfo(info: String): String
    fun basket(description: String): String
    fun mrcDashCostRub(mrc: String): String
    fun mrcSpaceRub(mrc: String): String

}