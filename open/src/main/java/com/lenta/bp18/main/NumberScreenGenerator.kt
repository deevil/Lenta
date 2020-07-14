package com.lenta.bp18.main

import com.lenta.bp18.features.auth.AuthFragment
import com.lenta.bp18.features.good_info.GoodInfoFragment
import com.lenta.bp18.features.select_good.SelectGoodFragment
import com.lenta.bp18.features.select_market.SelectMarketFragment
import com.lenta.bp18.platform.Constants
import com.lenta.shared.features.alert.AlertFragment
import com.lenta.shared.features.auxiliary_menu.AuxiliaryMenuFragment
import com.lenta.shared.features.select_oper_mode.SelectOperModeFragment
import com.lenta.shared.features.settings.SettingsFragment
import com.lenta.shared.features.support.SupportFragment
import com.lenta.shared.features.tech_login.TechLoginFragment
import com.lenta.shared.features.test_environment.PinCodeFragment
import com.lenta.shared.platform.activity.INumberScreenGenerator
import com.lenta.shared.platform.fragment.CoreFragment
import javax.inject.Inject

class NumberScreenGenerator @Inject constructor() : INumberScreenGenerator {

    override fun generateNumberScreenFromPostfix(postfix: String?): String? {
        return if (postfix == null) null else "$prefix/$postfix"
    }

    override fun generateNumberScreen(fragment: CoreFragment<*, *>): String? {
        return generateNumberScreenFromPostfix(when (fragment) {
            is AuthFragment -> Constants.AUTH_FRAGMENT
            is SelectMarketFragment -> Constants.SELECT_MARKET_FRAGMENT
            is SelectGoodFragment -> Constants.SELECT_GOODS_FRAGMENT
            is GoodInfoFragment -> Constants.GOODS_INFO_FRAGMENT
            is AlertFragment -> Constants.ALERT_SCREEN_NUMBER
            is AuxiliaryMenuFragment -> Constants.AUXILIARY_MENU_FRAGMENT
            is SelectOperModeFragment -> Constants.SELECT_OPER_MODE_FRAGMENT
            is SettingsFragment -> Constants.SETTINGS_FRAGMENT
            is SupportFragment -> Constants.SUPPORT_FRAGMENT
            is TechLoginFragment -> Constants.TECH_LOGIN_FRAGMENT
            is PinCodeFragment -> Constants.PINCODE_FRAGMENT
            else -> null
        }
        )
    }

    override fun getPrefixScreen(fragment: CoreFragment<*, *>): String {
        return prefix
    }

    companion object {
        const val prefix = "18"
    }

}