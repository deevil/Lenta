package com.lenta.shared.utilities.extentions

import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import com.lenta.shared.platform.activity.main_activity.CoreMainActivity
import com.lenta.shared.platform.fragment.CoreFragment

fun <T : ViewModel> Fragment.provideViewModel(clazz: Class<T>): T {
    return ViewModelProviders.of(this).get(clazz)
}

fun <T : ViewDataBinding, S : ViewModel> CoreFragment<T, S>.generateScreenNumber(): String {
    return activity.implementationOf(CoreMainActivity::class.java)?.generateNumberScreen(this) ?: ""
}

fun <T : ViewDataBinding, S : ViewModel> CoreFragment<T, S>.getScreenPrefix(): String {
    return activity.implementationOf(CoreMainActivity::class.java)?.getPrefixScreen(this) ?: ""
}