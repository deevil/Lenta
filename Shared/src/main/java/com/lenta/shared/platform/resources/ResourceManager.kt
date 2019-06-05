package com.lenta.shared.platform.resources

import android.content.Context
import com.lenta.shared.R
import javax.inject.Inject


class SharedStringResourceManager @Inject constructor(val context: Context) : ISharedStringResourceManager {
    override fun notSelected(): String = context.getString(R.string.not_selected)
}

interface ISharedStringResourceManager {
    fun notSelected(): String

}