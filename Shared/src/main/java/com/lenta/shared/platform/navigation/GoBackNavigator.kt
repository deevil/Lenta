package com.lenta.shared.platform.navigation

import android.os.Bundle


interface IGoBackNavigator {
    fun goBack(args: Bundle? = null)
}