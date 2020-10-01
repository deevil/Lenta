package com.lenta.movement.platform.navigation

import android.content.Context
import com.lenta.movement.R
import com.lenta.movement.platform.IResourcesManager
import javax.inject.Inject

private const val yesId = R.string.yes
private const val noId = R.string.no

class ResourcesManager @Inject constructor(private val context: Context) : IResourcesManager {
    override val yesTitle: String
        get() = context.getString(yesId)
    override val noTitle: String
        get() = context.getString(noId)
}