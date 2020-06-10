package com.lenta.bp12.platform.resource

import android.content.Context
import com.lenta.bp12.R

import javax.inject.Inject

class ResourceManager @Inject constructor(
        val context: Context
) : IResourceManager {

    //override fun workWith(taskType: String, quantity: Int): String = context.getString(R.string.work_with_pu_es_quantity, taskType, quantity)

}

interface IResourceManager {
    //fun workWith(taskType: String, quantity: Int): String
}