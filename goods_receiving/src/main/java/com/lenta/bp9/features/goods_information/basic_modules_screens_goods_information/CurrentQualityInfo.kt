package com.lenta.bp9.features.goods_information.basic_modules_screens_goods_information

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.requests.combined.scan_info.pojo.QualityInfo

interface CurrentQualityInfo {
    val qualityInfo: MutableLiveData<List<QualityInfo>>
    val spinQualitySelectedPosition: MutableLiveData<Int>
    val currentQualityInfoCode: String
        get() {
            val position = spinQualitySelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.run {
                        qualityInfo.value
                                ?.getOrNull(this)
                                ?.code
                                .orEmpty()
                    }.orEmpty()
        }
}