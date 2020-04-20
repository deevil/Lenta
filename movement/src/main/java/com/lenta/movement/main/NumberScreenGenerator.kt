package com.lenta.movement.main

import com.lenta.shared.platform.activity.INumberScreenGenerator
import com.lenta.shared.platform.fragment.CoreFragment
import javax.inject.Inject

class NumberScreenGenerator @Inject constructor() : INumberScreenGenerator {

    companion object {
        const val prefix = "10" // TODO уточнить номер, так как тот что в дизайне уже используется в writeoff breakage
    }

    override fun generateNumberScreenFromPostfix(postfix: String?): String? {
        return if (postfix == null) null else "$prefix/$postfix"
    }

    override fun generateNumberScreen(fragment: CoreFragment<*, *>): String? {
        return generateNumberScreenFromPostfix(when (fragment) {
            else -> null
        })
    }

    override fun getPrefixScreen(fragment: CoreFragment<*, *>): String {
        return prefix
    }
}