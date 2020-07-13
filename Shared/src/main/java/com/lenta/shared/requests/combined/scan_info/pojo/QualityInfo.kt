package com.lenta.shared.requests.combined.scan_info.pojo

data class QualityInfo(
        val id: String,
        val code: String,
        val name: String
) {
    fun convertToReasonRejectionInfo(): ReasonRejectionInfo {
        return ReasonRejectionInfo(
                id = this.id,
                qualityCode = "",
                code = this.code,
                name = this.name
        )
    }
}