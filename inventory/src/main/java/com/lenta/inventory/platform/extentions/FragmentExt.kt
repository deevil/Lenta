package com.lenta.inventory.platform.extentions

import com.lenta.inventory.R
import com.lenta.inventory.di.AppComponent
import com.lenta.inventory.main.MainActivity
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.utilities.extentions.implementationOf

fun CoreFragment<*, *>.getAppComponent(): AppComponent? {
    return activity.implementationOf(MainActivity::class.java)?.appComponent
}
