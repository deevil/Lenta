package com.lenta.shared.platform.resources

import android.content.Context
import com.lenta.shared.R
import javax.inject.Inject


class StringResourceManager @Inject constructor(val context: Context) : IStringResourceManager {
    override fun notSelected(): String = context.getString(R.string.not_selected)
}

interface IStringResourceManager {
    fun notSelected(): String

}