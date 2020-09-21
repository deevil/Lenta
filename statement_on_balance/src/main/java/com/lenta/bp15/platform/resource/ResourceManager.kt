package com.lenta.bp15.platform.resource

import android.content.Context
import com.lenta.bp15.R
import javax.inject.Inject

class ResourceManager @Inject constructor(
        val context: Context
) : IResourceManager {

    override fun tk(number: String): String = context.getString(R.string.tk_number, number)

}

interface IResourceManager {

    fun tk(number: String): String

}