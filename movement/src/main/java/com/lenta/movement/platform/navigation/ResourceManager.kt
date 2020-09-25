package com.lenta.movement.platform.navigation

import android.content.Context
import com.lenta.movement.R
import com.lenta.movement.platform.IResourceManager
import javax.inject.Inject

private const val yesId = R.string.yes
private const val noId = R.string.no

class ResourceManager @Inject constructor(private val context: Context) : IResourceManager {
    override val yes: String
        get() = context.getString(yesId)
    override val no: String
        get() = context.getString(noId)
}