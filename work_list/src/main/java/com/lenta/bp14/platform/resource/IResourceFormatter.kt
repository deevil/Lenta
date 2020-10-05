package com.lenta.bp14.platform.resource

import com.lenta.bp14.models.work_list.ZPart

interface IResourceFormatter {
    fun getFormattedZPartInfo(zPart: ZPart): String
    fun getLargeFormattedZPartInfo(zPart: ZPart): String
    fun getStorageZPartInfo(storage: String): String
}