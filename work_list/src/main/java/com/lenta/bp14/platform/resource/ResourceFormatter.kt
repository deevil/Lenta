package com.lenta.bp14.platform.resource

import com.lenta.bp14.models.work_list.ZPart
import com.lenta.bp14.uitls.convertToApplicationTime
import javax.inject.Inject

class ResourceFormatter @Inject constructor(
        private val resourceManager: IResourceManager
) : IResourceFormatter {
    override fun getFormattedZPartInfo(zPart: ZPart): String {
        val dateProd = zPart.getProdDate()
        val dateProdFormat = resourceManager.prodDatePattern.format(dateProd)

        val dateExpir = zPart.getExpirDate()
        val dateExpirPattern = resourceManager.expirDatePattern.format(dateExpir)

        val divider = resourceManager.datesDivider

        val dateLine = if (dateProd.isEmpty()) {
            dateExpirPattern
        } else {
            "$dateProdFormat $divider $dateExpirPattern"
        }

        return resourceManager.threeLinesPattern.format(zPart.batch, zPart.producer, dateLine)
    }

    override fun getLargeFormattedZPartInfo(zPart: ZPart): String {
        val dateProd = zPart.getProdDate()
        val dateProdFormat = resourceManager.prodDateLongPattern.format(dateProd)

        val dateExpir = zPart.getExpirDate()
        val dateExpirPattern = resourceManager.expirDateLongPattern.format(dateExpir)

        return if (dateProd.isEmpty()) {
            resourceManager.threeLinesPattern.format(zPart.batch, zPart.producer, dateExpirPattern)
        } else {
            resourceManager.fourLinesPattern.format(zPart.batch, zPart.producer, dateProdFormat, dateExpirPattern)
        }
    }

    override fun getStorageZPartInfo(storage: String): String {
        return resourceManager.storageZPartsPattern.format(storage)
    }

    private fun ZPart.getProdDate(): String = dateProd.convertToApplicationTime()

    private fun ZPart.getExpirDate(): String = dateExpir.convertToApplicationTime()
}
