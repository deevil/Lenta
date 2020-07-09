package com.lenta.movement.main

import com.lenta.movement.platform.Constants
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
            is ExitWithConfirmationFragment -> Constants.EXIT_WITH_CONFIRMATION_FRAGMENT
            is AuxiliaryMenuFragment -> Constants.AUXILIARY_MENU_FRAGMENT
            is FmpSettingsFragment -> Constants.FMP_SETTINGS_FRAGMENT
            is PrinterChangeFragment -> Constants.PRINTER_CHANGE_FRAGMENT
            is SelectOperModeFragment -> Constants.SELECT_OPERMODE_FRAGMENT
            is SettingsFragment -> Constants.SETTINGS_FRAGMENT
            is SupportFragment -> Constants.SUPPORT_FRAGMENT
            is TechLoginFragment -> Constants.TECH_LOGIN_FRAGMENT
            is PinCodeFragment -> Constants.PINCODE_FRAGMENT
            is FailurePinCodeFragment -> Constants.FAILURE_PINCODE_FRAGMENT
           /* is GoodsInfoFragment -> "09"
            is ExciseAlcoInfoFragment -> "09"
            is GoodsDetailsStorageFragment -> "22"
            is SetsInfoFragment -> "13"
            is SetComponentsFragment -> "14"
            is TaskListFragment -> "05"
            is JobCardFragment -> "06"
            is PartySignsFragment -> "89"*/
            else -> null
        }
        )
    }

    override fun getPrefixScreen(fragment: CoreFragment<*, *>): String {
        return prefix
    }

    companion object {
        const val prefix = "11"
    }
}