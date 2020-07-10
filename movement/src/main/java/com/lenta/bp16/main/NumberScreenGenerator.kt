package com.lenta.bp16.main

import com.lenta.bp16.features.goods_info.GoodsInfoFragment
import com.lenta.bp16.features.goods_irrelevant_info.IrrelevantGoodsInfoFragment
import com.lenta.bp16.features.goods_select.GoodsSelectFragment
import com.lenta.bp16.features.goods_without_manufacturer.GoodsWithoutManufacturerFragment
import com.lenta.bp16.platform.Constants
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.features.auxiliary_menu.AuxiliaryMenuFragment
import com.lenta.shared.features.exit.ExitWithConfirmationFragment
import com.lenta.shared.features.fmp_settings.FmpSettingsFragment
import com.lenta.shared.features.printer_change.PrinterChangeFragment
import com.lenta.shared.features.select_oper_mode.SelectOperModeFragment
import com.lenta.shared.features.settings.SettingsFragment
import com.lenta.shared.features.support.SupportFragment
import com.lenta.shared.features.tech_login.TechLoginFragment
import com.lenta.shared.features.test_environment.PinCodeFragment
import com.lenta.shared.features.test_environment.failure.FailurePinCodeFragment
import com.lenta.shared.platform.activity.INumberScreenGenerator
import com.lenta.shared.platform.fragment.CoreFragment
import javax.inject.Inject

class NumberScreenGenerator @Inject constructor() : INumberScreenGenerator {

    override fun generateNumberScreenFromPostfix(postfix: String?): String? {
        return if (postfix == null) null else "$prefix/$postfix"
    }

    override fun generateNumberScreen(fragment: CoreFragment<*, *>): String? {
        return generateNumberScreenFromPostfix(when (fragment) {
            is GoodsInfoFragment -> Constants.GOODS_INFO_FRAGMENT
            is IrrelevantGoodsInfoFragment -> Constants.GOODS_INFO_FRAGMENT
            is GoodsSelectFragment -> Constants.SELECT_GOODS_FRAGMENT
            is GoodsWithoutManufacturerFragment -> Constants.GOODS_INFO_FRAGMENT
            is AlertFragment -> Constants.ALERT_FRAGMENT
            else -> null
        }
        )
    }

    override fun getPrefixScreen(fragment: CoreFragment<*, *>): String {
        return prefix
    }

    companion object {
        const val prefix = Constants.BP
    }
}