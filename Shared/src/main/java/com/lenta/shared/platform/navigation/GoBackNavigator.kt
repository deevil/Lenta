package com.lenta.shared.platform.navigation

import android.os.Bundle


interface IGoBackNavigator {
    fun goBackWithArgs(args: Bundle)
    fun goBack()
}