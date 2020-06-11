package com.lenta.bp12.model.pojo

import com.lenta.bp12.request.pojo.ProducerInfo

data class Mark(
        var material: String,
        var markNumber: String,
        var boxNumber: String = "",
        var isBadMark: Boolean,
        var producerInfo: ProducerInfo
)