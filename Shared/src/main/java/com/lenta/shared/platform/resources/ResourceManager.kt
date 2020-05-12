package com.lenta.shared.platform.resources

import android.content.Context
import com.lenta.shared.R
import javax.inject.Inject


class SharedStringResourceManager @Inject constructor(val context: Context) : ISharedStringResourceManager {
    override fun notSelected(): String = context.getString(R.string.not_selected)
    override fun emptyCategory(): String = context.getString(R.string.empty_category)
    override fun loadingNewAppVersion(): String = context.getString(R.string.loading_new_app_version)
    override fun checkAppUpdates(): String = context.getString(R.string.check_app_updates)
}

interface ISharedStringResourceManager {
    fun notSelected(): String
    fun emptyCategory(): String
    fun loadingNewAppVersion(): String
    fun checkAppUpdates(): String

}