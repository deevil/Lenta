package com.lenta.bp12.model.pojo

import com.lenta.bp12.model.MarkType

data class MarkTypeGroup(
        val name: String,
        val code: String,
        val abbreviation: String,
        val markTypes: Set<MarkType>
)