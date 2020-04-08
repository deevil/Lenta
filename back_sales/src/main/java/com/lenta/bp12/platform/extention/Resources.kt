package com.lenta.bp12.platform.extention

import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz39V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz44V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz09V001


fun ZmpUtz09V001.getProviderInfo(code: String): ProviderInfo? {
    var formattedCode = code
    while (formattedCode.length < 10) {
        formattedCode = "0$formattedCode"
    }

    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_VENDORS.getWhere("VENDOR = \"$formattedCode\" LIMIT 1").first()?.let { provider ->
        ProviderInfo(
                code = formattedCode,
                name = provider.vendorname
        )
    }
}

fun ZmpUtz39V001.getTaskTypeList(): List<TaskType> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_TASK_TPS.all.map {
        TaskType(
                type = it.taskType,
                description = it.annotation,
                section = it.divAbtnr,
                purchaseGroup = it.divEkgrp
        )
    }
}

fun ZmpUtz39V001.getTaskType(code: String): TaskType? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_TASK_TPS.getWhere("TASK_TYPE = \"$code\" LIMIT 1").first()?.let {
        TaskType(
                type = it.taskType,
                description = it.annotation,
                section = it.divAbtnr,
                purchaseGroup = it.divEkgrp
        )
    }
}

fun ZmpUtz44V001.getReturnReasonList(taskType: String): List<ReturnReason> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_TASK_REASONS.getWhere("TASK_TYPE = \"$taskType\"").map {
        ReturnReason(
                code = it.reason,
                description = it.grtxt
        )
    }
}