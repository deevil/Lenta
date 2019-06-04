package com.lenta.inventory.platform.extentions

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.lenta.inventory.R
import com.lenta.inventory.di.AppComponent
import com.lenta.inventory.main.MainActivity
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.utilities.extentions.implementationOf

fun <T : ViewDataBinding, S : ViewModel> CoreFragment<T, S>.getAppComponent(): AppComponent? {
    return activity.implementationOf(MainActivity::class.java)?.appComponent
}

fun <T : ViewDataBinding, S : ViewModel> CoreFragment<T, S>.getAppTitle(): String {
    context?.let {
        return "${getString(R.string.app_name)} v${it.packageManager?.getPackageInfo(it.packageName, 0)?.versionName}"
    }
    return ""

}