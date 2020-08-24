package com.lenta.bp16.platform.resource

import android.content.Context
import com.lenta.bp16.R
import com.lenta.shared.utilities.extentions.getDeviceId
import com.lenta.shared.utilities.extentions.getDeviceIp
import com.lenta.shared.utilities.extentions.unsafeLazy

import javax.inject.Inject

class ResourceManager @Inject constructor(
        val context: Context
) : IResourceManager {

    override fun workWith(taskType: String, quantity: Int): String = context.getString(R.string.work_with_pu_es_quantity, taskType, quantity)

    override fun defectMark(): String = context.getString(R.string.defect_mark)

    override fun kgSuffix(): String {
        return context.getString(R.string.text_weight_hint)
    }

    override val deviceIp: String by unsafeLazy { context.getDeviceIp() }
}

interface IResourceManager {
    val deviceIp: String

    fun kgSuffix(): String
    fun workWith(taskType: String, quantity: Int): String
    fun defectMark(): String
}