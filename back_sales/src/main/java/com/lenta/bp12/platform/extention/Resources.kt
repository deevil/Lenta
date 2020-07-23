package com.lenta.bp12.platform.extention

import com.lenta.bp12.model.pojo.AlcoCodeInfo
import com.lenta.bp12.model.pojo.ReturnReason
import com.lenta.bp12.model.pojo.TaskType
import com.lenta.bp12.request.pojo.ProviderInfo
import com.lenta.shared.fmp.resources.fast.ZmpUtz39V001
import com.lenta.shared.fmp.resources.fast.ZmpUtz44V001
import com.lenta.shared.fmp.resources.slow.ZfmpUtz48V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz09V001
import com.lenta.shared.fmp.resources.slow.ZmpUtz22V001
import com.lenta.shared.utilities.extentions.isSapTrue


fun ZmpUtz09V001.getProviderInfo(code: String): ProviderInfo? {
    val formattedCode = code.addZerosToStart(10)

    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_VENDORS.getWhere("VENDOR = \"$formattedCode\" LIMIT 1").firstOrNull()?.let { provider ->
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
                code = it.taskType,
                description = it.annotation,
                isDivBySection = it.divAbtnr.isSapTrue(),
                isDivByPurchaseGroup = it.divEkgrp.isSapTrue()
        )
    }
}

fun ZmpUtz39V001.getTaskType(code: String): TaskType? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_TASK_TPS.getWhere("TASK_TYPE = \"$code\" LIMIT 1").firstOrNull()?.let {
        TaskType(
                code = it.taskType,
                description = it.annotation,
                isDivBySection = it.divAbtnr.isSapTrue(),
                isDivByPurchaseGroup = it.divEkgrp.isSapTrue()
        )
    }
}

fun ZmpUtz44V001.getReturnReason(taskType: String, reasonCode: String): ReturnReason? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_TASK_REASONS.getWhere("TASK_TYPE = \"$taskType\" AND REASON = \"$reasonCode\" LIMIT 1").firstOrNull()?.let {
        ReturnReason(
                code = it.reason,
                description = it.grtxt
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

fun ZmpUtz22V001.getAlcoCodeInfoList(alcoCode: String): List<AlcoCodeInfo> {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_ALCOD_LIST.getWhere("ZALCCOD = \"$alcoCode\"").map {
        AlcoCodeInfo(
                material = it.matnr,
                code = it.zalccod
        )
    }
}

fun ZfmpUtz48V001.getNameByMaterial(material: String): String? {
    @Suppress("INACCESSIBLE_TYPE")
    return localHelper_ET_MATNR_LIST.getWhere("MATERIAL = \"$material\" LIMIT 1").firstOrNull()?.name
}