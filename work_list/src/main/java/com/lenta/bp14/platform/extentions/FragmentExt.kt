package com.lenta.bp14.platform.extentions

import com.lenta.bp14.R
import com.lenta.bp14.di.AppComponent
import com.lenta.bp14.main.MainActivity
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.utilities.extentions.implementationOf

fun CoreFragment<*, *>.getAppComponent(): AppComponent? {
    return activity.implementationOf(MainActivity::class.java)?.appComponent
}

fun CoreFragment<*, *>.getAppTitle(): String {
    context?.let {
        return "${getString(R.string.app_name)} v${it.packageManager?.getPackageInfo(it.packageName, 0)?.versionName}"
    }
    return ""

}