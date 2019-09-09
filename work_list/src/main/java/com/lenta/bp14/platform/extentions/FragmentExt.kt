package com.lenta.bp14.platform.extentions

import com.lenta.bp14.di.AppComponent
import com.lenta.bp14.main.MainActivity
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.utilities.extentions.implementationOf

fun CoreFragment<*, *>.getAppComponent(): AppComponent? {
    return activity.implementationOf(MainActivity::class.java)?.appComponent
}