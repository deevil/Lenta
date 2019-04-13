package com.lenta.shared.platform.activity

import com.lenta.shared.platform.activity.main_activity.BaseMainActivity
import java.lang.ref.WeakReference

class ForegroundActivityProvider {

    private var weekReference: WeakReference<BaseMainActivity>? = null

    fun setActivity(baseMainActivity: BaseMainActivity) {
        weekReference = WeakReference(baseMainActivity)
    }

    fun clear() {
        weekReference?.clear()
        weekReference = null
    }

    fun getActivity(): BaseMainActivity? {
        return weekReference?.get()
    }

}