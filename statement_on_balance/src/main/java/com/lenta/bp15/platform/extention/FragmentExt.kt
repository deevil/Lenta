package com.lenta.bp15.platform.extention

import com.lenta.bp15.di.AppComponent
import com.lenta.bp15.main.MainActivity
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.utilities.extentions.implementationOf

fun CoreFragment<*, *>.getAppComponent(): AppComponent? {
    return activity.implementationOf(MainActivity::class.java)?.appComponent
}