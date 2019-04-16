package com.lenta.shared.platform.activity

import com.lenta.shared.platform.activity.main_activity.CoreMainActivity
import java.lang.ref.WeakReference

class ForegroundActivityProvider {

    private var weekReference: WeakReference<CoreMainActivity>? = null

    fun setActivity(baseMainActivity: CoreMainActivity) {
        weekReference = WeakReference(baseMainActivity)
    }

    fun clear() {
        weekReference?.clear()
        weekReference = null
    }

    fun getActivity(): CoreMainActivity? {
        return weekReference?.get()
    }

}