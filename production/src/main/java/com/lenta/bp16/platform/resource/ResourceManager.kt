package com.lenta.bp16.platform.resource

import android.content.Context
import com.lenta.bp16.R

import javax.inject.Inject

class ResourceManager @Inject constructor(
        val context: Context
) : IResourceManager {

    override fun workWith(taskType: String, quantity: Int): String = context.getString(R.string.work_with_pu_es_quantity, taskType, quantity)

    override fun defectMark(): String = context.getString(R.string.defect_mark)
}

interface IResourceManager {

    fun workWith(taskType: String, quantity: Int): String
    fun defectMark(): String

}