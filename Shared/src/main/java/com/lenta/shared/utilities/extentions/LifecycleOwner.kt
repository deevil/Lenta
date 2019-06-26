package com.lenta.shared.utilities.extentions

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

fun <T> LifecycleOwner.connectLiveData(source: LiveData<out T>, target: MutableLiveData<T>) {
    source.observe(this, Observer {
        target.value = it
    })
}