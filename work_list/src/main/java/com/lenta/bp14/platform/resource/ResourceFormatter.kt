package com.lenta.bp14.platform.resource

import com.lenta.bp14.models.work_list.ZPart
import com.lenta.bp14.uitls.convertToApplicationTime
import javax.inject.Inject

class ResourceFormatter @Inject constructor(
        private val resourceManager: IResourceManager
) : IResourceFormatter {
    override fun getFormattedZPartInfo(zPart: ZPart): String = resourceManager.zPartInfoPattern.format(
            zPart.batch,
            zPart.producer,
            zPart.dateProd.convertToApplicationTime(),
            zPart.dateExpir.convertToApplicationTime()
    )

    override fun getStorageZPartInfo(storage: String): String {
        return resourceManager.storageZPartsPattern.format(storage)
    }

}
