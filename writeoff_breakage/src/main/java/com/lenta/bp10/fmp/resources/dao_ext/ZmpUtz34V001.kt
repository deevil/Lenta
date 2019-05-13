package com.lenta.bp10.fmp.resources.dao_ext

import com.lenta.bp10.fmp.resources.fast.ZmpUtz34V001

fun ZmpUtz34V001.getMaterialTypes(taskType: String): MutableList<ZmpUtz34V001.ItemLocal_ET_MTART> {
    return localHelper_ET_MTART.getWhere("TASK_TYPE = \"$taskType\"")
}