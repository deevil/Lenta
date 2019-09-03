package com.lenta.bp10.platform.extentions

import com.lenta.bp10.R
import com.lenta.bp10.activity.main.MainActivity
import com.lenta.bp10.di.AppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.utilities.extentions.implementationOf

fun CoreFragment<*, *>.getAppComponent(): AppComponent? {
    return activity.implementationOf(MainActivity::class.java)?.appComponent
}
