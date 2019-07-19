package com.lenta.shared.requests.combined.scan_info.pojo


data class MaterialInfo(
        val material: String,
        val name: String,
        val matype: String,
        val buom: String,
        val mhdhbDays: Int,
        val mhdrzDays: Int,
        val bstme: String
)