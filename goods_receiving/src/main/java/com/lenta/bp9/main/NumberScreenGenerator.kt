package com.lenta.bp9.main

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
            is ExitWithConfirmationFragment -> "93"
            is AuxiliaryMenuFragment -> "50"
            is FmpSettingsFragment -> "100"
            is PrinterChangeFragment -> "53"
            is SelectOperModeFragment -> "54"
            is SettingsFragment -> "51"
            is SupportFragment -> "52"
            is TechLoginFragment -> "55"
            is PinCodeFragment -> "56"
            is FailurePinCodeFragment -> "96"
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