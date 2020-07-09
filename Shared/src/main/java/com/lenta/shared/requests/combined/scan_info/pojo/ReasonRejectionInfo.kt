package com.lenta.shared.requests.combined.scan_info.pojo

class ReasonRejectionInfo(
        val id: String,
        val qualityCode: String,
        val code: String,
        val name: String
) {
    fun convertToQualityInfo(): QualityInfo {
        return QualityInfo(
                id = this.id,
                code = this.code,
                name = this.name
        )

    }
}