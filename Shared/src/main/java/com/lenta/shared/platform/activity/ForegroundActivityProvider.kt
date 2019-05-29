package com.lenta.shared.platform.activity

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.platform.activity.main_activity.CoreMainActivity
import java.lang.ref.WeakReference

class ForegroundActivityProvider {

    val onPauseStateLiveData = MutableLiveData(true)

    private var weekReference: WeakReference<CoreMainActivity>? = null

    fun setActivity(baseMainActivity: CoreMainActivity) {
        weekReference = WeakReference(baseMainActivity)
        onPauseStateLiveData.postValue(false)
    }

    fun clear() {
        weekReference?.clear()
        weekReference = null
        onPauseStateLiveData.postValue(true)
    }

    fun getActivity(): CoreMainActivity? {
        return weekReference?.get()
    }

}