package com.lenta.shared.utilities.extentions

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

fun <T: ViewModel> Fragment.provideViewModel(clazz: Class<T>): T {
    return ViewModelProviders.of(this).get(clazz)
}