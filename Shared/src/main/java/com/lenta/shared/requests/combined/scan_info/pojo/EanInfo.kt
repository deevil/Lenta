package com.lenta.shared.requests.combined.scan_info.pojo


data class EanInfo(
        val ean: String,
        val materialNumber: String,
        val umren: Int,
        val umrez: Int,
        val uom: String
)