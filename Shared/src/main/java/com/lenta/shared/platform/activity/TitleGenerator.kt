package com.lenta.shared.platform.activity

import com.lenta.shared.platform.fragment.CoreFragment


interface INumberScreenGenerator {
    fun generateNumberScreen(fragment: CoreFragment<*, *>): String
}