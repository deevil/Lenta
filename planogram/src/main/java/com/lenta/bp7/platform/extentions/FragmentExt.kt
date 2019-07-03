package com.lenta.bp7.platform.extentions

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.lenta.bp7.activity.main.MainActivity
import com.lenta.bp7.R
import com.lenta.bp7.di.AppComponent
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