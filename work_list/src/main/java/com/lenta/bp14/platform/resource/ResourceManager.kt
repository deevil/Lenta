package com.lenta.bp14.platform.resource

import android.content.Context
import com.lenta.bp14.R

import javax.inject.Inject

class ResourceManager @Inject constructor(val context: Context) : IResourceManager {
    override fun serverConnectionError(): String = context.getString(R.string.server_connection_error)
}

interface IResourceManager {
    fun serverConnectionError(): String
}