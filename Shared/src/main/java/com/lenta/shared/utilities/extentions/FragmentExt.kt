package com.lenta.shared.utilities.extentions

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.lenta.shared.platform.activity.main_activity.CoreMainActivity
import com.lenta.shared.platform.fragment.CoreFragment

fun <T : ViewModel> Fragment.provideViewModel(clazz: Class<T>): T {
    return ViewModelProvider(this).get(clazz)
}

fun CoreFragment<*, *>.generateScreenNumber(): String {
    return activity.implementationOf(CoreMainActivity::class.java)?.generateNumberScreen(this).orEmpty()
}

fun CoreFragment<*, *>.generateScreenNumberFromPostfix(postfix: String?): String? {
    return activity.implementationOf(CoreMainActivity::class.java)?.generateNumberScreenFromPostfix(postfix)
}

fun CoreFragment<*, *>.getScreenPrefix(): String {
    return activity.implementationOf(CoreMainActivity::class.java)?.getPrefixScreen(this).orEmpty()
}

fun <T> Fragment.connectLiveData(source: MutableLiveData<out T>, target: MutableLiveData<T>) {
    this.viewLifecycleOwner.connectLiveData(source, target)
}