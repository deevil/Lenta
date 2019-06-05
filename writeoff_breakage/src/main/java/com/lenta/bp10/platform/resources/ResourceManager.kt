package com.lenta.bp10.platform.resources

import android.content.Context
import com.lenta.bp10.R
import com.lenta.shared.platform.resources.ISharedStringResourceManager


class StringResourceManager (val context: Context, sharedStringResourceManager: ISharedStringResourceManager) :
        IStringResourceManager, ISharedStringResourceManager by sharedStringResourceManager {
    override fun selectTaskType(): String = context.getString(R.string.select_task_type)
}

interface IStringResourceManager : ISharedStringResourceManager {
    fun selectTaskType(): String

}