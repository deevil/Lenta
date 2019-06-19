package com.lenta.bp10.activity.main

import com.lenta.bp10.features.good_information.general.GoodInfoFragment
import com.lenta.bp10.features.good_information.sets.SetsFragment
import com.lenta.bp10.features.good_information.sets.component.ComponentFragment
import com.lenta.bp10.features.goods_list.GoodsListFragment
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

    override fun generateNumberScreen(fragment: CoreFragment<*, *>): String? {
        return when (fragment) {
            is ExitWithConfirmationFragment -> "$prefix/93"
            is AuxiliaryMenuFragment -> "$prefix/50"
            is FmpSettingsFragment -> "$prefix/100"
            is PrinterChangeFragment -> "$prefix/53"
            is SelectOperModeFragment -> "$prefix/54"
            is SettingsFragment -> "$prefix/51"
            is SupportFragment -> "$prefix/52"
            is TechLoginFragment -> "$prefix/55"
            is PinCodeFragment -> "$prefix/56"
            is FailurePinCodeFragment -> "$prefix/96"
            is SetsFragment -> "$prefix/10"
            is GoodsListFragment -> "$prefix/06"
            is GoodInfoFragment -> "$prefix/07"
            is ComponentFragment -> "$prefix/11"
            else -> null
        }
    }

    override fun getPrefixScreen(fragment: CoreFragment<*, *>): String {
        return  prefix
    }

    companion object {
        const val prefix = "10"
    }

}