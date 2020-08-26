package com.lenta.bp9.features.goods_information.basic_modules_screens_goods_information

import androidx.lifecycle.MutableLiveData
import com.lenta.shared.requests.combined.scan_info.pojo.ReasonRejectionInfo

interface CurrentReasonRejectionInfo {
    val reasonRejectionInfo: MutableLiveData<List<ReasonRejectionInfo>>
    val spinReasonRejectionSelectedPosition: MutableLiveData<Int>
    val currentReasonRejectionInfoCode: String
        get() {
            val position = spinReasonRejectionSelectedPosition.value ?: -1
            return position
                    .takeIf { it >= 0 }
                    ?.run {
                        reasonRejectionInfo.value
                                ?.getOrNull(this)
                                ?.code
                                .orEmpty()
                    }
                    .orEmpty()
        }
}