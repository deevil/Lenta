package com.lenta.bp10.platform.extentions

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.lenta.bp10.activity.main.MainActivity
import com.lenta.bp10.di.AppComponent
import com.lenta.shared.platform.fragment.CoreFragment
import com.lenta.shared.utilities.extentions.implementationOf

fun <T : ViewDataBinding, S : ViewModel> CoreFragment<T, S>.getAppComponent(): AppComponent? {
    return activity.implementationOf(MainActivity::class.java)?.appComponent
}