package com.lenta.bp15.platform.resource

import android.content.Context
import javax.inject.Inject

class ResourceManager @Inject constructor(
        val context: Context
) : IResourceManager {



}

interface IResourceManager {

    //val deviceIp: String
    //fun tk(number: String): String

}