package com.lenta.bp9.platform.extentions

import com.lenta.bp9.di.AppComponent
import com.lenta.bp9.main.MainActivity
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.utilities.extentions.implementationOf

fun CoreFragment<*, *>.getAppComponent(): AppComponent? {
    return activity.implementationOf(MainActivity::class.java)?.appComponent
}
