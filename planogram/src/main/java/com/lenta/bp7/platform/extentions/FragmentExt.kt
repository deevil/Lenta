package com.lenta.bp7.platform.extentions

import com.lenta.bp7.activity.main.MainActivity
import com.lenta.bp7.di.AppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.utilities.extentions.implementationOf

fun CoreFragment<*, *>.getAppComponent(): AppComponent? {
    return activity.implementationOf(MainActivity::class.java)?.appComponent
}
